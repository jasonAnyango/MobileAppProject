package com.example.clubapp.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clubapp.data.model.UserRole
import com.example.clubapp.ui.screens.auth.EmailVerificationScreen
import com.example.clubapp.ui.screens.auth.LoginScreen
import com.example.clubapp.ui.screens.auth.RegisterScreen
import com.example.clubapp.viewmodel.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

// Navigation Routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object EmailVerification : Screen("email_verification")

    // Student Routes
    object StudentHome : Screen("student/home")
    object ClubBrowse : Screen("student/clubs")
    object Notifications : Screen("student/notifications")
    object ClubDetail : Screen("student/club/{clubId}") {
        fun createRoute(clubId: String) = "student/club/$clubId"
    }
    object EventDetail : Screen("student/event/{eventId}") {
        fun createRoute(eventId: String) = "student/event/$eventId"
    }
    object StudentProfile : Screen("student/profile")

    // Club Leader Routes
    object ClubLeaderHome : Screen("clubleader/home")
    object ManageClub : Screen("clubleader/manage-club")
    object ManageEventsLeader : Screen("admin/manage-events") // Keep for club leaders
    object CreateEvent : Screen("clubleader/create-event")
    object EditEvent : Screen("clubleader/edit-event/{eventId}") {
        fun createRoute(eventId: String) = "clubleader/edit-event/$eventId"
    }
    object MemberRequests : Screen("clubleader/member-requests")

    // Admin Routes
    object AdminHome : Screen("admin/home")
    object ManageUsers : Screen("admin/manage-users")
    object ManageClubs : Screen("admin/manage-clubs")
    object ClubApproval : Screen("admin/club-approval")
    object ManageEventsAdmin : Screen("admin/all-events") // Different route for admin
    object SystemAnnouncements : Screen("admin/announcements")
    object CreateClubAdmin : Screen("admin/create-club") // NEW: Admin club creation
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    // Use LaunchedEffect to handle auth state changes properly
    LaunchedEffect(authState.isAuthenticated, authState.isEmailVerified, authState.user?.role) {
        // This will run whenever auth state changes
        println("Auth State Changed - Authenticated: ${authState.isAuthenticated}, Email Verified: ${authState.isEmailVerified}, User: ${authState.user?.role}")
    }

    // Determine start destination based on auth state - use derivedStateOf for stability
    val startDestination by remember(authState.isAuthenticated, authState.isEmailVerified, authState.user?.role) {
        derivedStateOf {
            when {
                !authState.isAuthenticated -> Screen.Login.route
                !authState.isEmailVerified -> Screen.EmailVerification.route
                else -> when (authState.user?.role) {
                    UserRole.STUDENT -> Screen.StudentHome.route
                    UserRole.CLUB_LEADER -> Screen.ClubLeaderHome.route
                    UserRole.ADMIN -> Screen.AdminHome.route
                    else -> Screen.Login.route
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { user ->
                    val destination = when (user.role) {
                        UserRole.STUDENT -> Screen.StudentHome.route
                        UserRole.CLUB_LEADER -> Screen.ClubLeaderHome.route
                        UserRole.ADMIN -> Screen.AdminHome.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                onVerificationComplete = { user ->
                    val destination = when (user.role) {
                        UserRole.STUDENT -> Screen.StudentHome.route
                        UserRole.CLUB_LEADER -> Screen.ClubLeaderHome.route
                        UserRole.ADMIN -> Screen.AdminHome.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                },
                onLogout = {
                    // Use the proper signOut method from ViewModel
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Student Navigation Graph
        studentNavGraph(navController, authState.user?.id ?: "")

        // Club Leader Navigation Graph
        clubLeaderNavGraph(
            navController = navController,
            userId = authState.user?.id ?: "",
            clubId = authState.user?.clubId ?: ""
        )

        // Admin Navigation Graph
        adminNavGraph(
            navController = navController,
            userId = authState.user?.id ?: ""
        )
    }
}

// Extension functions for navigation
fun NavHostController.navigateToClubDetail(clubId: String) {
    navigate(Screen.ClubDetail.createRoute(clubId))
}

fun NavHostController.navigateToEventDetail(eventId: String) {
    navigate(Screen.EventDetail.createRoute(eventId))
}

// Admin navigation extensions
fun NavHostController.navigateToCreateClubAdmin() {
    navigate(Screen.CreateClubAdmin.route)
}