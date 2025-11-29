# LoRa Encrypted Messaging App

A secure, peer-to-peer messaging application that uses LoRa (Long Range) communication via ESP32 modules for encrypted messaging with GPS location sharing capabilities.

## Features

- **Encrypted Messaging**: Dynamic per-message key rotation using AES-CTR encryption
- **GPS Location Sharing**: Send and receive location data with messages
- **Live Location Tracking**: Real-time location updates every 5 seconds
- **Peer-to-Peer Architecture**: No cloud servers required
- **Modern UI**: Jetpack Compose with Material3 design
- **WiFi Connectivity**: ESP32 creates WiFi hotspot for phone connection

## Architecture

```
Android App (Kotlin) ↔ WiFi ↔ ESP32 (MicroPython) ↔ LoRa E32 ↔ Other ESP32 Devices
```

## Components

### Android App
- **Database**: Room database for local message storage
- **Encryption**: Dynamic key derivation with per-message rotation
- **UI**: Jetpack Compose with rounded, modern design
- **Networking**: TCP socket communication with ESP32

### ESP32 MicroPython
- **WiFi AP**: Creates "LoRa_Chat_XXXX" network
- **TCP Server**: Handles Android app connections
- **LoRa Bridge**: Forwards messages between Android and LoRa
- **Dual Mode**: Messaging mode and live location mode

## Installation

### Android App

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and install on Android device (API 24+)

### ESP32 Setup

1. Install MicroPython on ESP32
2. Upload the MicroPython files to ESP32:
   - `config.py` - Configuration
   - `wifi_server.py` - WiFi AP and TCP server
   - `message_handler.py` - Message processing
   - `receiver.py` - LoRa packet handling
   - `main.py` - Main application

3. Connect LoRa E32 module:
   - TX: GPIO 17
   - RX: GPIO 16
   - Baudrate: 9600

4. Power on ESP32 and connect to "LoRa_Chat_XXXX" WiFi network

## Usage

### Initial Setup

1. **Connect to ESP32**: Phone connects to ESP32's WiFi network
2. **Set Node ID**: Configure unique node ID for each device
3. **Add Contacts**: Add other users by their Node ID

### Messaging

1. **Send Message**: Type message and send (optionally with location)
2. **Receive Message**: Messages appear in chat interface
3. **Location Sharing**: Tap location button to share current GPS coordinates

### Live Location Mode

1. **Enable Tracking**: Switch to live location mode
2. **Automatic Updates**: Location sent every 5 seconds
3. **Real-time Display**: View other users' live locations

## Encryption Details

### Key Generation
- Initial key generated when first contact is added
- 16-byte random AES key
- Multiplier calculated as `nodeId1 × nodeId2`

### Dynamic Key Rotation
```
new_key = hash(initial_key + multiplier + message_index)
```

### Message Format
```
[TYPE(1)][NODE_ID(4)][TIMESTAMP(4)][LAT(4)][LON(4)][MSG_LEN(2)][MSG(var)][KEY_INDEX(4)][TAG(4)]
```

## Configuration

### ESP32 Configuration (`config.py`)
```python
NODE_ID = 1  # Unique for each ESP32
WIFI_SSID = "LoRa_Chat_1"
WIFI_PASSWORD = "lorachat123"
TCP_PORT = 8080
```

### Android Configuration (`Constants.kt`)
```kotlin
const val ESP32_IP = "192.168.4.1"
const val ESP32_PORT = 8080
const val LOCATION_UPDATE_INTERVAL = 5000L
```

## Security Features

- **End-to-End Encryption**: Messages encrypted before transmission
- **Dynamic Keys**: Each message uses a unique encryption key
- **Authentication**: HMAC-like tags for message integrity
- **No Cloud Storage**: All data stored locally on devices

## Troubleshooting

### Connection Issues
- Ensure phone is connected to ESP32's WiFi network
- Check ESP32 is powered and running MicroPython
- Verify LoRa E32 module connections

### Message Issues
- Check encryption keys are properly synchronized
- Verify both devices have correct Node IDs
- Ensure LoRa modules are within range

### Location Issues
- Grant location permissions to Android app
- Check GPS is enabled and has signal
- Verify location services are working

## File Structure

### Android App
```
app/src/main/java/com/example/lo_chat/
├── data/local/           # Room database
├── network/              # WiFi and encryption
├── repository/           # Data access layer
├── ui/                   # Jetpack Compose UI
├── viewmodel/            # MVVM ViewModels
└── util/                 # Utilities
```

### ESP32 MicroPython
```
micropython/
├── config.py             # Configuration
├── wifi_server.py        # WiFi AP and TCP server
├── message_handler.py    # Message processing
├── receiver.py           # LoRa packet handling
└── main.py              # Main application
```

## Dependencies

### Android
- Jetpack Compose
- Room Database
- Material3
- Coroutines
- Location Services

### ESP32
- MicroPython
- LoRa E32 module
- WiFi capabilities

## License

This project is open source and available under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Support

For issues and questions, please create an issue in the repository.


