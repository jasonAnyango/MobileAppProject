package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Data class to hold form state
data class AddEventUiState(
    val clubName: String = "IEEE Student Branch", // Hardcoded club name for testing
    val clubId: String = "cQqHqt95G2xmiCRxlrP2", // Hardcoded Club ID for testing
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class AddEventViewModel(
    private val clubId: String,
    private val clubName: String,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState(clubName = clubName, clubId = clubId))
    val uiState: StateFlow<AddEventUiState> = _uiState

    /**
     * Attempts to save a new event to the Firestore "events" collection.
     */
    fun saveEvent(title: String, date: String, location: String, description: String) {
        if (title.isBlank() || date.isBlank() || location.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all required fields.")
            return
        }

        val newEvent = Event(
            id = UUID.randomUUID().toString(), // Generate a unique ID
            clubName = clubName,
            title = title,
            date = date, // Format should be YYYY-MM-DD for sorting/filtering
            location = location,
            description = description,
            // You may add other fields like: createdBy, imageUrl, etc.
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            try {
                // Add the new event document to the 'events' collection
                db.collection("events").add(newEvent).await()

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                // Success feedback can be handled by the UI observing saveSuccess

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
 * Factory to create AddEventViewModel with dependencies.
 */
class AddEventViewModelFactory(
    private val clubId: String,
    private val clubName: String,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEventViewModel::class.java)) {
            return AddEventViewModel(clubId, clubName, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}