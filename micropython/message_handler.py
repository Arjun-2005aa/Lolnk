# message_handler.py

import time
from crypto_utils import parse_and_decrypt_lora_packet, encrypt_and_tag_lora_packet, pack_text_message
from config import NODE_ID, MESSAGE_TYPE_TEXT

class MessageHandler:
    def __init__(self, wifi_server_instance, lora_uart_instance):
        self.wifi_server = wifi_server_instance
        self.lora_uart = lora_uart_instance

    def handle_lora_message(self, message_bytes):
        # This is where LoRa messages are processed and potentially forwarded to Android
        print(f"LoRa Message Received: {message_bytes}")
        # Decrypt and parse the message
        parsed_message = parse_and_decrypt_lora_packet(message_bytes)
        if parsed_message:
            print(f"Decrypted LoRa Message: {parsed_message}")
            # Forward to Android (Android app will decrypt again, this is for simplicity for now)
            if self.wifi_server:
                self.wifi_server.send_to_android(message_bytes + b'\n') # Append newline as delimiter for TCP

    def handle_android_message(self, message_bytes):
        # This is where messages from the Android app are processed
        # The Android app now sends a full encrypted LoRa packet
        print(f"Android Encrypted LoRa Packet Received: {message_bytes}")
        if self.lora_uart:
            # Directly forward the encrypted packet received from Android to LoRa
            self.lora_uart.write(message_bytes + b'\n') # Add newline delimiter for LoRa UART
            print(f"Forwarded Android message to LoRa: {message_bytes}")