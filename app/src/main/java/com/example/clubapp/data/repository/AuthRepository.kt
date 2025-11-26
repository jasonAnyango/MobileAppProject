package com.example.clubapp.data.repository

import com.example.clubapp.data.model.Resource
import com.example.clubapp.data.model.User
import com.example.clubapp.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signUp(email: String, password: String, fullName: String, studentId: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")

            // Send email verification
            firebaseUser.sendEmailVerification().await()

            // Determine if this is the first user (will be admin)
            val usersCount = firestore.collection("users").get().await().size()
            val role = if (usersCount == 0) UserRole.ADMIN else UserRole.STUDENT

            val user = User(
                id = firebaseUser.uid,
                email = email,
                fullName = fullName,
                studentId = studentId,
                role = role,
                isEmailVerified = false
            )

            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Sign in failed")

            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send reset email")
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun reloadUser(): Boolean {
        return try {
            currentUser?.reload()?.await()
            currentUser?.isEmailVerified == true
        } catch (e: Exception) {
            false
        }
    }
}