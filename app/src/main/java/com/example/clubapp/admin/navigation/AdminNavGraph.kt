package com.example.clubapp.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clubapp.admin.ui.AdminDashboardScreen
import com.example.clubapp.admin.ui.AllEventsScreen
import com.example.clubapp.admin.ui.AllUsersScreen
import com.example.clubapp.admin.ui.ClubApplicationDetailsScreen
import com.example.clubapp.admin.ui.ClubDetailsScreen
import com.example.clubapp.admin.ui.ClubManagementScreen
import com.example.clubapp.admin.ui.EventDetailsScreen
import com.example.clubapp.admin.ui.ReportsScreen
import com.example.clubapp.admin.ui.UserDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavGraph() {
    val navController = rememberNavController()

    // Get current route to determine TopBar/BottomBar state
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define which screens are "Top Level" (No back arrow needed)
    val topLevelRoutes = listOf(
        AdminScreen.Dashboard.route,
        AdminScreen.AllClubs.route,
        AdminScreen.AllUsers.route,
        AdminScreen.AllEvents.route,
        AdminScreen.Reports.route
    )

    // Determine title based on route
    val currentTitle = when {
        currentRoute == AdminScreen.Dashboard.route -> "Dashboard"
        currentRoute == AdminScreen.AllClubs.route -> "Club Management"
        currentRoute == AdminScreen.AllUsers.route -> "User Management"
        currentRoute == AdminScreen.AllEvents.route -> "Events"
        currentRoute == AdminScreen.Reports.route -> "Reports"
        currentRoute?.contains("club_details") == true -> "Club Details"
        currentRoute?.contains("user_details") == true -> "User Profile"
        currentRoute?.contains("event_details") == true -> "Event Details"
        currentRoute?.contains("application") == true -> "Application Review"
        else -> "Admin Portal"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                navigationIcon = {
                    // Show Back Arrow ONLY if NOT on a Top Level screen
                    if (currentRoute !in topLevelRoutes) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Persistent Bottom Bar
            AdminBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AdminScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard
            composable(AdminScreen.Dashboard.route) {
                AdminDashboardScreen(
                    onAllClubs = { navController.navigate(AdminScreen.AllClubs.route) },
                    onAllUsers = { navController.navigate(AdminScreen.AllUsers.route) },
                    onAllEvents = { navController.navigate(AdminScreen.AllEvents.route) },
                    onReports = { navController.navigate(AdminScreen.Reports.route) }
                )
            }

            // Club Management
            composable(AdminScreen.AllClubs.route) {
                ClubManagementScreen(
                    onClubClick = { clubId -> navController.navigate(AdminScreen.clubDetails(clubId)) },
                    onApplicationClick = { appId -> navController.navigate(AdminScreen.clubApplicationDetails(appId)) }
                )
            }

            // Club Details
            composable(AdminScreen.ClubDetails.route, arguments = listOf(navArgument("clubId") { type = NavType.StringType })) {
                ClubDetailsScreen(it.arguments?.getString("clubId") ?: "")
            }

            // Application Details
            composable(AdminScreen.ClubApplicationDetails.route, arguments = listOf(navArgument("applicationId") { type = NavType.StringType })) {
                ClubApplicationDetailsScreen(it.arguments?.getString("applicationId") ?: "")
            }

            // All Users
            composable(AdminScreen.AllUsers.route) {
                AllUsersScreen(onUserClick = { userId -> navController.navigate(AdminScreen.userDetails(userId)) })
            }

            // User Details
            composable(AdminScreen.UserDetails.route, arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
                UserDetailsScreen(it.arguments?.getString("userId") ?: "")
            }

            // All Events
            composable(AdminScreen.AllEvents.route) {
                AllEventsScreen(onEventClick = { eventId -> navController.navigate(AdminScreen.eventDetails(eventId)) })
            }

            // Event Details
            composable(AdminScreen.EventDetails.route, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                EventDetailsScreen(it.arguments?.getString("eventId") ?: "")
            }

            // Reports
            composable(AdminScreen.Reports.route) {
                ReportsScreen()
            }
        }
    }
}

@Composable
fun AdminBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == AdminScreen.Dashboard.route,
            onClick = { onNavigate(AdminScreen.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("clubs") == true,
            onClick = { onNavigate(AdminScreen.AllClubs.route) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Clubs") },
            label = { Text("Clubs") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("users") == true,
            onClick = { onNavigate(AdminScreen.AllUsers.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Users") },
            label = { Text("Users") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("events") == true,
            onClick = { onNavigate(AdminScreen.AllEvents.route) },
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("reports") == true,
            onClick = { onNavigate(AdminScreen.Reports.route) },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
            label = { Text("Reports") }
        )
    }
}