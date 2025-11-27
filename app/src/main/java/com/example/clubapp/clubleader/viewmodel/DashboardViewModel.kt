package com.example.clubapp.clubleader.viewmodel // Adjust package as needed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.model.Club // Assuming your Club model is here
import com.example.clubapp.model.Event // Assuming your Event model is here
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ------------------- VIEW MODEL -------------------

class ClubDashboardViewModel(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClubDashboardUiState())
    val uiState: StateFlow<ClubDashboardUiState> = _uiState

    init {
        loadDashboardData()
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
                    clubName = club.name,
                    memberCount = club.memberIds.size,
                    eventCount = eventCount,
                    isLoading = false
                )

            } catch (e: Exception) {
                // Check for specific error types (e.g., Club not found)
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
        // Check if document exists and can be mapped
            ?: throw IllegalStateException("Club document not found for ID: $id. Check if the ID exists in the 'clubs' collection.")

        return club
    }

    private suspend fun getUpcomingEventsCount(clubName: String): Int {
        val eventsQuery = db.collection("events")
            .whereEqualTo("clubName", clubName)
            .get()
            .await()

        val today = LocalDate.now()
        // Define the expected date format
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        var upcomingCount = 0

        for (doc in eventsQuery.documents) {
            val eventDateString = doc.getString("date")
            if (eventDateString != null) {
                try {
                    val eventDate = LocalDate.parse(eventDateString, dateFormatter)
                    // Check if event date is today or in the future
                    if (eventDate.isAfter(today) || eventDate.isEqual(today)) {
                        upcomingCount++
                    }
                } catch (e: DateTimeParseException) {
                    // Log date parsing errors
                    println("Date format error for event ${doc.id}: $eventDateString")
                }
            }
        }
        return upcomingCount
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create ClubDashboardViewModel with dependencies for testing.
 */
class ClubDashboardViewModelFactory(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClubDashboardViewModel::class.java)) {
            return ClubDashboardViewModel(clubId, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}