package com.example.clubapp.viewmodel.clubleader

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubapp.data.model.*
import com.example.clubapp.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EventManagementState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val error: String? = null
)

class EventManagementViewModel(
    private val eventRepository: EventRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EventManagementState())
    val state: StateFlow<EventManagementState> = _state.asStateFlow()

    private var currentClubId: String = ""
    private var currentUserId: String = ""

    fun init(userId: String, clubId: String) {
        currentUserId = userId
        currentClubId = clubId
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            eventRepository.getEventsByClub(currentClubId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            events = result.data ?: emptyList(),
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
    }

    fun createEvent(
        title: String,
        description: String,
        location: String,
        startTime: com.google.firebase.Timestamp,
        endTime: com.google.firebase.Timestamp,
        maxAttendees: Int,
        clubName: String,
        imageUrl: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val event = Event(
                clubId = currentClubId,
                clubName = clubName,
                title = title,
                description = description,
                location = location,
                startTime = startTime,
                endTime = endTime,
                maxAttendees = maxAttendees,
                imageUrl = imageUrl,
                createdBy = currentUserId,
                status = EventStatus.UPCOMING
            )

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
            val updatedEvent = event.copy(
                updatedAt = com.google.firebase.Timestamp.now()
            )

            when (val result = eventRepository.updateEvent(updatedEvent)) {
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

    fun uploadEventImage(eventId: String, imageUri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = eventRepository.uploadEventImage(eventId, imageUri)) {
                is Resource.Success -> {
                    onResult(true, result.data)
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }
}