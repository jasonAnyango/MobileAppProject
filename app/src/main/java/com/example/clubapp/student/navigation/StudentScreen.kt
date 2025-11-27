
package com.example.clubapp.student.navigation

sealed class StudentScreen(val route: String) {
    object Dashboard : StudentScreen("student_dashboard")

    // Clubs
    object BrowseClubs : StudentScreen("student_browse_clubs")
    object ClubDetails : StudentScreen("student_club_details/{clubId}")
    object MyClubs : StudentScreen("student_my_clubs")

    // Events
    object BrowseEvents : StudentScreen("student_browse_events")
    object EventDetails : StudentScreen("student_event_details/{eventId}")
    object MyEvents : StudentScreen("student_my_events")

    // Requests
    object JoinRequests : StudentScreen("student_join_requests")
    object CreateClubRequest : StudentScreen("student_create_club_request")

    companion object {
        fun clubDetails(clubId: String) = "student_club_details/$clubId"
        fun eventDetails(eventId: String) = "student_event_details/$eventId"
    }
}
