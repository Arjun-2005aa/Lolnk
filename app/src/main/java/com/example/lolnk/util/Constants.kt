package com.example.lolnk.util

object Constants {
    const val ESP32_IP = "192.168.4.1"
    const val ESP32_PORT = 8080
    const val LOCATION_UPDATE_INTERVAL = 5000L
    val AES_KEY = "1234567890ABCDEF".toByteArray(Charsets.UTF_8)

    // Message Types
    const val MESSAGE_TYPE_GPS = 0x01.toByte()
    const val MESSAGE_TYPE_TEXT = 0x02.toByte()
}