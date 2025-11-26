package com.example.clubapp.data.repository

import android.net.Uri
import com.example.clubapp.data.model.Event
import com.example.clubapp.data.model.EventStatus
import com.example.clubapp.data.model.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    fun getAllEvents(): Flow<Resource<List<Event>>> = callbackFlow {
        val listener = firestore.collection("events")
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch events"))
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                trySend(Resource.Success(events))
            }

        awaitClose { listener.remove() }
    }

    fun getEventsByClub(clubId: String): Flow<Resource<List<Event>>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("clubId", clubId)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch events"))
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                trySend(Resource.Success(events))
            }

        awaitClose { listener.remove() }
    }

    fun getUpcomingEvents(): Flow<Resource<List<Event>>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("status", EventStatus.UPCOMING.name)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch events"))
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                trySend(Resource.Success(events))
            }

        awaitClose { listener.remove() }
    }

    suspend fun getEventById(eventId: String): Resource<Event> {
        return try {
            val doc = firestore.collection("events").document(eventId).get().await()
            val event = doc.toObject(Event::class.java) ?: throw Exception("Event not found")
            Resource.Success(event)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch event")
        }
    }

    suspend fun createEvent(event: Event): Resource<String> {
        return try {
            val docRef = firestore.collection("events").document()
            val newEvent = event.copy(id = docRef.id)
            docRef.set(newEvent).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create event")
        }
    }

    suspend fun updateEvent(event: Event): Resource<Unit> {
        return try {
            firestore.collection("events").document(event.id).set(event).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update event")
        }
    }

    suspend fun deleteEvent(eventId: String): Resource<Unit> {
        return try {
            firestore.collection("events").document(eventId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete event")
        }
    }

    suspend fun registerForEvent(eventId: String, userId: String): Resource<Unit> {
        return try {
            firestore.collection("events").document(eventId)
                .update("registeredMemberIds", FieldValue.arrayUnion(userId))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to register for event")
        }
    }

    suspend fun unregisterFromEvent(eventId: String, userId: String): Resource<Unit> {
        return try {
            firestore.collection("events").document(eventId)
                .update("registeredMemberIds", FieldValue.arrayRemove(userId))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unregister from event")
        }
    }

    suspend fun uploadEventImage(eventId: String, imageUri: Uri): Resource<String> {
        return try {
            val ref = storage.reference.child("events/$eventId/${UUID.randomUUID()}.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Resource.Success(url)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload image")
        }
    }
}