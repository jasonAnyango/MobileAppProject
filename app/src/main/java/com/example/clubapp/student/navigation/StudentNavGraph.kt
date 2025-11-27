package com.example.clubapp.student.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clubapp.student.ui.BrowseClubsScreen  // Add this import
import com.example.clubapp.student.ui.MyClubsScreen      // Add this import
import com.example.clubapp.student.ui.StudentDashboardScreen
import com.example.clubapp.student.ui.BrowseEventsScreen
import com.example.clubapp.student.ui.MyEventsScreen
import com.example.clubapp.student.ui.StudentClubDetailsScreen
import com.example.clubapp.student.ui.StudentEventDetailsScreen
import com.example.clubapp.student.ui.JoinRequestsScreen
import com.example.clubapp.student.ui.CreateClubRequestScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNavGraph(
    studentRepository: com.example.clubapp.student.data.StudentRepository
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = listOf(
        StudentScreen.Dashboard.route,
        StudentScreen.BrowseClubs.route,
        StudentScreen.MyClubs.route,
        StudentScreen.BrowseEvents.route,
        StudentScreen.MyEvents.route,
        StudentScreen.JoinRequests.route
    )

    val currentTitle = when {
        currentRoute == StudentScreen.Dashboard.route -> "Student Dashboard"
        currentRoute == StudentScreen.BrowseClubs.route -> "Browse Clubs"
        currentRoute == StudentScreen.MyClubs.route -> "My Clubs"
        currentRoute == StudentScreen.BrowseEvents.route -> "Browse Events"
        currentRoute == StudentScreen.MyEvents.route -> "My Events"
        currentRoute == StudentScreen.JoinRequests.route -> "My Requests"
        currentRoute?.contains("club_details") == true -> "Club Details"
        currentRoute?.contains("event_details") == true -> "Event Details"
        currentRoute?.contains("create_club") == true -> "Create Club"
        else -> "Student Portal"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                navigationIcon = {
                    if (currentRoute !in topLevelRoutes) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            StudentBottomNavigation(
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
            startDestination = StudentScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard
            composable(StudentScreen.Dashboard.route) {
                StudentDashboardScreen(
                    studentRepository = studentRepository,
                    onBrowseClubs = { navController.navigate(StudentScreen.BrowseClubs.route) },
                    onMyClubs = { navController.navigate(StudentScreen.MyClubs.route) },
                    onBrowseEvents = { navController.navigate(StudentScreen.BrowseEvents.route) },
                    onMyEvents = { navController.navigate(StudentScreen.MyEvents.route) },
                    onCreateClub = { navController.navigate(StudentScreen.CreateClubRequest.route) }
                )
            }

            // Clubs
            composable(StudentScreen.BrowseClubs.route) {
                BrowseClubsScreen(
                    studentRepository = studentRepository,
                    onClubClick = { clubId -> navController.navigate(StudentScreen.clubDetails(clubId)) }
                )
            }

            composable(StudentScreen.MyClubs.route) {
                MyClubsScreen(
                    studentRepository = studentRepository,
                    onClubClick = { clubId -> navController.navigate(StudentScreen.clubDetails(clubId)) }
                )
            }

            composable(StudentScreen.ClubDetails.route,
                arguments = listOf(navArgument("clubId") { type = NavType.StringType })) {
                StudentClubDetailsScreen(
                    studentRepository = studentRepository,
                    clubId = it.arguments?.getString("clubId") ?: ""
                )
            }

            // Events
            composable(StudentScreen.BrowseEvents.route) {
                BrowseEventsScreen(
                    studentRepository = studentRepository,
                    onEventClick = { eventId -> navController.navigate(StudentScreen.eventDetails(eventId)) }
                )
            }

            composable(StudentScreen.MyEvents.route) {
                MyEventsScreen(
                    studentRepository = studentRepository,
                    onEventClick = { eventId -> navController.navigate(StudentScreen.eventDetails(eventId)) }
                )
            }

            composable(StudentScreen.EventDetails.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                StudentEventDetailsScreen(
                    studentRepository = studentRepository,
                    eventId = it.arguments?.getString("eventId") ?: ""
                )
            }

            // Requests
            composable(StudentScreen.JoinRequests.route) {
                JoinRequestsScreen(studentRepository = studentRepository)
            }

            composable(StudentScreen.CreateClubRequest.route) {
                CreateClubRequestScreen(
                    studentRepository = studentRepository,
                    onSubmit = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun StudentBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == StudentScreen.Dashboard.route,
            onClick = { onNavigate(StudentScreen.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("clubs") == true,
            onClick = { onNavigate(StudentScreen.BrowseClubs.route) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Clubs") },
            label = { Text("Clubs") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("events") == true,
            onClick = { onNavigate(StudentScreen.BrowseEvents.route) },
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") }
        )
        NavigationBarItem(
            selected = currentRoute?.contains("requests") == true,
            onClick = { onNavigate(StudentScreen.JoinRequests.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Requests") },
            label = { Text("Requests") }
        )
    }
}
