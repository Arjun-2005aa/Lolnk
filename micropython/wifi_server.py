# wifi_server.py

import network, socket, time
from config import WIFI_SSID, WIFI_PASSWORD, TCP_PORT

class WifiServer:
    def __init__(self, message_handler_callback):
        self.message_handler_callback = message_handler_callback
        self.ap = None
        self.client_socket = None
        self.server_socket = None

    def start_ap(self):
        self.ap = network.WLAN(network.AP_IF)
        self.ap.active(True)
        self.ap.config(essid=WIFI_SSID, password=WIFI_PASSWORD, authmode=network.AUTH_WPA_WPA2_PSK)
        print(f"Started WiFi AP: {WIFI_SSID} IP: {self.ap.ifconfig()[0]}")

    def start_tcp_server(self):
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_socket.bind(('', TCP_PORT))
        self.server_socket.listen(1)
        print(f"Listening for TCP connections on port {TCP_PORT}")

    def handle_connections(self):
        try:
            conn, addr = self.server_socket.accept()
            conn.setblocking(False)
            self.client_socket = conn
            print(f"Client connected from {addr}")
            while True:
                try:
                    data = self.client_socket.recv(1024)
                    if data:
                        print(f"Received from Android: {data.decode().strip()}")
                        # Here, you would typically process the message from Android
                        # For now, we just acknowledge or pass to a handler
                        self.message_handler_callback(data) # Pass to message handler
                    else:
                        # Client disconnected
                        print("Client disconnected")
                        self.client_socket.close()
                        self.client_socket = None
                        break
                except OSError as e:
                    if e.args[0] == 11: # errno.EAGAIN, no data available
                        time.sleep_ms(10)
                        continue
                    else:
                        raise
        except OSError as e:
            if e.args[0] == 11: # errno.EAGAIN, no incoming connection
                pass
            else:
                print(f"TCP Server error: {e}")
        except Exception as e:
            print(f"Error in handle_connections: {e}")

    def send_to_android(self, data):
        if self.client_socket:
            try:
                self.client_socket.sendall(data)
                # print(f"Sent to Android: {data}")
            except Exception as e:
                print(f"Error sending to Android: {e}")
                self.client_socket.close()
                self.client_socket = None

    def stop(self):
        if self.client_socket:
            self.client_socket.close()
        if self.server_socket:
            self.server_socket.close()
        if self.ap:
            self.ap.active(False)
        print("WiFi Server stopped.")
