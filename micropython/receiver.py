# receiver.py

from machine import UART
from config import LORA_TX_PIN, LORA_RX_PIN, LORA_BAUDRATE
import time

class LoRaReceiver:
    def __init__(self, message_handler_callback):
        self.uart = UART(2, baudrate=LORA_BAUDRATE, tx=LORA_TX_PIN, rx=LORA_RX_PIN)
        self.message_handler_callback = message_handler_callback
        print("LoRa UART initialized.")

    def listen_for_lora(self):
        if self.uart.any():
            # Read until newline, assuming packets are terminated by newline
            # This needs to be robust for binary data, might need a fixed-size read or specific delimiter handling
            # For now, we'll assume the transmitter sends a newline after the 29-byte packet
            try:
                # Read up to 30 bytes (29 for packet + 1 for newline)
                data = self.uart.read(30)
                if data and len(data) == 30 and data[-1:] == b'\n':
                    packet = data[:-1] # Remove the newline
                    self.message_handler_callback(packet)
                elif data:
                    print(f"Incomplete LoRa packet or missing newline: {data}")
            except Exception as e:
                print(f"Error reading from LoRa UART: {e}")

