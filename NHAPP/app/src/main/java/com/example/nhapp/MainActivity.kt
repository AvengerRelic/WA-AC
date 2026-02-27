package com.example.nhapp

import android.content.Intent
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
import com.example.nhapp.data.SupabaseNetwork
import com.example.nhapp.ui.*
import com.example.nhapp.ui.theme.NHAppTheme
import io.github.jan.supabase.gotrue.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        intent?.let { handleSupabaseDeeplink(it) }

        setContent {
            NHAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NHAppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSupabaseDeeplink(intent)
    }

    private fun handleSupabaseDeeplink(intent: Intent) {
        SupabaseNetwork.client.handleDeeplinks(intent)
    }
}

@Composable
fun NHAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // BYPASS: Forcing the app to start at chat_list and ignore authentication for now
    NavHost(
        navController = navController,
        startDestination = "chat_list"
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
