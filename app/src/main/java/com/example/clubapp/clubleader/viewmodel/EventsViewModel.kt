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

// ------------------- VIEW MODEL -------------------

class EventsViewModel(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState

    init {
        loadEventsData()
    }

    private fun loadEventsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 1. Get Club Name
                val club = getClubDetails(clubId)

                // 2. Fetch all events for that club
                val allEvents = getEventsByClubName(club.name)

                // 3. Filter the events
                val today = LocalDate.now()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val upcoming = mutableListOf<Event>()
                val past = mutableListOf<Event>()

                allEvents.forEach { event ->
                    try {
                        val eventDate = LocalDate.parse(event.date, dateFormatter)
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
                    clubName = club.name,
                    clubEvents = allEvents,
                    upcomingEvents = upcoming.sortedBy { it.date }, // Sort upcoming events
                    pastEvents = past.sortedByDescending { it.date }, // Sort past events
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalStateException -> e.message // Catching Club not found
                    else -> "Failed to load events: ${e.message}"
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

        return clubDoc.toObject(Club::class.java)
            ?: throw IllegalStateException("Club document not found for ID: $id.")
    }

    private suspend fun getEventsByClubName(clubName: String): List<Event> {
        val eventsQuery = db.collection("events")
            .whereEqualTo("clubName", clubName)
            .get()
            .await()

        // Map Firestore documents to Event model, excluding any that fail to map
        return eventsQuery.documents.mapNotNull { it.toObject(Event::class.java) }
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create EventsViewModel with dependencies for testing.
 */
class EventsViewModelFactory(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            return EventsViewModel(clubId, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}