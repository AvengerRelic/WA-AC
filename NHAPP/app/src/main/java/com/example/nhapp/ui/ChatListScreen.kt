package com.example.nhapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nhapp.data.Chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onChatClick: (String) -> Unit
) {
    val chats by viewModel.chats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NHAPP") },
                actions = {
                    TextButton(onClick = { authViewModel.logout() }) {
                        Text("Logout", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Open New Chat */ }) {
                Icon(Icons.Filled.Add, contentDescription = "New Chat")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(chats) { chat ->
                ChatItem(chat = chat, onClick = { onChatClick(chat.id) })
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Placeholder for Avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {}
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Chat Details", style = MaterialTheme.typography.titleMedium)
                Text(chat.createdAt, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
