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
            // Using copy() ensures we get the ID safely even if using val/var
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Club::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // --- ADDED THIS FUNCTION FOR THE UI BUTTON ---
    suspend fun toggleClubStatus(clubId: String, isActive: Boolean) {
        try {
            db.collection("clubs").document(clubId)
                .update("isActive", isActive).await()
        } catch (e: Exception) {
            e.printStackTrace()
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
                doc.toObject(ClubRegistration::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // --- LOGIC UNCOMMENTED AND ENABLED ---
    suspend fun approveApplication(app: ClubRegistration) {
        try {
            // A. Mark request as Approved
            db.collection("club_requests").document(app.id)
                .update("status", "Approved").await()

            // B. Create Real Club in 'clubs' collection
            val newClubRef = db.collection("clubs").document()
            val newClub = Club(
                id = newClubRef.id,
                name = app.clubName,
                description = app.description,
                mission = app.mission,
                leaderId = app.applicantId,
                memberIds = listOf(app.applicantId), // Leader is first member
                officers = mapOf(app.applicantId to "Chairperson"), // Default role
                isActive = true
            )
            newClubRef.set(newClub).await()

            // C. Promote User to Club Lead
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
                doc.toObject(User::class.java)?.apply { uid = doc.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.apply { uid = doc.id }
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
                doc.toObject(Event::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}