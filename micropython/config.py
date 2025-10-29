# config.py

NODE_ID = 1  # Unique for each ESP32 device
WIFI_SSID = "LoRa_Chat_1"
WIFI_PASSWORD = "lorachat123"
TCP_PORT = 8080

# LoRa E32 UART pins
LORA_TX_PIN = 17
LORA_RX_PIN = 16
LORA_BAUDRATE = 9600

# Message Types
MESSAGE_TYPE_GPS = 0x01
MESSAGE_TYPE_TEXT = 0x02
