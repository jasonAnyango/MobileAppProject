package com.example.clubapp.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubapp.data.model.*
import com.example.clubapp.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val clubs: List<Club> = emptyList(),
    val pendingClubs: List<Club> = emptyList(),
    val events: List<Event> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val userRepository: UserRepository,
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository,
    private val membershipRepository: MembershipRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Load all users
            userRepository.getAllUsers().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            users = result.data ?: emptyList(),
                            isLoading = false
                        )
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
        }

        viewModelScope.launch {
            // Load all clubs
            clubRepository.getAllClubs().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            clubs = result.data ?: emptyList()
                        )
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            // Load pending clubs
            clubRepository.getPendingClubs().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            pendingClubs = result.data ?: emptyList()
                        )
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            // Load all events
            eventRepository.getAllEvents().collect { result ->
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
    }

    // User Management
    fun promoteToClubLeader(userId: String, clubId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = userRepository.promoteToClubLeader(userId, clubId)) {
                is Resource.Success -> {
                    onResult(true, "User promoted to Club Leader successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun promoteToAdmin(userId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = userRepository.promoteToAdmin(userId)) {
                is Resource.Success -> {
                    onResult(true, "User promoted to Admin successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteUser(userId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = userRepository.deleteUser(userId)) {
                is Resource.Success -> {
                    onResult(true, "User deleted successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    // Club Management
    fun approveClub(clubId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = clubRepository.approveClub(clubId)) {
                is Resource.Success -> {
                    // Get club details to send notification to leader
                    clubRepository.getClubById(clubId).let { clubResult ->
                        if (clubResult is Resource.Success && clubResult.data != null) {
                            val club = clubResult.data
                            notificationRepository.sendNotificationToUser(
                                userId = club.leaderId,
                                type = NotificationType.CLUB_APPROVED,
                                title = "Club Approved!",
                                message = "Your club '${club.name}' has been approved by admin.",
                                relatedId = clubId
                            )
                        }
                    }
                    onResult(true, "Club approved successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun rejectClub(clubId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = clubRepository.rejectClub(clubId)) {
                is Resource.Success -> {
                    // Get club details to send notification to leader
                    clubRepository.getClubById(clubId).let { clubResult ->
                        if (clubResult is Resource.Success && clubResult.data != null) {
                            val club = clubResult.data
                            notificationRepository.sendNotificationToUser(
                                userId = club.leaderId,
                                type = NotificationType.CLUB_REJECTED,
                                title = "Club Rejected",
                                message = "Your club '${club.name}' has been rejected by admin.",
                                relatedId = clubId
                            )
                        }
                    }
                    onResult(true, "Club rejected")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteClub(clubId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = clubRepository.deleteClub(clubId)) {
                is Resource.Success -> {
                    onResult(true, "Club deleted successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun createClub(club: Club, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = clubRepository.createClub(club)) {
                is Resource.Success -> {
                    onResult(true, "Club created successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun updateClub(club: Club, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = clubRepository.updateClub(club)) {
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

    //Event Management
    fun deleteEvent(eventId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = eventRepository.deleteEvent(eventId)) {
                is Resource.Success -> {
                    onResult(true, "Event deleted successfully!")
                }

                is Resource.Error -> {
                    onResult(false, result.message)
                }

                else -> {}
            }
        }
    }

    fun createEvent(event: Event, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = eventRepository.createEvent(event)) {
                is Resource.Success -> {
                    onResult(true, "Event created successfully!")
                }

                is Resource.Error -> {
                    onResult(false, result.message)
                }

                else -> {}
            }
        }
    }

    fun updateEvent(event: Event, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = eventRepository.updateEvent(event)) {
                is Resource.Success -> {
                    onResult(true, "Event updated successfully!")
                }

                is Resource.Error -> {
                    onResult(false, result.message)
                }

                else -> {}
            }
        }
    }

    // System Announcements
    fun sendSystemAnnouncement(
        title: String,
        message: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val userIds = _state.value.users.map { it.id }

            when (val result =
                notificationRepository.sendSystemAnnouncement(title, message, userIds)) {
                is Resource.Success -> {
                    onResult(true, "Announcement sent to all users!")
                }

                is Resource.Error -> {
                    onResult(false, result.message)
                }

                else -> {}
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}