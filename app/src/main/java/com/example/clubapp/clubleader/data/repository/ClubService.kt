package com.example.clubapp.clubleader.data.repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.example.clubapp.model.Club

class ClubService(private val db: FirebaseFirestore) {

    /**
     * Finds the club document led by the currently logged-in user.
     *
     * @return The Club data model, including its document ID, or null if no club is found.
     * @throws Exception if Firebase operation fails or user is not logged in.
     */
    suspend fun getClubByLeaderUid(): Pair<Club, String>? { // Returns Pair<Club data, Club Document ID>
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUid == null) {
            // Throw an exception if this function is called without a logged-in user
            throw IllegalStateException("User must be logged in to fetch club leader data.")
        }

        // 1. Query the 'clubs' collection where the 'leaderId' field matches the current user's UID.
        val querySnapshot = db.collection("clubs")
            .whereEqualTo("leaderId", currentUid)
            .limit(1) // A user should only lead one club
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            return null // No club found for this leader
        }

        // 2. Extract the data and the document ID (which is the clubId you need everywhere)
        val clubDocument = querySnapshot.documents.first()
        val club = clubDocument.toObject<Club>()

        return if (club != null) {
            Pair(club, clubDocument.id)
        } else {
            null
        }
    }
}