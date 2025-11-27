package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.clubleader.state.EventsUiState
import com.example.clubapp.model.Club
import com.example.clubapp.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.example.clubapp.clubleader.data.repository.ClubService
import kotlinx.coroutines.runBlocking
import com.google.firebase.firestore.Query // Added for future ordering/filtering

// ------------------- VIEW MODEL -------------------

class EventsViewModel(
    private val clubId: String,
    private val clubName: String, // 💡 NEW: Receive clubName from the Factory
    private val db: FirebaseFirestore
) : ViewModel() {

    // Initialize UiState with the received clubName
    private val _uiState = MutableStateFlow(EventsUiState(clubName = clubName))
    val uiState: StateFlow<EventsUiState> = _uiState

    init {
        // Only load data if a valid club ID was found by the factory
        if (clubId.isNotEmpty()) {
            loadEventsData()
        } else {
            // Handle case where leader is logged in but not linked to a club
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Club Leader data not found. Ensure your account is linked to a club.",
                clubName = "Unlinked User" // Set a default name if fetch failed
            )
        }
    }

    private fun loadEventsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 1. Fetch all events for that club using the clubName (as per your current logic)
                val allEvents = getEventsByClubName(clubName)

                // 2. Filter the events
                val today = LocalDate.now()
                // Use ISO format for parsing if possible, or your stored format "yyyy-MM-dd"
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val upcoming = mutableListOf<Event>()
                val past = mutableListOf<Event>()

                allEvents.forEach { event ->
                    try {
                        val eventDate = LocalDate.parse(event.date, dateFormatter)
                        // If the event date is today or later
                        if (eventDate.isAfter(today) || eventDate.isEqual(today)) {
                            upcoming.add(event)
                        } else {
                            past.add(event)
                        }
                    } catch (e: DateTimeParseException) {
                        println("Date format error for event ${event.id}: ${event.date}")
                    }
                }

                _uiState.value = _uiState.value.copy(
                    // clubName is already set
                    clubEvents = allEvents,
                    upcomingEvents = upcoming.sortedBy { it.date },
                    pastEvents = past.sortedByDescending { it.date },
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load events: ${e.message}"
                )
            }
        }
    }

    // This is now redundant since the factory provides the club name.
    // Kept here for context, but not used in the dynamic flow.
    private suspend fun getClubDetails(id: String): Club {
        // This function is no longer called in loadEventsData()
        val clubDoc = db.collection("clubs").document(id).get().await()
        return clubDoc.toObject(Club::class.java)
            ?: throw IllegalStateException("Club document not found for ID: $id.")
    }

    private suspend fun getEventsByClubName(clubName: String): List<Event> {
        val eventsQuery = db.collection("events")
            .whereEqualTo("clubName", clubName)
            .get()
            .await()

        return eventsQuery.documents.mapNotNull { it.toObject(Event::class.java) }
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create EventsViewModel. Fetches the club ID and name dynamically.
 */
class EventsViewModelFactory(
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    private var dynamicClubId: String? = null
    private var dynamicClubName: String? = null

    init {
        val clubService = ClubService(db)
        // runBlocking is required to make the async fetch synchronous for the factory
        runBlocking {
            try {
                val clubData = clubService.getClubByLeaderUid()
                dynamicClubId = clubData?.second // The document ID is the clubId
                dynamicClubName = clubData?.first?.name
            } catch (e: Exception) {
                println("ERROR: EventsViewModelFactory failed to fetch club details for leader: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            // Pass the dynamically fetched values, using empty strings if lookup failed.
            val clubIdToUse = dynamicClubId ?: ""
            val clubNameToUse = dynamicClubName ?: ""

            return EventsViewModel(clubIdToUse, clubNameToUse, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}