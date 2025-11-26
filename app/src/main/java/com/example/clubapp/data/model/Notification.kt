package com.example.clubapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Notification(
    @DocumentId
    val id: String = "",
    val userId: String = "", // Recipient
    val type: NotificationType = NotificationType.SYSTEM_ANNOUNCEMENT,
    val title: String = "",
    val message: String = "",
    val relatedId: String = "", // Club ID, Event ID, etc.
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)