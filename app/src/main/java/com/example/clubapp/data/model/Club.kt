package com.example.clubapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Club(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "", // e.g., "Sports", "Academic", "Arts"
    val coverImageUrl: String = "",
    val additionalImages: List<String> = emptyList(),
    val leaderId: String = "", // Reference to User
    val leaderName: String = "",
    val memberIds: List<String> = emptyList(), // List of student User IDs
    val memberCount: Int = 0,
    val status: ClubStatus = ClubStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)