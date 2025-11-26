package com.example.clubapp.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubapp.data.model.Resource
import com.example.clubapp.data.model.User
import com.example.clubapp.data.repository.AuthRepository
import com.example.clubapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isEmailVerified: Boolean = false,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Listen for authentication state changes
        checkAuthStatus()

        // Set up a listener for auth state changes
        viewModelScope.launch {
            // This will re-check auth status whenever the state might have changed
            // You might want to implement a proper Firebase Auth State Listener here
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                when (val result = userRepository.getUserById(currentUser.uid)) {
                    is Resource.Success -> {
                        _authState.value = AuthState(
                            isLoading = false,
                            isAuthenticated = true,
                            user = result.data,
                            isEmailVerified = currentUser.isEmailVerified
                        )
                    }
                    is Resource.Error -> {
                        _authState.value = AuthState(
                            isLoading = false,
                            error = result.message,
                            isAuthenticated = false,
                            user = null
                        )
                    }
                    else -> {
                        _authState.value = AuthState(isLoading = false)
                    }
                }
            } else {
                // No user is signed in
                _authState.value = AuthState(
                    isLoading = false,
                    isAuthenticated = false,
                    user = null,
                    isEmailVerified = false
                )
            }
        }
    }

    fun signUp(email: String, password: String, fullName: String, studentId: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signUp(email, password, fullName, studentId)) {
                is Resource.Success -> {
                    _authState.value = AuthState(
                        user = result.data,
                        isAuthenticated = true,
                        isEmailVerified = false,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _authState.value = AuthState(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _authState.value = _authState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signIn(email, password)) {
                is Resource.Success -> {
                    _authState.value = AuthState(
                        user = result.data,
                        isAuthenticated = true,
                        isEmailVerified = authRepository.currentUser?.isEmailVerified == true,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _authState.value = AuthState(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _authState.value = _authState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            authRepository.signOut()
            // Force reset the auth state after sign out
            _authState.value = AuthState(
                isLoading = false,
                isAuthenticated = false,
                user = null,
                isEmailVerified = false
            )
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            val isVerified = authRepository.reloadUser()
            _authState.value = _authState.value.copy(isEmailVerified = isVerified)
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Resource.Success -> {
                    onResult(true, null)
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    // Add this method to force refresh auth state
    fun refreshAuthState() {
        checkAuthStatus()
    }
}