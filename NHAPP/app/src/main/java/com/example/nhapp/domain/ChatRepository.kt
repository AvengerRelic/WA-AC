package com.example.nhapp.domain

import com.example.nhapp.data.Chat
import com.example.nhapp.data.Message
import com.example.nhapp.data.SupabaseNetwork
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository {
    private val db = SupabaseNetwork.db
    private val realtime = SupabaseNetwork.realtime

    suspend fun getMyChats(): List<Chat> {
        return db["chats"]
            .select()
            .decodeList<Chat>()
    }

    suspend fun getMessages(chatId: String): List<Message> {
        return db["messages"]
            .select {
                filter {
                    eq("chat_id", chatId)
                }
            }
            .decodeList<Message>()
    }

    suspend fun sendMessage(chatId: String, content: String, senderId: String) {
        val payload = mapOf(
            "chat_id" to chatId,
            "sender_id" to senderId,
            "content" to content
        )
        db["messages"].insert(payload)
    }

    fun listenForNewMessages(chatId: String): Flow<Message> {
        val channel = realtime.channel("chat_$chatId")

        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "chat_id=eq.$chatId"
        }.map { action ->
            action.decodeRecord<Message>()
        }
    }
}
