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
    onLogout: () -> Unit // Logout function passed from the main activity/host
) {
    val navController = rememberNavController()

    // Get current route to help us decide when to show the "Back" arrow
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define the routes that should not have a "Back" button (usually the main bottom nav screens)
    val noBackRoutes = setOf(
        ClubLeaderScreen.Dashboard.route,
        ClubLeaderScreen.Events.route,
        ClubLeaderScreen.Members.route,
        ClubLeaderScreen.Announcements.route
    )

    val showBackArrow = currentRoute != null && currentRoute !in noBackRoutes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Club Portal") },
                navigationIcon = {
                    // Show Back Arrow ONLY if the current route is NOT a main nav screen
                    if (showBackArrow) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // The Logout Button (Always visible)
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

            // --- MAIN NAVIGATION ROUTES ---

            composable(ClubLeaderScreen.Dashboard.route) {
                ClubLeaderDashboardScreen(navController = navController)
            }

            composable(ClubLeaderScreen.Events.route) {
                EventsScreen(navController = navController)
            }

            composable(ClubLeaderScreen.Members.route) {
                MembersScreen(navController = navController)
            }

            composable(ClubLeaderScreen.Announcements.route) {
                AnnouncementsScreen(navController = navController)
            }

            // --- SUB-SCREENS (Form and Management) ---

            composable(ClubLeaderScreen.AddEvent.route) {
                // IMPORTANT: The AddEventScreen now handles its own navigation back
                // to Events via the ViewModel success state (using LaunchedEffect).
                AddEventScreen(navController = navController)
            }

            composable(ClubLeaderScreen.AddAnnouncement.route) {
                // Assuming AddAnnouncementScreen uses the same ViewModel/LaunchedEffect pattern
                // to navigate back to Announcements on publish success.
                AddAnnouncementScreen(navController = navController)
            }

            composable(ClubLeaderScreen.ManageClub.route) {
                // Placeholder for the Club Settings/Management screen
                ManageClubScreen(navController = navController)
            }
        }
    }
}