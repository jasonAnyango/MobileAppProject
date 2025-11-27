package com.example.clubapp.clubleader.state

import com.example.clubapp.model.Announcement // Assume your Announcement model is here

data class AnnouncementsUiState(
    val announcements: List<Announcement> = emptyList(),
    val clubName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)