package com.example.clubapp.clubleader.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clubapp.clubleader.ui.*

@Composable
fun ClubLeaderNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ClubLeaderScreen.Dashboard.route
    ) {

        composable(ClubLeaderScreen.Dashboard.route) {
            ClubLeaderDashboardScreen(navController = navController)
        }

        composable(ClubLeaderScreen.Events.route) {
            EventsScreen(navController = navController)
        }

        composable(ClubLeaderScreen.AddEvent.route) {
            AddEventScreen(
                navController = navController,
                onPublish = { navController.popBackStack() } // return to Events screen
            )
        }

        composable(ClubLeaderScreen.Members.route) {
            MembersScreen(navController = navController)
        }

        composable(ClubLeaderScreen.Announcements.route) {
            AnnouncementsScreen(navController = navController)
        }

        composable(ClubLeaderScreen.AddAnnouncement.route) {
            AddAnnouncementScreen(
                navController = navController,
                onPublish = { navController.popBackStack() } // return to Announcements
            )
        }

        // ----------------- Manage Club -----------------
        composable(ClubLeaderScreen.ManageClub.route) {
            ManageClubScreen(navController = navController)
        }
    }
}
