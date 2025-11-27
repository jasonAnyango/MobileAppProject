package com.example.clubapp.student.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clubapp.model.User
import com.example.clubapp.student.data.StudentRepository
import com.example.clubapp.student.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNavGraph(
    studentRepository: StudentRepository,
    onLogout: () -> Unit,           // Parent callback to handle sign out
    onSwitchToLeader: () -> Unit    // Parent callback to switch NavHost
) {
    val navController = rememberNavController()

    // Dialog state
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 1. Check if User is a Leader (To show the Star button)
    val userState = produceState<User?>(initialValue = null) {
        value = studentRepository.getMyUserProfile()
    }
    val user = userState.value
    val isClubLeader = user?.role == "Club Lead" || user?.isClubLeader == true

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

    // Set up scroll behavior for the TopAppBar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Unify TopAppBar structure with AdminNavGraph
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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Go Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    actions = {
                        // 1. Switch Button (Conditional)
                        if (isClubLeader) {
                            IconButton(onClick = onSwitchToLeader) {
                                Icon(Icons.Default.Star, contentDescription = "Switch to Leader View", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // 2. Logout Button (Opens Dialog)
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    // --- COLOR FIX: Using default surface/background for a cleaner look ---
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface, // Clean background
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    scrollBehavior = scrollBehavior
                )
                // Subtle divider for structure
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
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

        // --- LOGOUT CONFIRMATION DIALOG ---
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        onLogout() // Executes the action passed from AppNavGraph
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
                    onCreateClub = { navController.navigate(StudentScreen.CreateClubRequest.route) },
                    onManageClub = onSwitchToLeader
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
