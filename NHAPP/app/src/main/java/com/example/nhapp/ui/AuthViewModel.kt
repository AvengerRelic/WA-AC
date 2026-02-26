package com.example.nhapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhapp.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { isAuthenticated ->
                _authState.value = if (isAuthenticated) AuthState.Authenticated else AuthState.Unauthenticated
            }
        }
    }

    fun requestOtp(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.signInWithEmailOtp(email)
                _authState.value = AuthState.OtpSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send OTP")
            }
        }
    }

    fun verifyOtp(email: String, token: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.verifyEmailOtp(email, token)
                // The realtime session flow will automatically push state to Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to verify OTP")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
