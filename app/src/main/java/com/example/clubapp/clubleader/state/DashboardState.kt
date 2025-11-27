package com.example.clubapp.clubleader.state // Adjust package as needed

data class ClubDashboardUiState(
    val clubName: String = "Loading...",
    val memberCount: Int = 0,
    val eventCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)