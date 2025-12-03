package com.example.lolnk.util

object Constants {
    const val ESP32_IP = "192.168.4.1"
    const val ESP32_PORT = 8080
    const val LOCATION_UPDATE_INTERVAL = 5000L

    // Message Types
    const val MESSAGE_TYPE_GPS = 0x01.toByte()
    const val MESSAGE_TYPE_TEXT = 0x02.toByte()
}