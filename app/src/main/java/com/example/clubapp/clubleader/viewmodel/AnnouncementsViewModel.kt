package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.clubleader.state.AnnouncementsUiState
import com.example.clubapp.model.Announcement
import com.example.clubapp.model.Club
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ------------------- VIEW MODEL -------------------

class AnnouncementsViewModel(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementsUiState())
    val uiState: StateFlow<AnnouncementsUiState> = _uiState

    init {
        loadAnnouncementsData()
    }

    private fun loadAnnouncementsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 1. Get Club Details (to get club name for display and filtering)
                val club = getClubDetails(clubId)

                // 2. Fetch all announcements posted by that club, sorted by date (newest first)
                val announcements = fetchAnnouncementsByClubId(clubId)

                _uiState.value = _uiState.value.copy(
                    clubName = club.name,
                    announcements = announcements,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalStateException -> e.message
                    else -> "Failed to load announcements: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    // --- Data Fetching Functions ---

    private suspend fun getClubDetails(id: String): Club {
        val clubDoc = db.collection("clubs").document(id).get().await()
        return clubDoc.toObject(Club::class.java)
            ?: throw IllegalStateException("Club document not found for ID: $id.")
    }

    private suspend fun fetchAnnouncementsByClubId(id: String): List<Announcement> {
        val announcementsQuery = db.collection("announcements")
            // Assuming the Announcement document stores the ID of the club that posted it
            .whereEqualTo("clubId", id)
            // Order by date descending (most recent first). Adjust "date" field name if necessary.
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()

        return announcementsQuery.documents.mapNotNull { it.toObject(Announcement::class.java) }
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create AnnouncementsViewModel with dependencies for testing.
 */
class AnnouncementsViewModelFactory(
    private val clubId: String,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnnouncementsViewModel::class.java)) {
            return AnnouncementsViewModel(clubId, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}