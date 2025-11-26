package com.example.clubapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId
    val id: String = "",
    val clubId: String = "",
    val clubName: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val registeredMemberIds: List<String> = emptyList(), // Only club members can register
    val maxAttendees: Int = 0, // 0 means unlimited
    val status: EventStatus = EventStatus.UPCOMING,
    val createdBy: String = "", // User ID
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)