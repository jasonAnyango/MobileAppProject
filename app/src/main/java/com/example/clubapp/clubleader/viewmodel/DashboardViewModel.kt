package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.model.Club
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.example.clubapp.clubleader.data.repository.ClubService // Assuming this import path
import kotlinx.coroutines.runBlocking // Import runBlocking for factory

// --- UiState is assumed to be defined elsewhere, e.g., ClubDashboardUiState ---
data class ClubDashboardUiState(
    val clubName: String = "",
    val memberCount: Int = 0,
    val eventCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)


// ------------------- VIEW MODEL -------------------

class ClubDashboardViewModel(
    private val clubId: String, // Dynamic Club ID
    private val clubName: String, // Dynamic Club Name
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClubDashboardUiState(clubName = clubName))
    val uiState: StateFlow<ClubDashboardUiState> = _uiState

    init {
        // Check if a club was found for the leader
        if (clubId.isNotEmpty()) {
            loadDashboardData()
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Club Leader data not found. Ensure your account is linked to a club.",
                clubName = "Unlinked User"
            )
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // STEP 1: Fetch Club Details using the clubId
                val club = getClubDetails(clubId)

                // STEP 2: Fetch Upcoming Event Count using the Club Name
                val eventCount = getUpcomingEventsCount(club.name)

                _uiState.value = _uiState.value.copy(
                    clubName = club.name, // Ensure this reflects the latest name from Firestore
                    memberCount = club.memberIds.size,
                    eventCount = eventCount,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalStateException -> e.message
                    else -> "Failed to load dashboard: ${e.message}"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    private suspend fun getClubDetails(id: String): Club {
        val clubDoc = db.collection("clubs").document(id).get().await()

        val club = clubDoc.toObject(Club::class.java)
            ?: throw IllegalStateException("Club document not found for ID: $id. Check if the ID exists in the 'clubs' collection.")

        return club
    }

    private suspend fun getUpcomingEventsCount(clubName: String): Int {
        val eventsQuery = db.collection("events")
            .whereEqualTo("clubName", clubName)
            .get()
            .await()

        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        var upcomingCount = 0

        for (doc in eventsQuery.documents) {
            val eventDateString = doc.getString("date")
            if (eventDateString != null) {
                try {
                    val eventDate = LocalDate.parse(eventDateString, dateFormatter)
                    if (eventDate.isAfter(today) || eventDate.isEqual(today)) {
                        upcomingCount++
                    }
                } catch (e: DateTimeParseException) {
                    println("Date format error for event ${doc.id}: $eventDateString")
                }
            }
        }
        return upcomingCount
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create ClubDashboardViewModel. Fetches the club ID and name dynamically.
 */
class ClubDashboardViewModelFactory(
    private val db: FirebaseFirestore // 💡 Only need the database instance now
) : ViewModelProvider.Factory {

    private var dynamicClubId: String? = null
    private var dynamicClubName: String? = null

    init {
        val clubService = ClubService(db)
        // runBlocking is required here for synchronous factory creation
        runBlocking {
            try {
                val clubData = clubService.getClubByLeaderUid()
                dynamicClubId = clubData?.second
                dynamicClubName = clubData?.first?.name
            } catch (e: Exception) {
                println("ERROR: ClubDashboardViewModelFactory failed to fetch club details for leader: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClubDashboardViewModel::class.java)) {
            // Pass the dynamically fetched values
            val clubIdToUse = dynamicClubId ?: ""
            val clubNameToUse = dynamicClubName ?: "Loading Club..." // Default placeholder

            return ClubDashboardViewModel(clubIdToUse, clubNameToUse, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}