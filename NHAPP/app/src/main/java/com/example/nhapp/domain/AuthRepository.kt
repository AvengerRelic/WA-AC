package com.example.nhapp.domain

import com.example.nhapp.data.SupabaseNetwork
import io.github.jan.supabase.gotrue.otp.OtpType
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository {
    private val auth = SupabaseNetwork.auth

    val sessionStatus: Flow<Boolean> = auth.sessionStatus.map {
        it is io.github.jan.supabase.gotrue.SessionStatus.Authenticated
    }

    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun signInWithEmailOtp(email: String) {
        auth.signInWith(OTP) {
            this.email = email
            // You can specify type = OtpType.Email.MAGIC_LINK if you prefer simple links instead of typing a code
        }
    }

    suspend fun verifyEmailOtp(email: String, token: String) {
        auth.verifyEmailOtp(
            type = OtpType.Email.MAGIC_LINK,
            email = email,
            token = token
        )
    }

    suspend fun logout() {
        auth.signOut()
    }
}
