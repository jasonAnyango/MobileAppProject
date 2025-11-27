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
import com.example.clubapp.clubleader.data.repository.ClubService
import kotlinx.coroutines.runBlocking

// ------------------- VIEW MODEL -------------------

class AnnouncementsViewModel(
    private val clubId: String,
    private val clubName: String, // 💡 NEW: Receive clubName from the Factory
    private val db: FirebaseFirestore
) : ViewModel() {

    // Initialize UiState with the received clubName
    private val _uiState = MutableStateFlow(AnnouncementsUiState(clubName = clubName))
    val uiState: StateFlow<AnnouncementsUiState> = _uiState

    init {
        // Only load data if a valid club ID was found
        if (clubId.isNotEmpty()) {
            loadAnnouncementsData()
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Club Leader data not found. Ensure your account is linked to a club.",
                clubName = "Unlinked User"
            )
        }
    }

    private fun loadAnnouncementsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 1. Fetch all announcements posted by that club, sorted by date (newest first)
                val announcements = fetchAnnouncementsByClubId(clubId)

                _uiState.value = _uiState.value.copy(
                    // clubName is already set in the UiState constructor
                    announcements = announcements,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = "Failed to load announcements: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    // --- Data Fetching Functions ---

    // Removed getClubDetails as clubName is fetched in the Factory

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
 * Factory to create AnnouncementsViewModel. Fetches the club ID and name dynamically.
 */
class AnnouncementsViewModelFactory(
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
                dynamicClubId = clubData?.second // The document ID is the clubId
                dynamicClubName = clubData?.first?.name
            } catch (e: Exception) {
                println("ERROR: AnnouncementsViewModelFactory failed to fetch club details for leader: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnnouncementsViewModel::class.java)) {
            // Pass the dynamically fetched values
            val clubIdToUse = dynamicClubId ?: ""
            val clubNameToUse = dynamicClubName ?: "Loading Club..."

            return AnnouncementsViewModel(clubIdToUse, clubNameToUse, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}