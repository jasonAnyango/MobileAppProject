package com.example.clubapp.admin.data

import com.example.clubapp.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminRepository(private val db: FirebaseFirestore) {

    // ==========================================
    // 1. CLUB MANAGEMENT
    // ==========================================

    suspend fun getAllClubs(): List<Club> {
        return try {
            val snapshot = db.collection("clubs").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Club::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ==========================================
    // 2. APPROVAL SYSTEM (Requests)
    // ==========================================

    suspend fun getPendingApplications(): List<ClubRegistration> {
        return try {
            val snapshot = db.collection("club_requests")
                .whereEqualTo("status", "Pending")
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ClubRegistration::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun approveApplication(app: ClubRegistration) {
        // --- COMMENTED OUT AS REQUESTED ---
        /*
        try {
            // A. Mark request as Approved
            db.collection("club_requests").document(app.id)
                .update("status", "Approved").await()

            // B. Create Real Club
            val newClubRef = db.collection("clubs").document()
            val newClub = Club(
                id = newClubRef.id,
                name = app.clubName,
                description = app.description,
                mission = app.mission,
                leaderId = app.applicantId,
                memberIds = listOf(app.applicantId),
                isActive = true
            )
            newClubRef.set(newClub).await()

            // C. Promote User
            db.collection("users").document(app.applicantId).update(
                mapOf(
                    "role" to "Club Lead",
                    "isClubLeader" to true,
                    "clubsJoined" to FieldValue.arrayUnion(newClubRef.id)
                )
            ).await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        */
    }

    suspend fun rejectApplication(appId: String) {
        try {
            db.collection("club_requests").document(appId)
                .update("status", "Rejected").await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // 3. USER MANAGEMENT
    // ==========================================

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(uid = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.copy(uid = doc.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun suspendUser(userId: String) {
        try {
            db.collection("users").document(userId)
                .update("isSuspended", true).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun reactivateUser(userId: String) {
        try {
            db.collection("users").document(userId)
                .update("isSuspended", false).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // 4. ANNOUNCEMENTS
    // ==========================================

    suspend fun createGlobalAnnouncement(title: String, message: String, adminName: String) {
        try {
            val ref = db.collection("announcements").document()
            val announcement = Announcement(
                id = ref.id,
                title = title,
                message = message,
                date = System.currentTimeMillis().toString(),
                senderName = adminName,
                clubId = "GLOBAL"
            )
            ref.set(announcement).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // 5. EVENTS (Read-Only)
    // ==========================================

    suspend fun getAllEvents(): List<Event> {
        return try {
            val snapshot = db.collection("events").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}