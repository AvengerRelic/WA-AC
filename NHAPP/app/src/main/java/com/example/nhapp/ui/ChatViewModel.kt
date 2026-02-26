package com.example.nhapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhapp.data.Chat
import com.example.nhapp.data.Message
import com.example.nhapp.domain.AuthRepository
import com.example.nhapp.domain.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val authRepository = AuthRepository()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun fetchChats() {
        viewModelScope.launch {
            try {
                val myChats = chatRepository.getMyChats()
                _chats.value = myChats
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAndListenMessages(chatId: String) {
        viewModelScope.launch {
            try {
                // Fetch historical messages
                val history = chatRepository.getMessages(chatId).sortedBy { it.createdAt }
                _messages.value = history

                // Subscribe to real-time inserts
                chatRepository.listenForNewMessages(chatId).collect { newMessage ->
                    _messages.value = _messages.value + newMessage
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(chatId: String, content: String) {
        viewModelScope.launch {
            try {
                val senderId = authRepository.getCurrentUserId() ?: return@launch
                chatRepository.sendMessage(chatId, content, senderId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
