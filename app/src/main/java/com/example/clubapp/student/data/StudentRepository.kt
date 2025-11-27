package com.example.clubapp.student.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StudentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    val currentUserId: String? get() = auth.currentUser?.uid

    // Collection references
    private fun studentDoc() = firestore.collection("students").document(currentUserId ?: "")
    private fun clubsCollection() = firestore.collection("clubs")
    private fun eventsCollection() = firestore.collection("events")
    private fun joinRequestsCollection() = firestore.collection("joinRequests")
    private fun clubApplicationsCollection() = firestore.collection("clubApplications")
    private fun eventRegistrationsCollection() = firestore.collection("eventRegistrations")

    // Get current student profile
    suspend fun getCurrentStudent(): Student? {
        return try {
            studentDoc().get().await().toObject<Student>()
        } catch (e: Exception) {
            null
        }
    }

    // Get all active clubs
    suspend fun getAllClubs(): List<Club> {
        return try {
            clubsCollection()
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .toObjects(Club::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get club by ID
    suspend fun getClubById(clubId: String): Club? {
        return try {
            clubsCollection().document(clubId).get().await().toObject<Club>()
        } catch (e: Exception) {
            null
        }
    }

    // Get student's clubs
    suspend fun getStudentClubs(): List<Club> {
        return try {
            clubsCollection()
                .whereArrayContains("members", currentUserId ?: "")
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .toObjects(Club::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get all events
    suspend fun getAllEvents(): List<Event> {
        return try {
            eventsCollection()
                .get()
                .await()
                .toObjects(Event::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get event by ID - ADD THIS MISSING METHOD
    suspend fun getEventById(eventId: String): Event? {
        return try {
            eventsCollection().document(eventId).get().await().toObject<Event>()
        } catch (e: Exception) {
            null
        }
    }

    // Get events for student's clubs
    suspend fun getEventsForStudentClubs(): List<Event> {
        val studentClubs = getStudentClubs()
        val clubIds = studentClubs.map { it.id }

        return try {
            eventsCollection()
                .whereIn("clubId", clubIds)
                .get()
                .await()
                .toObjects(Event::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get student's event registrations
    suspend fun getStudentEventRegistrations(): List<EventRegistration> {
        return try {
            eventRegistrationsCollection()
                .whereEqualTo("studentId", currentUserId)
                .get()
                .await()
                .toObjects(EventRegistration::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Register for event
    suspend fun registerForEvent(eventId: String): Boolean {
        return try {
            val registration = EventRegistration(
                id = "${currentUserId}_$eventId",
                eventId = eventId,
                studentId = currentUserId ?: "",
                registrationDate = com.google.firebase.Timestamp.now(),
                status = "Registered"
            )

            eventRegistrationsCollection()
                .document(registration.id)
                .set(registration)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Unregister from event
    suspend fun unregisterFromEvent(eventId: String): Boolean {
        return try {
            eventRegistrationsCollection()
                .document("${currentUserId}_$eventId")
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Send join request to club
    suspend fun sendJoinRequest(clubId: String, message: String? = null): Boolean {
        return try {
            val request = ClubJoinRequest(
                id = "${currentUserId}_$clubId",
                clubId = clubId,
                studentId = currentUserId ?: "",
                studentName = getCurrentStudent()?.name ?: "",
                requestDate = com.google.firebase.Timestamp.now(),
                status = "Pending",
                message = message
            )

            joinRequestsCollection()
                .document(request.id)
                .set(request)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get student's join requests
    suspend fun getStudentJoinRequests(): List<ClubJoinRequest> {
        return try {
            joinRequestsCollection()
                .whereEqualTo("studentId", currentUserId)
                .get()
                .await()
                .toObjects(ClubJoinRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Submit club creation request
    suspend fun submitClubCreationRequest(
        clubName: String,
        purpose: String,
        mission: String
    ): Boolean {
        return try {
            val application = ClubApplication(
                id = "${currentUserId}_${System.currentTimeMillis()}",
                clubName = clubName,
                applicantName = getCurrentStudent()?.name ?: "",
                applicantId = currentUserId ?: "",
                status = "Pending",
                purpose = purpose,
                mission = mission,
                applicationDate = com.google.firebase.Timestamp.now()
            )

            clubApplicationsCollection()
                .document(application.id)
                .set(application)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Check if student has pending request for club
    suspend fun hasPendingRequest(clubId: String): Boolean {
        return try {
            val request = joinRequestsCollection()
                .document("${currentUserId}_$clubId")
                .get()
                .await()
                .toObject<ClubJoinRequest>()

            request?.status == "Pending"
        } catch (e: Exception) {
            false
        }
    }

    // Check if student is registered for event
    suspend fun isRegisteredForEvent(eventId: String): Boolean {
        return try {
            eventRegistrationsCollection()
                .document("${currentUserId}_$eventId")
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }
}

// Firebase Data Models
data class Student(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val studentId: String = "",
    val profilePicture: String = "",
    val joinedClubs: List<String> = emptyList(),
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)

data class Club(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val memberCount: Int = 0,
    val isActive: Boolean = true,
    val admins: List<String> = emptyList(),
    val members: List<String> = emptyList(),
    val imageUrl: String = "",
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val location: String = "",
    val clubId: String = "",
    val clubName: String = "",
    val maxParticipants: Int = 0,
    val currentParticipants: Int = 0,
    val imageUrl: String = "",
    val isActive: Boolean = true
)

data class EventRegistration(
    val id: String = "",
    val eventId: String = "",
    val studentId: String = "",
    val registrationDate: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val status: String = "Registered" // Registered, Attended, Cancelled
)

data class ClubJoinRequest(
    val id: String = "",
    val clubName: String = "",
    val clubId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val requestDate: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val status: String = "Pending", // Pending, Approved, Rejected
    val message: String? = null
)

data class ClubApplication(
    val id: String = "",
    val clubName: String = "",
    val applicantName: String = "",
    val applicantId: String = "",
    val status: String = "Pending",
    val purpose: String = "",
    val mission: String = "",
    val applicationDate: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)
