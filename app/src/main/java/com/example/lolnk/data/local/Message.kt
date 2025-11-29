package com.example.lolnk.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: Int,
    val nodeId: Int,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val message: String,
    val keyIndex: Int,
    val tag: String,
    val isIncoming: Boolean = true
)