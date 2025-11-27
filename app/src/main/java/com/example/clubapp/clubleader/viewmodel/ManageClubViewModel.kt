package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.clubleader.state.ManageClubUiState
import com.example.clubapp.model.Club // Import your Club model
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ------------------- VIEW MODEL -------------------

class ManageClubViewModel(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageClubUiState(clubId = clubId))
    val uiState: StateFlow<ManageClubUiState> = _uiState

    init {
        loadClubDetails()
    }

    private fun loadClubDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val club = getClubDetails(clubId)

                _uiState.value = _uiState.value.copy(
                    clubName = club.name,
                    clubDescription = club.description,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalStateException -> e.message
                    else -> "Failed to load club details: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    // --- Data Update Function ---

    fun saveClubDetails(name: String, description: String, meetingTime: String, newLogoUri: String? = null) {
        if (name.isBlank() || description.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Club Name and Description cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)

            try {
                // Prepare fields for update
                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "description" to description,
                    "meetingTime" to meetingTime,
                )

                // ⚠️ NOTE: Image upload logic would go here, updating the 'logoUrl' field.
                // For now, we only update if a new URI is provided (stubbed)
                newLogoUri?.let { updates["logoUrl"] = it }

                // Execute the update
                db.collection("clubs").document(clubId).update(updates).await()

                // Update the local state with the new values
                _uiState.value = _uiState.value.copy(
                    clubName = name,
                    clubDescription = description,
                    meetingTime = meetingTime,
                    isSaving = false,
                    saveSuccess = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save changes: ${e.message}"
                )
            }
        }
    }

    // --- Helper Function ---

    private suspend fun getClubDetails(id: String): Club {
        val clubDoc = db.collection("clubs").document(id).get().await()
        return clubDoc.toObject(Club::class.java)
            ?: throw IllegalStateException("Club document not found for ID: $id.")
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create ManageClubViewModel with dependencies.
 */
class ManageClubViewModelFactory(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageClubViewModel::class.java)) {
            return ManageClubViewModel(clubId, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}