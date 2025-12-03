package com.example.lolnk.util

import android.util.Log
import com.example.lolnk.data.local.Message
import com.example.lolnk.util.Constants.AES_KEY
import com.example.lolnk.util.Constants.MESSAGE_TYPE_GPS
import com.example.lolnk.util.Constants.MESSAGE_TYPE_TEXT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    private const val AES_ALGORITHM = "AES/ECB/NoPadding"
    private const val HMAC_ALGORITHM = "HmacSHA256"

    // AES CTR encryption using AES-ECB keystream (decryption is the same)
    fun aesCtrCrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        val aesCipher = Cipher.getInstance(AES_ALGORITHM)
        val secretKeySpec = SecretKeySpec(key, "AES")

        val out = ByteArray(data.size)
        val blocks = (data.size + 15) / 16

        for (b in 0 until blocks) {
            val counter = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(b.toLong()).array()
            val block = iv + counter
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val ks = aesCipher.doFinal(block)

            for (i in 0 until 16) {
                val idx = b * 16 + i
                if (idx < data.size) {
                    out[idx] = (data[idx] xor ks[i]).toByte()
                }
            }
        }
        return out
    }

    // Simple HMAC-like tag
    fun makeTag(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        val secretKeySpec = SecretKeySpec(key, HMAC_ALGORITHM)
        mac.init(secretKeySpec)
        mac.update(iv)
        mac.update(ciphertext)
        return mac.doFinal().copyOfRange(0, 4)
    }

    // Pack GPS plaintext data (Type + NodeId + Timestamp + Lat + Lon)
    fun packGpsPlaintext(nodeId: Int, timestamp: Long, lat: Int, lon: Int): ByteArray {
        val buffer = ByteBuffer.allocate(1 + 4 + 4 + 4).order(ByteOrder.BIG_ENDIAN)
        buffer.put(MESSAGE_TYPE_GPS)
        buffer.put(nodeId.toByte())
        buffer.putInt(timestamp.toInt())
        buffer.putInt(lat)
        buffer.putInt(lon)
        return buffer.array()
    }

    // Pack Text Message plaintext data (Type + NodeId + Timestamp + TextLength + Text)
    fun packTextMessage(nodeId: Int, timestamp: Long, text: String): ByteArray {
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val textLength = textBytes.size
        val buffer = ByteBuffer.allocate(1 + 4 + 4 + 2 + textLength).order(ByteOrder.BIG_ENDIAN)
        buffer.put(MESSAGE_TYPE_TEXT)
        buffer.put(nodeId.toByte())
        buffer.putInt(timestamp.toInt())
        buffer.putShort(textLength.toShort())
        buffer.put(textBytes)
        return buffer.array()
    }

    // Encrypt and tag a LoRa packet
    fun encryptAndTagLoRaPacket(senderNodeId: Int, plaintext: ByteArray): ByteArray {
        // Encryption is disabled, return raw plaintext
        return plaintext
    }

    // Parse and decrypt LoRa packet
    fun parseAndDecryptLoRaPacket(packet: ByteArray): Message? {
        if (packet.isEmpty()) {
            Log.e("CryptoUtils", "Packet is empty")
            return null
        }

        // Decryption is disabled, treat packet as raw plaintext
        val plaintext = packet

        // Parse based on message type
        val messageType = plaintext[0]
        val senderNodeId = plaintext[1].toInt() and 0xFF
        val timestamp = ByteBuffer.wrap(plaintext, 2, 4).order(ByteOrder.BIG_ENDIAN).getInt().toLong() and 0xFFFFFFFFL

        return when (messageType) {
            MESSAGE_TYPE_GPS -> {
                if (plaintext.size != 13) {
                    Log.e("CryptoUtils", "GPS plaintext incorrect size: ${plaintext.size}")
                    return null
                }
                val lat = ByteBuffer.wrap(plaintext, 6, 4).order(ByteOrder.BIG_ENDIAN).getInt()
                val lon = ByteBuffer.wrap(plaintext, 10, 4).order(ByteOrder.BIG_ENDIAN).getInt()
                Message(
                    type = messageType.toInt(),
                    nodeId = senderNodeId,
                    timestamp = timestamp * 1000L,
                    latitude = lat / 1e7,
                    longitude = lon / 1e7,
                    message = "",
                    keyIndex = 0,
                    tag = "",
                    isIncoming = true
                )
            }
            MESSAGE_TYPE_TEXT -> {
                if (plaintext.size < 8) { // Min size: type(1) + nodeId(1) + timestamp(4) + textLength(2)
                    Log.e("CryptoUtils", "Text plaintext too short: ${plaintext.size}")
                    return null
                }
                val textLength = ByteBuffer.wrap(plaintext, 6, 2).order(ByteOrder.BIG_ENDIAN).getShort().toInt()
                val textBytes = ByteArray(textLength)
                System.arraycopy(plaintext, 8, textBytes, 0, textLength)
                val text = String(textBytes, Charsets.UTF_8)
                Message(
                    type = messageType.toInt(),
                    nodeId = senderNodeId,
                    timestamp = timestamp * 1000L,
                    latitude = 0.0, // Not applicable for text messages
                    longitude = 0.0, // Not applicable for text messages
                    message = text,
                    keyIndex = 0,
                    tag = "",
                    isIncoming = true
                )
            }
            else -> {
                Log.e("CryptoUtils", "Unknown message type: $messageType")
                null
            }
        }
    }

    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    // Helper for XOR operation
    private infix fun Byte.xor(other: Byte): Byte = (this.toInt() xor other.toInt()).toByte()
}