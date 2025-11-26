package com.example.clubapp.admin.data

// Represents a verified, active club
data class Club(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val isActive: Boolean,
    val admins: List<String>,
    val recentEvent: String? = null
)

// Represents a new club application waiting for approval
data class ClubApplication(
    val id: String,
    val clubName: String,
    val applicantName: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val purpose: String,
    val mission: String,
    val foundingMemberId: String
)

// Represents a user in the system
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String, // "Student", "Admin", "Club Lead"
    val clubsJoined: List<String>
)

// Represents an event
data class Event(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val clubName: String,
    val description: String
)