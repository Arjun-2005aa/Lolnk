package com.example.lolnk

import android.app.Application
import com.example.lolnk.data.local.AppDatabase
import com.example.lolnk.repository.ContactRepository
import com.example.lolnk.repository.MessageRepository
import com.example.lolnk.network.NetworkService

class LolnkApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val contactRepository by lazy { ContactRepository(database.contactDao()) }
    val messageRepository by lazy { MessageRepository(database.messageDao()) }
    val networkService by lazy { NetworkService(messageRepository) }
}