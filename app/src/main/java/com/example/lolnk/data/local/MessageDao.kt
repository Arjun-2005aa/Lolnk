package com.example.lolnk.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages WHERE nodeId = :nodeId ORDER BY timestamp ASC")
    fun getMessagesForNode(nodeId: Int): Flow<List<Message>>
}