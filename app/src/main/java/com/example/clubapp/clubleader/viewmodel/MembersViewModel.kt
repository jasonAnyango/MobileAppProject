package com.example.clubapp.clubleader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clubapp.clubleader.state.ClubMember
import com.example.clubapp.clubleader.state.MembersUiState
import com.example.clubapp.model.Club
import com.example.clubapp.model.ClubRegistration
import com.example.clubapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.clubapp.clubleader.data.repository.ClubService
import kotlinx.coroutines.runBlocking

// ------------------- VIEW MODEL -------------------

class MembersViewModel(
    private val clubId: String,
    private val clubName: String, // 💡 NEW: Receive clubName from the Factory
    private val db: FirebaseFirestore
) : ViewModel() {

    // Initialize UiState with the received clubName
    private val _uiState = MutableStateFlow(MembersUiState(clubName = clubName))
    val uiState: StateFlow<MembersUiState> = _uiState

    init {
        // Check if a club was found for the leader
        if (clubId.isNotEmpty()) {
            loadMembersData()
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Club Leader data not found. Ensure your account is linked to a club.",
                clubName = "Unlinked User"
            )
        }
    }

    // --- Public action for handling requests ---
    fun approveRequest(registrationId: String) {
        // TODO: Implement logic to update ClubRegistration status to "Approved",
        // add applicantId to Club.memberIds, and update User.clubsJoined.
    }

    fun rejectRequest(registrationId: String) {
        // TODO: Implement logic to update ClubRegistration status to "Rejected" or delete the document.
    }

    private fun loadMembersData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 1. Get Club Details (to get member IDs and officers map)
                val club = getClubDetails(clubId)

                // 2. Fetch User Details for all member IDs
                val allClubMembers = fetchAllClubMembers(club.memberIds, club.officers)

                // 3. Fetch Pending Requests
                val pendingRequests = fetchPendingRequests(club.name)

                _uiState.value = _uiState.value.copy(
                    // clubName is already set in the UiState constructor
                    allMembers = allClubMembers,
                    pendingRequests = pendingRequests,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalStateException -> e.message
                    else -> "Failed to load members: ${e.message}"
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

    private suspend fun fetchAllClubMembers(memberIds: List<String>, officers: Map<String, String>): List<ClubMember> {
        if (memberIds.isEmpty()) return emptyList()

        // Firestore supports 'in' queries for up to 10 items. For simplicity,
        // we'll use a single query, but in production, you'd batch queries for larger lists.
        val usersQuery = db.collection("users")
            .whereIn("uid", memberIds)
            .get()
            .await()

        return usersQuery.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java)
            if (user != null) {
                // Determine the member's specific role in the club
                val role = officers[user.uid] ?: "Member"
                ClubMember(user, role)
            } else {
                null
            }
        }
    }

    private suspend fun fetchPendingRequests(clubName: String): List<ClubRegistration> {
        val pendingQuery = db.collection("club_registrations")
            .whereEqualTo("clubName", clubName)
            .whereEqualTo("status", "Pending")
            .get()
            .await()

        return pendingQuery.documents.mapNotNull { it.toObject(ClubRegistration::class.java) }
    }
}

// ------------------- VIEW MODEL FACTORY -------------------

/**
 * Factory to create MembersViewModel. Fetches the club ID and name dynamically.
 */
class MembersViewModelFactory(
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
                println("ERROR: MembersViewModelFactory failed to fetch club details for leader: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MembersViewModel::class.java)) {
            // Pass the dynamically fetched values
            val clubIdToUse = dynamicClubId ?: ""
            val clubNameToUse = dynamicClubName ?: "Loading Club..."

            return MembersViewModel(clubIdToUse, clubNameToUse, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}