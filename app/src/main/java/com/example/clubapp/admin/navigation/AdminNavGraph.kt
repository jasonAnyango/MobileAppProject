package com.example.clubapp.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.clubapp.admin.ui.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavGraph(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // Track current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Top-level routes (no back arrow)
    val topLevelRoutes = listOf(
        AdminScreen.Dashboard.route,
        AdminScreen.AllClubs.route,
        AdminScreen.AllUsers.route,
        AdminScreen.AllEvents.route,
        AdminScreen.Reports.route
    )

    // Logout confirmation dialog state
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Nice professional titles
    val currentTitle = when {
        currentRoute == AdminScreen.Dashboard.route -> "Admin Dashboard"
        currentRoute == AdminScreen.AllClubs.route -> "Clubs"
        currentRoute == AdminScreen.AllUsers.route -> "Users"
        currentRoute == AdminScreen.AllEvents.route -> "Events"
        currentRoute == AdminScreen.Reports.route -> "Reports & Analytics"
        currentRoute?.contains("club_details") == true -> "Club Overview"
        currentRoute?.contains("user_details") == true -> "User Overview"
        currentRoute?.contains("event_details") == true -> "Event Overview"
        currentRoute?.contains("application") == true -> "Application Details"
        else -> "Admin Portal"
    }

    Scaffold(
        topBar = {
            // Improved TopAppBar: bold title, clearer wording, better spacing
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        if (currentRoute !in topLevelRoutes) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Go Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    actions = {
                        // Logout opens confirmation dialog
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                )
                // subtle divider for structure
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        },
        bottomBar = {
            // Only show Bottom Bar on top-level screens
            if (currentRoute in topLevelRoutes) {
                AdminBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // pop up to the real start destination (robust)
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // Logout confirmation dialog (Material3 AlertDialog)
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

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
                    onClubClick = { clubId -> navController.navigate(AdminScreen.clubDetails(clubId)) },
                    onApplicationClick = { appId -> navController.navigate(AdminScreen.clubApplicationDetails(appId)) }
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
                AllUsersScreen(onUserClick = { userId -> navController.navigate(AdminScreen.userDetails(userId)) })
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
                AllEventsScreen(onEventClick = { eventId -> navController.navigate(AdminScreen.eventDetails(eventId)) })
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
