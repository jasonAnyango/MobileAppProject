package com.example.clubapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState() // role: "Admin", "Student", "VerificationSent", "ResetSent"
    data class Error(val message: String) : AuthState()
    object Unverified : AuthState()
}

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                val user = auth.currentUser

                // Check standard Firebase Auth email verification
                if (user != null && !user.isEmailVerified) {
                    auth.signOut()
                    _authState.value = AuthState.Unverified
                    return@launch
                }

                if (user != null) {
                    val doc = db.collection("users").document(user.uid).get().await()

                    val isSuspended = doc.getBoolean("isSuspended") ?: false

                    if (isSuspended) {
                        auth.signOut() // Kick them out immediately
                        _authState.value = AuthState.Error("Account Suspended. Contact Admin.")
                        return@launch
                    }
                    val roleString = doc.getString("role")

                    // Check if the string equals "ADMIN" (Case sensitive!)
                    val isAdmin = roleString == "ADMIN"

                    val role = if (isAdmin) "Admin" else "Student"
                    _authState.value = AuthState.Success(role)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun registerUser(email: String, pass: String, name: String, studentId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user

                if (user != null) {
                    // Update: Saved data now matches your database structure
                    val userMap = hashMapOf(
                        "uid" to user.uid,
                        "fullName" to name,
                        "studentId" to studentId,
                        "email" to email,
                        "role" to "STUDENT", // Defaulting new users to STUDENT role
                        "emailVerified" to false,
                        "clubId" to null
                    )
                    db.collection("users").document(user.uid).set(userMap).await()

                    // Send Verification & Sign Out
                    user.sendEmailVerification().await()
                    auth.signOut()

                    _authState.value = AuthState.Success("VerificationSent")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success("ResetSent")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }
}