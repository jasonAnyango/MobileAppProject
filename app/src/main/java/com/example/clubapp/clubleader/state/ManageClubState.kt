package com.example.clubapp.clubleader.state

data class ManageClubUiState(
    // Data fields loaded from Club model
    val clubId: String = "cQqHqt95G2xmiCRxlrP2", // Hardcoded for testing
    val clubName: String = "IEEE Student Branch",
    val clubDescription: String = "",
    val meetingTime: String = "",
    val logoUrl: String? = null,

    // Status fields
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)