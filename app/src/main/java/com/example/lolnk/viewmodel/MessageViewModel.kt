package com.example.lolnk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.lolnk.data.local.Message
import com.example.lolnk.repository.MessageRepository
import kotlinx.coroutines.launch

class MessageViewModel(private val repository: MessageRepository, private val nodeId: Int) : ViewModel() {

    val messages = repository.getMessagesForNode(nodeId).asLiveData()

    fun insert(message: Message) = viewModelScope.launch {
        repository.insert(message)
    }
}

class MessageViewModelFactory(private val repository: MessageRepository, private val nodeId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessageViewModel(repository, nodeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}