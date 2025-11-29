package com.example.lolnk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lolnk.LolnkApplication
import com.example.lolnk.data.local.Message
import com.example.lolnk.viewmodel.ContactViewModel
import com.example.lolnk.viewmodel.ContactViewModelFactory
import com.example.lolnk.viewmodel.MessageViewModel
import com.example.lolnk.viewmodel.MessageViewModelFactory
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(application: LolnkApplication, contactNodeId: Int) {
    val contactViewModel: ContactViewModel = viewModel(
        factory = ContactViewModelFactory(application.contactRepository)
    )
    val messageViewModel: MessageViewModel = viewModel(
        factory = MessageViewModelFactory(application.messageRepository, contactNodeId)
    )

    val contact = runBlocking { contactViewModel.getContactByNodeId(contactNodeId) }
    val messages by messageViewModel.messages.observeAsState(initial = emptyList())

    var messageInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contact?.name ?: "Unknown Contact") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                IconButton(onClick = {
                    if (messageInput.isNotBlank()) {
                        // TODO: Replace with actual message type, keyIndex, tag, lat, lon
                        val newMessage = Message(
                            type = 0, // Placeholder
                            nodeId = contactNodeId,
                            timestamp = System.currentTimeMillis(),
                            latitude = 0.0, // Placeholder
                            longitude = 0.0, // Placeholder
                            message = messageInput,
                            keyIndex = 0, // Placeholder
                            tag = "", // Placeholder
                            isIncoming = false // This message is sent by current user
                        )
                        messageViewModel.insert(newMessage)
                        application.networkService.sendMessage(contactNodeId, messageInput) // Send via network
                        messageInput = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send Message")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            reverseLayout = true // To show latest messages at the bottom
        ) {
            items(messages.reversed()) { message -> // Reverse to show newest at bottom
                MessageBubble(message = message, isCurrentUser = !message.isIncoming)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message.message,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                .padding(8.dp)
        )
    }
}
