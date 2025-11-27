package com.example.clubapp.clubleader.state

import com.example.clubapp.model.User // Assuming your User model is here
import com.example.clubapp.model.ClubRegistration // Assuming your ClubRegistration model is here

// Wrapper class to hold User data + Club-specific role
data class ClubMember(
    val user: User,
    val clubRole: String // e.g., "Men's Captain" or "Member"
)

data class MembersUiState(
    val allMembers: List<ClubMember> = emptyList(),
    val pendingRequests: List<ClubRegistration> = emptyList(),
    val clubName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)