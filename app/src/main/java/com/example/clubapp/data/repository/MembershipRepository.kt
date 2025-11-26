package com.example.clubapp.data.repository

import com.example.clubapp.data.model.MembershipRequest
import com.example.clubapp.data.model.MembershipStatus
import com.example.clubapp.data.model.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MembershipRepository(
    private val firestore: FirebaseFirestore
) {
    fun getRequestsByClub(clubId: String): Flow<Resource<List<MembershipRequest>>> = callbackFlow {
        val listener = firestore.collection("membershipRequests")
            .whereEqualTo("clubId", clubId)
            .whereEqualTo("status", MembershipStatus.PENDING.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch requests"))
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull {
                    it.toObject(MembershipRequest::class.java)
                } ?: emptyList()
                trySend(Resource.Success(requests))
            }

        awaitClose { listener.remove() }
    }

    fun getRequestsByStudent(studentId: String): Flow<Resource<List<MembershipRequest>>> = callbackFlow {
        val listener = firestore.collection("membershipRequests")
            .whereEqualTo("studentId", studentId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch requests"))
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull {
                    it.toObject(MembershipRequest::class.java)
                } ?: emptyList()
                trySend(Resource.Success(requests))
            }

        awaitClose { listener.remove() }
    }

    suspend fun createRequest(request: MembershipRequest): Resource<String> {
        return try {
            // Check if request already exists
            val existing = firestore.collection("membershipRequests")
                .whereEqualTo("clubId", request.clubId)
                .whereEqualTo("studentId", request.studentId)
                .whereEqualTo("status", MembershipStatus.PENDING.name)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Resource.Error("You already have a pending request for this club")
            }

            val docRef = firestore.collection("membershipRequests").document()
            val newRequest = request.copy(id = docRef.id)
            docRef.set(newRequest).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create request")
        }
    }

    suspend fun approveRequest(requestId: String, clubId: String, studentId: String, leaderId: String): Resource<Unit> {
        return try {
            firestore.runBatch { batch ->
                // Update request status
                val requestRef = firestore.collection("membershipRequests").document(requestId)
                batch.update(requestRef, mapOf(
                    "status" to MembershipStatus.APPROVED.name,
                    "respondedAt" to Timestamp.now(),
                    "respondedBy" to leaderId
                ))

                // Add student to club members
                val clubRef = firestore.collection("clubs").document(clubId)
                batch.update(clubRef, mapOf(
                    "memberIds" to FieldValue.arrayUnion(studentId),
                    "memberCount" to FieldValue.increment(1)
                ))
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to approve request")
        }
    }

    suspend fun rejectRequest(requestId: String, leaderId: String): Resource<Unit> {
        return try {
            firestore.collection("membershipRequests").document(requestId)
                .update(mapOf(
                    "status" to MembershipStatus.REJECTED.name,
                    "respondedAt" to Timestamp.now(),
                    "respondedBy" to leaderId
                ))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to reject request")
        }
    }

    suspend fun removeMember(clubId: String, memberId: String): Resource<Unit> {
        return try {
            firestore.collection("clubs").document(clubId)
                .update(mapOf(
                    "memberIds" to FieldValue.arrayRemove(memberId),
                    "memberCount" to FieldValue.increment(-1)
                ))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove member")
        }
    }
}