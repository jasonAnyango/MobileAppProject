package com.example.clubapp.clubleader.navigation

sealed class ClubLeaderScreen(val route: String) {
    object Dashboard : ClubLeaderScreen("dashboard")
    object Events : ClubLeaderScreen("events")
    object AddEvent : ClubLeaderScreen("add_event")
    object Members : ClubLeaderScreen("members")
    object Announcements : ClubLeaderScreen("announcements")
    object AddAnnouncement : ClubLeaderScreen("add_announcement")
    object ManageClub : ClubLeaderScreen("manage_club")
}
