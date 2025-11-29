package com.example.lolnk.network

import android.util.Log
import com.example.lolnk.data.local.Message
import com.example.lolnk.repository.MessageRepository
import com.example.lolnk.util.Constants
import com.example.lolnk.util.CryptoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NetworkService(private val messageRepository: MessageRepository) {

    private val tcpClient = TcpClient(Constants.ESP32_IP, Constants.ESP32_PORT)
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    init {
        tcpClient.setMessageListener { packetBytes ->
            serviceScope.launch {
                val message = CryptoUtils.parseAndDecryptLoRaPacket(packetBytes)
                message?.let { messageRepository.insert(it) }
            }
        }
    }

    fun connect() {
        serviceScope.launch {
            tcpClient.connect()
        }
    }

    fun disconnect() {
        tcpClient.disconnect()
    }

    fun sendMessage(recipientNodeId: Int, messageText: String) {
        serviceScope.launch {
            val timestamp = System.currentTimeMillis() / 1000L // Seconds
            // For now, assume current device's NODE_ID is 0 (placeholder)
            val senderNodeId = 0 // TODO: Replace with actual current device's NODE_ID

            val plaintext = CryptoUtils.packTextMessage(senderNodeId, timestamp, messageText)
            val encryptedPacket = CryptoUtils.encryptAndTagLoRaPacket(senderNodeId, plaintext)
            tcpClient.sendMessage(encryptedPacket)
        }
    }
}