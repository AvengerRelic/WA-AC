package com.example.nhapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nhapp.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NHAppNavigation()
                }
            }
        }
    }
}

@Composable
fun NHAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) "chat_list" else "auth"
    ) {
        composable("auth") {
            AuthScreen(viewModel = authViewModel)
        }
        composable("chat_list") {
            ChatListScreen(
                authViewModel = authViewModel,
                onChatClick = { chatId ->
                    navController.navigate("chat_room/$chatId")
                }
            )
        }
        composable("chat_room/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatRoomScreen(
                chatId = chatId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
