package com.example.lolnk.network

import android.util.Log
import com.example.lolnk.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket

class TcpClient(private val ipAddress: String, private val port: Int) {

    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var messageListener: ((ByteArray) -> Unit)? = null

    fun setMessageListener(listener: (ByteArray) -> Unit) {
        messageListener = listener
    }

    suspend fun connect() = withContext(Dispatchers.IO) {
        try {
            socket = Socket(ipAddress, port)
            inputStream = socket!!.getInputStream()
            outputStream = socket!!.getOutputStream()
            Log.d("TcpClient", "Connected to $ipAddress:$port")
            startListeningForMessages()
        } catch (e: Exception) {
            Log.e("TcpClient", "Error connecting: ${e.message}")
            disconnect()
        }
    }

    suspend fun sendMessage(data: ByteArray) = withContext(Dispatchers.IO) {
        try {
            outputStream?.write(data)
            outputStream?.flush()
            Log.d("TcpClient", "Sent: ${data.toHexString()}")
        } catch (e: Exception) {
            Log.e("TcpClient", "Error sending message: ${e.message}")
            disconnect()
        }
    }

    private fun startListeningForMessages() {
        Thread {
            try {
                val buffer = ByteArray(30) // 29 bytes for packet + 1 for newline
                var bytesRead: Int
                while (socket?.isConnected == true && inputStream != null) {
                    bytesRead = inputStream!!.read(buffer, 0, 30)
                    if (bytesRead > 0) {
                        // Assuming the last byte is the newline character, we pass the first 29 bytes
                        if (bytesRead == 30 && buffer[29].toChar() == '\n') {
                            val packet = buffer.copyOfRange(0, 29)
                            Log.d("TcpClient", "Received packet: ${packet.toHexString()}")
                            messageListener?.invoke(packet)
                        } else {
                            Log.w("TcpClient", "Incomplete or malformed packet received: $bytesRead bytes")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TcpClient", "Error listening for messages: ${e.message}")
            } finally {
                disconnect()
            }
        }.start()
    }

    fun disconnect() {
        try {
            socket?.close()
            inputStream?.close()
            outputStream?.close()
            socket = null
            inputStream = null
            outputStream = null
            Log.d("TcpClient", "Disconnected.")
        } catch (e: Exception) {
            Log.e("TcpClient", "Error disconnecting: ${e.message}")
        }
    }

    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}