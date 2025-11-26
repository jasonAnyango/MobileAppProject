package com.example.clubapp.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubapp.data.model.*
import com.example.clubapp.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StudentHomeState(
    val isLoading: Boolean = false,
    val clubs: List<Club> = emptyList(),
    val events: List<Event> = emptyList(),
    val membershipRequests: List<MembershipRequest> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val error: String? = null
)

class StudentHomeViewModel(
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository,
    private val membershipRepository: MembershipRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StudentHomeState())
    val state: StateFlow<StudentHomeState> = _state.asStateFlow()

    private var currentUserId: String = ""

    fun init(userId: String) {
        currentUserId = userId
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Load clubs
            clubRepository.getApprovedClubs().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            clubs = result.data ?: emptyList(),
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
            // Load upcoming events
            eventRepository.getUpcomingEvents().collect { result ->
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
            membershipRepository.getRequestsByStudent(currentUserId).collect { result ->
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

        viewModelScope.launch {
            // Load notifications
            notificationRepository.getNotificationsByUser(currentUserId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            notifications = result.data ?: emptyList()
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(currentUserId)
        }
    }
}