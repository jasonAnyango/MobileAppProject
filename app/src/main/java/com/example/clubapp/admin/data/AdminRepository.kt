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
                doc.toObject(Club::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

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

    suspend fun approveApplication(app: ClubRegistration) {
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
                officers = mapOf(app.applicantId to "Chairperson"),
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

    suspend fun getUpcomingEvents(): List<Event> {
        return try {
            val allEvents = getAllEvents()

            // Sort events using the 'date' string (assuming YYYY-MM-DD format for correct sorting).
            // Then take the top 3 for the dashboard preview.
            val sortedEvents = allEvents.sortedBy { it.date }

            return sortedEvents.take(3)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ==========================================
    // 6. REPORTING & ANALYTICS (NEW)
    // ==========================================

    // Data class to hold the report summary
    data class DashboardStats(
        val totalStudents: Int,
        val totalClubs: Int,
        val totalEvents: Int,
        val activeClubs: Int,
        val inactiveClubs: Int,
        val topClubs: List<Club>
    )

    // Calculate stats on the fly
    suspend fun getDashboardStats(): DashboardStats {
        return try {
            val users = getAllUsers()
            val clubs = getAllClubs()
            val events = getAllEvents()

            // Sort clubs by number of members to find the "Top 5"
            val sortedClubs = clubs.sortedByDescending { it.memberIds.size }.take(5)

            DashboardStats(
                totalStudents = users.size,
                totalClubs = clubs.size,
                totalEvents = events.size,
                activeClubs = clubs.count { it.isActive },
                inactiveClubs = clubs.count { !it.isActive },
                topClubs = sortedClubs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Return zeroed out stats if it fails
            DashboardStats(0, 0, 0, 0, 0, emptyList())
        }
    }

    // Generate a simple CSV string for export
    suspend fun generateCsvExport(): String {
        return try {
            val clubs = getAllClubs()
            val sb = StringBuilder()

            // CSV Header
            sb.append("Club Name,Leader ID,Members Count,Status,Mission\n")

            // CSV Rows
            clubs.forEach { club ->
                // Escape commas in text fields to prevent breaking the CSV format
                val safeName = club.name.replace(",", " ")
                val safeMission = club.mission.replace(",", " ")
                val status = if(club.isActive) "Active" else "Inactive"

                sb.append("$safeName,${club.leaderId},${club.memberIds.size},$status,$safeMission\n")
            }
            sb.toString()
        } catch (e: Exception) {
            "Error generating report"
        }
    }
}