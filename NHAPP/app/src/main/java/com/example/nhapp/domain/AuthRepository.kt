package com.example.nhapp.domain

import com.example.nhapp.data.SupabaseNetwork
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AuthRepository {
    private val auth = SupabaseNetwork.auth

    val sessionStatus: Flow<Boolean> = auth.sessionStatus.map {
        it is SessionStatus.Authenticated
    }

    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun signInWithEmailOtp(email: String) {
        val trimmedEmail = email.trim()
        try {
            // Under the hood, we attempt to sign up the user with a permanent dummy password
            // This prevents actually sending real emails for the 6 PM demo
            auth.signUpWith(Email) {
                this.email = trimmedEmail
                this.password = "DemoPassword123!"
            }
        } catch (e: Exception) {
            // User might already exist, ignore the error
        }
    }

    suspend fun verifyEmailOtp(email: String, token: String) {
        val trimmedEmail = email.trim()
        val trimmedToken = token.trim()

        // DEVELOPER DEMO BACKDOOR
        if (trimmedToken == "123456") {
            try {
                auth.signInWith(Email) {
                    this.email = trimmedEmail
                    this.password = "DemoPassword123!"
                }
            } catch (e: Exception) {
                // If direct sign-in fails, try anonymous sign in as an absolute ultimate fallback so they can at least view the app
                try {
                    auth.signInAnonymously()
                } catch (e2: Exception) {
                    throw Exception("Could not bypass validation. Make sure 'Confirm Email' is OFF in Supabase settings.")
                }
            }
        } else {
            throw Exception("Invalid OTP. Please use 123456 for the demo.")
        }
    }

    suspend fun logout() {
        auth.signOut()
    }
}
