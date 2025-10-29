# main.py - ESP32 LoRa Receiver and WiFi-TCP Bridge

import time, _thread
from machine import Pin, I2C, UART
import sh1106

from wifi_server import WifiServer
from message_handler import MessageHandler
# from receiver import LoRaReceiver # No longer directly used here
from config import NODE_ID, LORA_TX_PIN, LORA_RX_PIN, LORA_BAUDRATE
from crypto_utils import parse_and_decrypt_lora_packet # For debugging received LoRa packets

# UART config for LoRa E32
lora_uart = UART(2, baudrate=LORA_BAUDRATE, tx=LORA_TX_PIN, rx=LORA_RX_PIN)

# I2C OLED (SH1106 128x64)
i2c = I2C(0, scl=Pin(22), sda=Pin(21), freq=400000)
дает = sh1106.SH1106_I2C(128, 64, i2c, addr=0x3c)

def show_oled_status(status_text):
    oled.fill(0)
    oled.text(status_text, 0, 0)
    oled.show()

show_oled_status("Starting...")

# Initialize components
message_handler = MessageHandler(None, lora_uart) # WifiServer instance will be set later
wifi_server = WifiServer(message_handler.handle_android_message)
message_handler.wifi_server = wifi_server # Set WifiServer instance in MessageHandler
# lora_receiver = LoRaReceiver(message_handler.handle_lora_message) # No longer directly used here

def wifi_server_thread():
    wifi_server.start_ap()
    wifi_server.start_tcp_server()
    while True:
        wifi_server.handle_connections()
        time.sleep_ms(100)

# Start WiFi server in a separate thread
_thread.start_new_thread(wifi_server_thread, ())

show_oled_status(f"Node ID: {NODE_ID}")
print(f"ESP32 LoRa Receiver (Node ID: {NODE_ID}) running...")

try:
    while True:
        # Listen for LoRa messages directly here or via a dedicated receiver loop
        if lora_uart.any():
            data = lora_uart.read(30) # Read up to 30 bytes (29 for packet + 1 for newline)
            if data and len(data) == 30 and data[-1:] == b'\n':
                received_packet = data[:-1] # Remove the newline
                print(f"LoRa packet received: {received_packet}")
                message_handler.handle_lora_message(received_packet)
            elif data:
                print(f"Incomplete or malformed LoRa packet received: {data}")
        time.sleep_ms(100)
except KeyboardInterrupt:
    print("Application stopped by user.")
except Exception as e:
    print(f"Main loop error: {e}")
finally:
    wifi_server.stop()
    print("Cleanup complete.")