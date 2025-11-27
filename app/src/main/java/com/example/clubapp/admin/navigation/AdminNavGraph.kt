package com.example.clubapp.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp // Logout Icon
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

// Import your UI screens
import com.example.clubapp.admin.ui.* @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavGraph(
    onLogout: () -> Unit // Pass the logout function here
) {
    val navController = rememberNavController()

    // Get current route to determine TopBar/BottomBar state
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define which screens are "Top Level" (Show Bottom Bar, No Back Arrow)
    // We check against YOUR AdminScreen routes
    val topLevelRoutes = listOf(
        AdminScreen.Dashboard.route,
        AdminScreen.AllClubs.route,
        AdminScreen.AllUsers.route,
        AdminScreen.AllEvents.route,
        AdminScreen.Reports.route
    )

    // Dynamic Title Logic
    val currentTitle = when {
        currentRoute == AdminScreen.Dashboard.route -> "Dashboard"
        currentRoute == AdminScreen.AllClubs.route -> "Club Management"
        currentRoute == AdminScreen.AllUsers.route -> "User Management"
        currentRoute == AdminScreen.AllEvents.route -> "Events"
        currentRoute == AdminScreen.Reports.route -> "Reports"
        // Check for contain matches for details screens
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
                actions = {
                    // LOGOUT BUTTON
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Only show Bottom Bar on top-level screens
            if (currentRoute in topLevelRoutes) {
                AdminBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(AdminScreen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AdminScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // 1. Dashboard
            composable(AdminScreen.Dashboard.route) {
                AdminDashboardScreen(
                    onAllClubs = { navController.navigate(AdminScreen.AllClubs.route) },
                    onAllUsers = { navController.navigate(AdminScreen.AllUsers.route) },
                    onAllEvents = { navController.navigate(AdminScreen.AllEvents.route) },
                    onReports = { navController.navigate(AdminScreen.Reports.route) }
                )
            }

            // 2. All Clubs (Management)
            composable(AdminScreen.AllClubs.route) {
                ClubManagementScreen(
                    onClubClick = { clubId ->
                        navController.navigate(AdminScreen.clubDetails(clubId))
                    },
                    onApplicationClick = { appId ->
                        navController.navigate(AdminScreen.clubApplicationDetails(appId))
                    }
                )
            }

            // 3. Club Details
            composable(
                route = AdminScreen.ClubDetails.route,
                arguments = listOf(navArgument("clubId") { type = NavType.StringType })
            ) { backStackEntry ->
                ClubDetailsScreen(clubId = backStackEntry.arguments?.getString("clubId") ?: "")
            }

            // 4. Club Application Details
            composable(
                route = AdminScreen.ClubApplicationDetails.route,
                arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
            ) { backStackEntry ->
                ClubApplicationDetailsScreen(
                    applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
                )
            }

            // 5. All Users
            composable(AdminScreen.AllUsers.route) {
                AllUsersScreen(
                    onUserClick = { userId ->
                        navController.navigate(AdminScreen.userDetails(userId))
                    }
                )
            }

            // 6. User Details
            composable(
                route = AdminScreen.UserDetails.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                UserDetailsScreen(userId = backStackEntry.arguments?.getString("userId") ?: "")
            }

            // 7. All Events
            composable(AdminScreen.AllEvents.route) {
                AllEventsScreen(
                    onEventClick = { eventId ->
                        navController.navigate(AdminScreen.eventDetails(eventId))
                    }
                )
            }

            // 8. Event Details
            composable(
                route = AdminScreen.EventDetails.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                EventDetailsScreen(eventId = backStackEntry.arguments?.getString("eventId") ?: "")
            }

            // 9. Reports
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
            selected = currentRoute == AdminScreen.AllClubs.route,
            onClick = { onNavigate(AdminScreen.AllClubs.route) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Clubs") },
            label = { Text("Clubs") }
        )
        NavigationBarItem(
            selected = currentRoute == AdminScreen.AllUsers.route,
            onClick = { onNavigate(AdminScreen.AllUsers.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Users") },
            label = { Text("Users") }
        )
        NavigationBarItem(
            selected = currentRoute == AdminScreen.AllEvents.route,
            onClick = { onNavigate(AdminScreen.AllEvents.route) },
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") }
        )
        NavigationBarItem(
            selected = currentRoute == AdminScreen.Reports.route,
            onClick = { onNavigate(AdminScreen.Reports.route) },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
            label = { Text("Reports") }
        )
    }
}