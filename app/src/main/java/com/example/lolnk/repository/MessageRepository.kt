package com.example.lolnk.repository

import com.example.lolnk.data.local.Message
import com.example.lolnk.data.local.MessageDao
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {

    fun getMessagesForNode(nodeId: Int): Flow<List<Message>> {
        return messageDao.getMessagesForNode(nodeId)
    }

    suspend fun insert(message: Message) {
        messageDao.insert(message)
    }
}