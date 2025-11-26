package com.example.clubapp.data.repository

import com.example.clubapp.data.model.Notification
import com.example.clubapp.data.model.NotificationType
import com.example.clubapp.data.model.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore
) {
    fun getNotificationsByUser(userId: String): Flow<Resource<List<Notification>>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch notifications"))
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull {
                    it.toObject(Notification::class.java)
                } ?: emptyList()
                trySend(Resource.Success(notifications))
            }

        awaitClose { listener.remove() }
    }

    suspend fun createNotification(notification: Notification): Resource<String> {
        return try {
            val docRef = firestore.collection("notifications").document()
            val newNotification = notification.copy(id = docRef.id)
            docRef.set(newNotification).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create notification")
        }
    }

    suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark as read")
        }
    }

    suspend fun markAllAsRead(userId: String): Resource<Unit> {
        return try {
            val notifications = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            firestore.runBatch { batch ->
                notifications.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark all as read")
        }
    }

    suspend fun deleteNotification(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete notification")
        }
    }

    // Helper to send notification to a specific user
    suspend fun sendNotificationToUser(
        userId: String,
        type: NotificationType,
        title: String,
        message: String,
        relatedId: String = ""
    ): Resource<String> {
        val notification = Notification(
            userId = userId,
            type = type,
            title = title,
            message = message,
            relatedId = relatedId
        )
        return createNotification(notification)
    }

    // Helper to send system-wide announcement
    suspend fun sendSystemAnnouncement(title: String, message: String, userIds: List<String>): Resource<Unit> {
        return try {
            firestore.runBatch { batch ->
                userIds.forEach { userId ->
                    val docRef = firestore.collection("notifications").document()
                    val notification = Notification(
                        id = docRef.id,
                        userId = userId,
                        type = NotificationType.SYSTEM_ANNOUNCEMENT,
                        title = title,
                        message = message
                    )
                    batch.set(docRef, notification)
                }
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send announcement")
        }
    }
}