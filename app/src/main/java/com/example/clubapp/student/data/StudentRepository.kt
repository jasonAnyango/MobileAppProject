package com.example.clubapp.student.data

import com.example.clubapp.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudentRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // FIX: Use a getter so it always checks the CURRENT logged-in user
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // ==========================================
    // 1. STUDENT PROFILE
    // ==========================================
    suspend fun getMyUserProfile(): User? {
        if (currentUserId.isEmpty()) return null
        return try {
            val doc = db.collection("users").document(currentUserId).get().await()
            doc.toObject(User::class.java)?.apply { uid = doc.id }
        } catch (e: Exception) {
            null
        }
    }

    // ==========================================
    // 2. CLUBS
    // ==========================================

    suspend fun getActiveClubs(): List<Club> {
        return try {
            val snapshot = db.collection("clubs")
                .whereEqualTo("isActive", true)
                .get().await()
            val list = snapshot.toObjects(Club::class.java)
            list.mapIndexed { index, club ->
                club.id = snapshot.documents[index].id
                club
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun joinClub(clubId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            db.collection("clubs").document(clubId)
                .update("memberIds", FieldValue.arrayUnion(currentUserId))

            db.collection("users").document(currentUserId)
                .update("clubsJoined", FieldValue.arrayUnion(clubId))
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun leaveClub(clubId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            db.collection("clubs").document(clubId)
                .update("memberIds", FieldValue.arrayRemove(currentUserId))

            db.collection("users").document(currentUserId)
                .update("clubsJoined", FieldValue.arrayRemove(clubId))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==========================================
    // 3. EVENTS (Updated for "Pending" Logic)
    // ==========================================

    suspend fun getAllEvents(): List<Event> {
        return try {
            val snapshot = db.collection("events").get().await()
            val list = snapshot.toObjects(Event::class.java)
            list.mapIndexed { index, event ->
                event.id = snapshot.documents[index].id
                event
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // NEW: Check my status for a specific event
    suspend fun getEventRegistrationStatus(eventId: String): String {
        if (currentUserId.isEmpty()) return "None"
        return try {
            val regId = "${currentUserId}_$eventId"
            val doc = db.collection("event_registrations").document(regId).get().await()
            if (doc.exists()) {
                doc.getString("status") ?: "None"
            } else {
                "None"
            }
        } catch (e: Exception) {
            "None"
        }
    }

    suspend fun registerForEvent(eventId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            val regId = "${currentUserId}_$eventId"
            val registration = EventRegistration(
                id = regId,
                eventId = eventId,
                studentId = currentUserId,
                status = "Pending", // Default is now Pending
                timestamp = FieldValue.serverTimestamp()
            )

            db.collection("event_registrations").document(regId).set(registration).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cancelEventRegistration(eventId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            val regId = "${currentUserId}_$eventId"
            db.collection("event_registrations").document(regId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==========================================
    // 4. CLUB PROPOSALS & MANAGEMENT
    // ==========================================

    suspend fun submitClubProposal(request: ClubRegistration): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            val ref = db.collection("club_requests").document()
            request.id = ref.id
            request.applicantId = currentUserId
            request.status = "Pending"
            ref.set(request).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getClubsManagedByMe(): List<Club> {
        if (currentUserId.isEmpty()) return emptyList()
        return try {
            val snapshot = db.collection("clubs")
                .whereEqualTo("leaderId", currentUserId)
                .whereEqualTo("isActive", true)
                .get().await()
            val list = snapshot.toObjects(Club::class.java)
            list.mapIndexed { index, club ->
                club.id = snapshot.documents[index].id
                club
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyClubProposals(): List<ClubRegistration> {
        if (currentUserId.isEmpty()) return emptyList()
        return try {
            val snapshot = db.collection("club_requests")
                .whereEqualTo("applicantId", currentUserId)
                .get().await()
            val list = snapshot.toObjects(ClubRegistration::class.java)
            list.mapIndexed { index, item ->
                item.id = snapshot.documents[index].id
                item
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}