package com.example.clubapp.clubleader.state

data class AddAnnouncementUiState(
    val clubName: String = "IEEE Student Branch", // Placeholder for the club name
    val clubId: String = "cQqHqt95G2xmiCRxlrP2", // Hardcoded Club ID for testing
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)