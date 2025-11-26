package com.example.clubapp.data.repository

import android.net.Uri
import com.example.clubapp.data.model.Resource
import com.example.clubapp.data.model.User
import com.example.clubapp.data.model.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun getUserById(userId: String): Resource<User> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java) ?: throw Exception("User not found")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user")
        }
    }

    fun getAllUsers(): Flow<Resource<List<User>>> = callbackFlow {
        val listener = firestore.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch users"))
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                trySend(Resource.Success(users))
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateUser(user: User): Resource<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "fullName" to user.fullName,
                "phoneNumber" to user.phoneNumber,
                "profileImageUrl" to user.profileImageUrl,
                "updatedAt" to Timestamp.now()
            )
            firestore.collection("users").document(user.id).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user")
        }
    }

    suspend fun promoteToClubLeader(userId: String, clubId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId).update(
                mapOf(
                    "role" to UserRole.CLUB_LEADER,
                    "clubId" to clubId,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to promote user")
        }
    }

    suspend fun promoteToAdmin(userId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId).update(
                mapOf(
                    "role" to UserRole.ADMIN,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to promote to admin")
        }
    }

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Resource<String> {
        return try {
            val ref = storage.reference.child("profiles/$userId/${UUID.randomUUID()}.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Resource.Success(url)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload image")
        }
    }

    suspend fun deleteUser(userId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete user")
        }
    }

    // In UserRepository.kt
    suspend fun updateUserClub(userId: String, clubId: String, onResult: (Boolean, String?) -> Unit) {
        return try {
            firestore.collection("users").document(userId).update("clubId", clubId).await()
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }

    // And make sure you have a MembershipRepository with:
    suspend fun addMember(clubId: String, userId: String, onResult: (Boolean, String?) -> Unit) {
        return try {
            // Add user to club's members list
            firestore.collection("clubs").document(clubId).update(
                "memberIds", FieldValue.arrayUnion(userId)
            ).await()
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }
}