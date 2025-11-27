package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.clubapp.clubleader.data.repository.ClubService // Assuming this import path
import kotlinx.coroutines.runBlocking

// Data class to hold form state
data class AddEventUiState(
    val clubName: String = "Loading Club...", // Dynamic club name
    val clubId: String = "", // Dynamic Club ID
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class AddEventViewModel(
    private val clubId: String,
    private val clubName: String,
    private val db: FirebaseFirestore
) : ViewModel() {

    // Initialize with dynamic club name and ID
    private val _uiState = MutableStateFlow(AddEventUiState(clubName = clubName, clubId = clubId))
    val uiState: StateFlow<AddEventUiState> = _uiState

    /**
     * Attempts to save a new event to the Firestore "events" collection.
     */
    fun saveEvent(title: String, date: String, location: String, description: String) {
        if (clubId.isBlank() || clubName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Cannot add event: Club details are missing.")
            return
        }
        if (title.isBlank() || date.isBlank() || location.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all required fields.")
            return
        }

        val newEvent = Event(
            id = UUID.randomUUID().toString(), // Generate a unique ID
            clubName = clubName, // Use the dynamically loaded clubName
            title = title,
            date = date,
            location = location,
            description = description,
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            try {
                // Add the new event document to the 'events' collection
                db.collection("events").add(newEvent).await()

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to add event: ${e.message}"
                )
            }
        }
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create AddEventViewModel. Dynamically fetches the club ID and name.
 */
class AddEventViewModelFactory(
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    private var dynamicClubId: String? = null
    private var dynamicClubName: String? = null

    init {
        val clubService = ClubService(db)
        // runBlocking is required here for synchronous factory creation
        runBlocking {
            try {
                val clubData = clubService.getClubByLeaderUid()
                dynamicClubId = clubData?.second // The document ID is the clubId
                dynamicClubName = clubData?.first?.name
            } catch (e: Exception) {
                println("ERROR: AddEventViewModelFactory failed to fetch club details for leader: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEventViewModel::class.java)) {
            // Pass the dynamically fetched values
            val clubIdToUse = dynamicClubId ?: ""
            val clubNameToUse = dynamicClubName ?: "Unlinked Club"

            return AddEventViewModel(clubIdToUse, clubNameToUse, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}