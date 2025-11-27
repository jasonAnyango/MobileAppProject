package com.example.clubapp.clubleader.state

import com.example.clubapp.model.Event // Import your Event model

data class EventsUiState(
    val clubEvents: List<Event> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val pastEvents: List<Event> = emptyList(),
    val clubName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)