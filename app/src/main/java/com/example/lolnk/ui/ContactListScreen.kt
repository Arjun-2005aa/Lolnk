package com.example.lolnk.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lolnk.LolnkApplication
import com.example.lolnk.data.local.Contact
import com.example.lolnk.viewmodel.ContactViewModel
import com.example.lolnk.viewmodel.ContactViewModelFactory

@Composable
fun ContactListScreen(application: LolnkApplication, navController: NavController) {
    val contactViewModel: ContactViewModel = viewModel(
        factory = ContactViewModelFactory(application.contactRepository)
    )
    val contacts by contactViewModel.allContacts.observeAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(contacts) { contact ->
                ContactRow(contact) {
                    navController.navigate("chat/${contact.nodeId}")
                }
            }
        }
    }

    if (showDialog) {
        AddContactDialog(
            onAddContact = {
                val name = it.first
                val nodeId = it.second.toIntOrNull() ?: 0
                contactViewModel.insert(Contact(name = name, nodeId = nodeId))
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ContactRow(contact: Contact, onClick: () -> Unit) {
    Text(text = contact.name, modifier = Modifier.clickable(onClick = onClick).padding(16.dp))
}

@Composable
fun AddContactDialog(onAddContact: (Pair<String, String>) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var nodeId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Contact") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = nodeId, onValueChange = { nodeId = it }, label = { Text("Node ID") })
            }
        },
        confirmButton = {
            Button(onClick = { onAddContact(Pair(name, nodeId)) }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}