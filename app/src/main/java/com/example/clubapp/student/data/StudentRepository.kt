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
    private val currentUserId = auth.currentUser?.uid ?: ""

    // ==========================================
    // 1. STUDENT PROFILE
    // ==========================================
    suspend fun getCurrentUser(): User? {
        if (currentUserId.isEmpty()) return null
        return try {
            // SYNC FIX: We read from "users", not "students"
            val doc = db.collection("users").document(currentUserId).get().await()
            doc.toObject(User::class.java)?.apply { uid = doc.id }
        } catch (e: Exception) {
            null
        }
    }

    // ==========================================
    // 2. CLUBS
    // ==========================================

    // Get all ACTIVE clubs
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

    // Join a Club (Directly updates arrays)
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
    // 3. CREATE CLUB REQUEST
    // ==========================================

    suspend fun submitClubProposal(request: ClubRegistration): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            // SYNC FIX: We write to "club_requests" so Admin can see it
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

    // ==========================================
    // 4. EVENTS
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

    suspend fun registerForEvent(eventId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            val regId = "${currentUserId}_$eventId"
            val registration = EventRegistration(
                id = regId,
                eventId = eventId,
                studentId = currentUserId,
                timestamp = FieldValue.serverTimestamp()
            )

            db.collection("event_registrations").document(regId).set(registration).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==========================================
    // 5. HELPER: Get my Profile
    // ==========================================
    suspend fun getMyUserProfile(): User? {
        return getCurrentUser()
    }

    // ... inside StudentRepository class ...

    // ==========================================
    // 6. LEADER HELPER: Get clubs I manage
    // ==========================================
    suspend fun getClubsManagedByMe(): List<Club> {
        if (currentUserId.isEmpty()) return emptyList()
        return try {
            // Query: Find clubs where "leaderId" matches the current user
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
            e.printStackTrace()
            emptyList()
        }
    }

    // Inside StudentRepository class...

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