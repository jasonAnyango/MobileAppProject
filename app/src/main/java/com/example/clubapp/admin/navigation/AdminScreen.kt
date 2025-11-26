package com.example.clubapp.admin.navigation

sealed class AdminScreen(val route: String) {
    object Dashboard : AdminScreen("admin_dashboard")

    // Clubs
    object AllClubs : AdminScreen("admin_all_clubs")
    object ClubDetails : AdminScreen("admin_club_details/{clubId}")
    object ClubApplications : AdminScreen("admin_club_applications")
    object ClubApplicationDetails : AdminScreen("admin_club_application_details/{applicationId}")

    // Users
    object AllUsers : AdminScreen("admin_all_users")
    object UserDetails : AdminScreen("admin_user_details/{userId}")

    // Events
    object AllEvents : AdminScreen("admin_all_events")
    object EventDetails : AdminScreen("admin_event_details/{eventId}")

    // Reports
    object Reports : AdminScreen("admin_reports")

    // Helper functions to build routes with arguments
    companion object {
        fun clubDetails(clubId: String) = "admin_club_details/$clubId"
        fun clubApplicationDetails(applicationId: String) = "admin_club_application_details/$applicationId"
        fun userDetails(userId: String) = "admin_user_details/$userId"
        fun eventDetails(eventId: String) = "admin_event_details/$eventId"
    }
}