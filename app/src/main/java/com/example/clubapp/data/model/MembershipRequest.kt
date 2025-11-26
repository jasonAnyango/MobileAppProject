package com.example.clubapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MembershipRequest(
    @DocumentId
    val id: String = "",
    val clubId: String = "",
    val clubName: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val message: String = "", // Optional message from student
    val status: MembershipStatus = MembershipStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null,
    val respondedBy: String = "" // Club leader ID
)