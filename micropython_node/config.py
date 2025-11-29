# config.py for a standalone LoRa Node

NODE_ID = 2  # Unique ID for this specific node (e.g., different from the Bridge ESP32's NODE_ID=1)

# LoRa E32 UART pins
LORA_TX_PIN = 17
LORA_RX_PIN = 16
LORA_BAUDRATE = 9600

# AES key (must be the same across all communicating devices)
AES_KEY = b"1234567890ABCDEF"
