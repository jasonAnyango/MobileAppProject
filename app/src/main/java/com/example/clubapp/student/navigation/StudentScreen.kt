package com.example.clubapp.student.navigation

sealed class StudentScreen(val route: String) {
    // --- 1. GENERAL STUDENT SCREENS ---
    object Dashboard : StudentScreen("student_dashboard")

    // Clubs
    object BrowseClubs : StudentScreen("student_browse_clubs")
    object MyClubs : StudentScreen("student_my_clubs")
    object ClubDetails : StudentScreen("student_club_details/{clubId}")

    // Events
    object BrowseEvents : StudentScreen("student_browse_events")
    object MyEvents : StudentScreen("student_my_events")
    object EventDetails : StudentScreen("student_event_details/{eventId}")

    // Requests
    object JoinRequests : StudentScreen("student_join_requests")
    object CreateClubRequest : StudentScreen("student_create_club_request")

    // --- 2. CLUB LEADER SCREENS (NEW) ---

    // Screen to pick which club to manage (if they lead more than one)
    object ClubSelection : StudentScreen("leader_select_club")

    // The Dashboard for a specific club (Requires ID)
    object ManageClub : StudentScreen("leader_manage_club/{clubId}")

    // Add Event specifically for that club
    object AddEvent : StudentScreen("leader_add_event/{clubId}")

    // Manage Members specifically for that club
    object ManageMembers : StudentScreen("leader_manage_members/{clubId}")

    // --- 3. HELPER FUNCTIONS ---
    companion object {
        // Student Helpers
        fun clubDetails(clubId: String) = "student_club_details/$clubId"
        fun eventDetails(eventId: String) = "student_event_details/$eventId"

        // Leader Helpers (To pass the ID easily)
        fun manageClub(clubId: String) = "leader_manage_club/$clubId"
        fun addEvent(clubId: String) = "leader_add_event/$clubId"
        fun manageMembers(clubId: String) = "leader_manage_members/$clubId"
    }
}