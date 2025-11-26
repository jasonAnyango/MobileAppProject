package com.example.clubapp.data.repository

import android.net.Uri
import com.example.clubapp.data.model.Club
import com.example.clubapp.data.model.ClubStatus
import com.example.clubapp.data.model.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ClubRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    fun getAllClubs(): Flow<Resource<List<Club>>> = callbackFlow {
        val listener = firestore.collection("clubs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch clubs"))
                    return@addSnapshotListener
                }

                val clubs = snapshot?.documents?.mapNotNull { it.toObject(Club::class.java) } ?: emptyList()
                trySend(Resource.Success(clubs))
            }

        awaitClose { listener.remove() }
    }

    fun getApprovedClubs(): Flow<Resource<List<Club>>> = callbackFlow {
        val listener = firestore.collection("clubs")
            .whereEqualTo("status", ClubStatus.APPROVED.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch clubs"))
                    return@addSnapshotListener
                }

                val clubs = snapshot?.documents?.mapNotNull { it.toObject(Club::class.java) } ?: emptyList()
                trySend(Resource.Success(clubs))
            }

        awaitClose { listener.remove() }
    }

    fun getPendingClubs(): Flow<Resource<List<Club>>> = callbackFlow {
        val listener = firestore.collection("clubs")
            .whereEqualTo("status", ClubStatus.PENDING.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch pending clubs"))
                    return@addSnapshotListener
                }

                val clubs = snapshot?.documents?.mapNotNull { it.toObject(Club::class.java) } ?: emptyList()
                trySend(Resource.Success(clubs))
            }

        awaitClose { listener.remove() }
    }

    suspend fun getClubById(clubId: String): Resource<Club> {
        return try {
            val doc = firestore.collection("clubs").document(clubId).get().await()
            val club = doc.toObject(Club::class.java) ?: throw Exception("Club not found")
            Resource.Success(club)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch club")
        }
    }

    suspend fun createClub(club: Club): Resource<String> {
        return try {
            val docRef = firestore.collection("clubs").document()
            val newClub = club.copy(id = docRef.id)
            docRef.set(newClub).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create club")
        }
    }

    suspend fun updateClub(club: Club): Resource<Unit> {
        return try {
            firestore.collection("clubs").document(club.id).set(club).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update club")
        }
    }

    suspend fun approveClub(clubId: String): Resource<Unit> {
        return try {
            firestore.collection("clubs").document(clubId).update(
                mapOf(
                    "status" to ClubStatus.APPROVED.name,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to approve club")
        }
    }

    suspend fun rejectClub(clubId: String): Resource<Unit> {
        return try {
            firestore.collection("clubs").document(clubId).update(
                mapOf(
                    "status" to ClubStatus.REJECTED.name,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to reject club")
        }
    }

    suspend fun deleteClub(clubId: String): Resource<Unit> {
        return try {
            firestore.collection("clubs").document(clubId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete club")
        }
    }

    suspend fun uploadClubImage(clubId: String, imageUri: Uri): Resource<String> {
        return try {
            val ref = storage.reference.child("clubs/$clubId/${UUID.randomUUID()}.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Resource.Success(url)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload image")
        }
    }

    fun getClubsByLeader(leaderId: String): Flow<Resource<List<Club>>> = callbackFlow {
        val listener = firestore.collection("clubs")
            .whereEqualTo("leaderId", leaderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch clubs"))
                    return@addSnapshotListener
                }

                val clubs = snapshot?.documents?.mapNotNull { it.toObject(Club::class.java) } ?: emptyList()
                trySend(Resource.Success(clubs))
            }

        awaitClose { listener.remove() }
    }
}