package com.example.clubapp.clubleader.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp // Logout Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
// Import your UI screens
import com.example.clubapp.clubleader.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubLeaderNavGraph(
    onLogout: () -> Unit // <--- 1. We added this parameter so they can Log Out
) {
    val navController = rememberNavController()

    // Get current route to help us decide when to show the "Back" arrow
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // The Dashboard is the "Home", so it shouldn't have a back arrow
    val isDashboard = currentRoute == ClubLeaderScreen.Dashboard.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Club Portal") },
                navigationIcon = {
                    // Show Back Arrow ONLY if NOT on the Dashboard
                    if (!isDashboard) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // 2. The Logout Button (Always visible)
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->

        // Pass innerPadding so the content isn't covered by the Top Bar
        NavHost(
            navController = navController,
            startDestination = ClubLeaderScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
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
                    onPublish = { navController.popBackStack() } // Return to Events list
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
                    onPublish = { navController.popBackStack() } // Return to Announcements list
                )
            }

            composable(ClubLeaderScreen.ManageClub.route) {
                ManageClubScreen(navController = navController)
            }
        }
    }
}