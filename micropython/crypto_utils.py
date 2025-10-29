# crypto_utils.py

import os, time, struct, ucryptolib, uhashlib
from config import AES_KEY, MESSAGE_TYPE_GPS, MESSAGE_TYPE_TEXT

# ---- AES CTR encryption using AES-ECB keystream ----
def aes_ctr_crypt(key, iv, data):
    aes = ucryptolib.aes(key, 1)  # 1 = AES-ECB
    out = bytearray(len(data))
    blocks = (len(data) + 15) // 16
    for b in range(blocks):
        counter = b.to_bytes(8, "big")
        block = iv + counter
        ks = aes.encrypt(block)
        for i in range(16):
            idx = b * 16 + i
            if idx < len(data):
                out[idx] = data[idx] ^ ks[i]
    return out

# ---- simple HMAC-like tag ----
def make_tag(key, iv, ciphertext):
    h = uhashlib.sha256()
    h.update(key)
    h.update(iv)
    h.update(ciphertext)
    return h.digest()[:4]

# ---- pack GPS plaintext data (Type + NodeId + Timestamp + Lat + Lon) ----
def pack_gps_plaintext(node_id, timestamp, lat, lon):
    # Clamp lat/lon to signed int32 range
    lat = max(min(int(lat), 2147483647), -2147483648)
    lon = max(min(int(lon), 2147483647), -2147483648)
    return struct.pack(">BBIii", MESSAGE_TYPE_GPS, int(node_id), int(timestamp), lat, lon)
    # total = 1 (type) + 1 (node_id) + 4 (timestamp) + 4 (lat) + 4 (lon) = 14 bytes

# ---- pack Text Message plaintext data (Type + NodeId + Timestamp + TextLength + Text) ----
def pack_text_message(node_id, timestamp, text):
    text_bytes = text.encode('utf-8')
    text_length = len(text_bytes)
    # Max text length is limited by packet size, here we assume it fits
    # Format: Type (1) + NodeId (1) + Timestamp (4) + TextLength (2) + Text (var)
    return struct.pack(">BBIH", MESSAGE_TYPE_TEXT, int(node_id), int(timestamp), text_length) + text_bytes

# ---- unpack plaintext data ----
def unpack_plaintext(data):
    message_type = data[0]
    sender_node_id = data[1]
    timestamp = struct.unpack_from(">I", data, 2)[0]

    if message_type == MESSAGE_TYPE_GPS:
        # GPS: Type (1) + NodeId (1) + Timestamp (4) + Lat (4) + Lon (4) = 14 bytes
        if len(data) != 14:
            return None # Malformed GPS packet
        lat, lon = struct.unpack_from(">ii", data, 6)
        return {"type": message_type, "node_id": sender_node_id, "timestamp": timestamp, "lat": lat, "lon": lon}
    elif message_type == MESSAGE_TYPE_TEXT:
        # Text: Type (1) + NodeId (1) + Timestamp (4) + TextLength (2) + Text (var)
        if len(data) < 8:
            return None # Malformed Text packet (too short for header)
        text_length = struct.unpack_from(">H", data, 6)[0]
        text_bytes = data[8:8+text_length]
        text = text_bytes.decode('utf-8')
        return {"type": message_type, "node_id": sender_node_id, "timestamp": timestamp, "text": text}
    else:
        return None # Unknown message type

# ---- Encrypt and tag a LoRa packet ----
def encrypt_and_tag_lora_packet(sender_node_id, plaintext):
    iv = os.urandom(8)
    ciphertext = aes_ctr_crypt(AES_KEY, iv, plaintext)
    tag = make_tag(AES_KEY, iv, ciphertext)

    # Packet format: frame_cnt(4) + iv(8) + ciphertext(var) + tag(4) + newline
    # frame_cnt is a placeholder here, will be added by main loop or handled by Android
    # For now, we'll use a dummy frame_cnt = 0
    frame_cnt = 0 # Placeholder
    pkt = frame_cnt.to_bytes(4, "big") + iv + ciphertext + tag
    return pkt

# ---- Parse and decrypt LoRa packet ----
def parse_and_decrypt_lora_packet(packet_bytes):
    if len(packet_bytes) < 17: # Min size: frame_cnt(4) + iv(8) + type(1) + nodeId(1) + timestamp(4) + tag(4) = 22. But ciphertext can be variable.
        print(f"Packet too short: {len(packet_bytes)} bytes")
        return None

    # Extract components
    frame_cnt = int.from_bytes(packet_bytes[0:4], "big")
    iv = packet_bytes[4:12]
    # Ciphertext length is variable, depends on plaintext length
    ciphertext = packet_bytes[12:-4] # Everything between IV and Tag
    received_tag = packet_bytes[-4:]

    # Verify tag
    calculated_tag = make_tag(AES_KEY, iv, ciphertext)
    if received_tag != calculated_tag:
        print("Tag verification failed!")
        return None

    # Decrypt
    plaintext = aes_ctr_crypt(AES_KEY, iv, ciphertext)

    # Unpack plaintext
    return unpack_plaintext(plaintext)
