package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.clubleader.state.AddAnnouncementUiState
import com.example.clubapp.model.Announcement // Import your Announcement model
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID
import com.example.clubapp.clubleader.data.repository.ClubService // Assuming this import path
import kotlinx.coroutines.runBlocking

// ------------------- VIEW MODEL -------------------

class AddAnnouncementViewModel(
    private val clubId: String,
    private val clubName: String,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddAnnouncementUiState(
            clubName = clubName, // Use dynamic club name
            clubId = clubId      // Use dynamic club ID
        )
    )
    val uiState: StateFlow<AddAnnouncementUiState> = _uiState

    /**
     * Attempts to save a new announcement to the Firestore "announcements" collection.
     */
    fun saveAnnouncement(title: String, description: String) {
        if (clubId.isBlank() || clubName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Cannot publish: Club details are missing for this leader account.")
            return
        }
        if (title.isBlank() || description.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all required fields.")
            return
        }

        val newAnnouncement = Announcement(
            id = UUID.randomUUID().toString(), // Generate a unique ID
            clubId = clubId, // Use dynamic club ID
            title = title,
            message = description,
            date = LocalDate.now().toString() // Save the current date in YYYY-MM-DD format
            // Add other necessary fields (e.g., postedByUserId)
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            try {
                // Add the new announcement document to the 'announcements' collection
                db.collection("announcements").add(newAnnouncement).await()

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to publish announcement: ${e.message}"
                )
            }
        }
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create AddAnnouncementViewModel. Dynamically fetches the club ID and name.
 */
class AddAnnouncementViewModelFactory(
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
                println("ERROR: AddAnnouncementViewModelFactory failed to fetch club details for leader: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddAnnouncementViewModel::class.java)) {
            // Pass the dynamically fetched values
            val clubIdToUse = dynamicClubId ?: ""
            val clubNameToUse = dynamicClubName ?: "Unlinked Club"

            return AddAnnouncementViewModel(clubIdToUse, clubNameToUse, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}