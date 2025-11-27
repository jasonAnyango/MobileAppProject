package com.example.clubapp.model

import com.google.firebase.firestore.PropertyName

// --- SHARED DATA MODELS ---

data class Club(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var mission: String = "",
    var leaderId: String = "",
    var memberIds: List<String> = emptyList(),

    // NEW: Map to store specific roles within this club
    // Example: {"uid_123": "Treasurer", "uid_456": "Men's Captain"}
    var officers: Map<String, String> = emptyMap(),

    @get:PropertyName("isActive") var isActive: Boolean = true
)

data class ClubRegistration(
    var id: String = "",          // Changed from 'val' to 'var'
    var clubName: String = "",
    var description: String = "",
    var mission: String = "",
    var applicantId: String = "",
    var applicantName: String = "",
    var status: String = "Pending"
)

data class User(
    var uid: String = "",         // Changed from 'val' to 'var'
    var fullName: String = "",
    var email: String = "",
    var role: String = "Student",
    var clubsJoined: List<String> = emptyList(),
    @get:PropertyName("isSuspended") var isSuspended: Boolean = false,
    @get:PropertyName("isClubLeader") var isClubLeader: Boolean = false
)

data class Event(
    var id: String = "",          // Changed from 'val' to 'var'
    var title: String = "",
    var date: String = "",
    var location: String = "",
    var clubName: String = "",
    var description: String = ""
)

data class Announcement(
    var id: String = "",
    var title: String = "",
    var message: String = "",
    var date: String = "",
    var senderName: String = "",
    var clubId: String = "GLOBAL"
)

data class EventRegistration(
    var id: String = "",
    var eventId: String = "",
    var studentId: String = "",
    var status: String = "Registered", // "Registered", "Cancelled"
    var timestamp: Any? = null
)