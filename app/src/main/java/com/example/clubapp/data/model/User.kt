package com.example.clubapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: UserRole = UserRole.STUDENT,
    val profileImageUrl: String = "",
    val phoneNumber: String = "",
    val studentId: String = "", // For students
    val clubId: String? = null, // For club leaders (one club per leader)
    val isEmailVerified: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)