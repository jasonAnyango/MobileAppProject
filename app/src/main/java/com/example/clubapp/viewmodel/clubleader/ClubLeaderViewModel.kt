package com.example.clubapp.viewmodel.clubleader

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubapp.data.model.*
import com.example.clubapp.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ClubLeaderState(
    val isLoading: Boolean = false,
    val club: Club? = null,
    val events: List<Event> = emptyList(),
    val membershipRequests: List<MembershipRequest> = emptyList(),
    val members: List<User> = emptyList(),
    val error: String? = null
)

class ClubLeaderViewModel(
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository,
    private val membershipRepository: MembershipRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClubLeaderState())
    val state: StateFlow<ClubLeaderState> = _state.asStateFlow()

    private var currentUserId: String = ""
    private var currentClubId: String = ""

    fun init(userId: String, clubId: String) {
        currentUserId = userId
        currentClubId = clubId
        loadClubData()
    }

    private fun loadClubData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Load club details
            when (val result = clubRepository.getClubById(currentClubId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        club = result.data,
                        isLoading = false
                    )
                    loadMembers(result.data?.memberIds ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }

        viewModelScope.launch {
            // Load club events
            eventRepository.getEventsByClub(currentClubId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            events = result.data ?: emptyList()
                        )
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            // Load membership requests
            membershipRepository.getRequestsByClub(currentClubId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            membershipRequests = result.data ?: emptyList()
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadMembers(memberIds: List<String>) {
        viewModelScope.launch {
            val members = mutableListOf<User>()
            memberIds.forEach { memberId ->
                when (val result = userRepository.getUserById(memberId)) {
                    is Resource.Success -> {
                        result.data?.let { members.add(it) }
                    }
                    else -> {}
                }
            }
            _state.value = _state.value.copy(members = members)
        }
    }

    fun updateClub(
        name: String,
        description: String,
        category: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val currentClub = _state.value.club ?: return@launch

            val updatedClub = currentClub.copy(
                name = name,
                description = description,
                category = category,
                updatedAt = com.google.firebase.Timestamp.now()
            )

            when (val result = clubRepository.updateClub(updatedClub)) {
                is Resource.Success -> {
                    onResult(true, "Club updated successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun uploadClubImage(imageUri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = clubRepository.uploadClubImage(currentClubId, imageUri)) {
                is Resource.Success -> {
                    val currentClub = _state.value.club ?: return@launch
                    val updatedClub = currentClub.copy(coverImageUrl = result.data ?: "")
                    clubRepository.updateClub(updatedClub)
                    onResult(true, "Image uploaded successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun approveMembershipRequest(requestId: String, studentId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = membershipRepository.approveRequest(requestId, currentClubId, studentId, currentUserId)) {
                is Resource.Success -> {
                    onResult(true, "Member approved successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun rejectMembershipRequest(requestId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = membershipRepository.rejectRequest(requestId, currentUserId)) {
                is Resource.Success -> {
                    onResult(true, "Request rejected")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun removeMember(memberId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = membershipRepository.removeMember(currentClubId, memberId)) {
                is Resource.Success -> {
                    onResult(true, "Member removed successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }
}