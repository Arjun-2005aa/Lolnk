package com.example.lolnk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
    var rawLog by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }

    application.networkService.setMessageListener {
        rawLog += "Received: ${it.toHexString()}\n"
    }

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
                        val newMessage = Message(
                            type = 0, // Placeholder
                            nodeId = contactNodeId,
                            timestamp = System.currentTimeMillis(),
                            latitude = 0.0,
                            longitude = 0.0,
                            message = messageInput,
                            keyIndex = 0,
                            tag = "",
                            isIncoming = false
                        )
                        messageViewModel.insert(newMessage)
                        val sentData = application.networkService.sendMessage(contactNodeId, messageInput)
                        rawLog += "Sent: ${sentData.toHexString()}\n"
                        messageInput = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send Message")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageBubble(message = message, isCurrentUser = !message.isIncoming)
                }
            }
            Text(
                text = rawLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            )
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

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }