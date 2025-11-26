package com.example.clubapp.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.clubapp.ui.screens.student.*

fun NavGraphBuilder.studentNavGraph(
    navController: NavHostController,
    userId: String
) {
    // Student Home
    composable(Screen.StudentHome.route) {
        StudentHomeScreen(
            userId = userId,
            onNavigateToClubDetail = { clubId ->
                navController.navigateToClubDetail(clubId)
            },
            onNavigateToEventDetail = { eventId ->
                navController.navigateToEventDetail(eventId)
            },
            onNavigateToClubBrowse = {
                navController.navigate(Screen.ClubBrowse.route)
            },
            onNavigateToProfile = {
                navController.navigate(Screen.StudentProfile.route)
            },
            onNavigateToNotifications = {
                navController.navigate(Screen.Notifications.route)
            }
        )
    }

    // Notifications
    composable(Screen.Notifications.route) {
        NotificationScreen(
            userId = userId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Club Browse
    composable(Screen.ClubBrowse.route) {
        ClubBrowseScreen(
            userId = userId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToClubDetail = { clubId ->
                navController.navigateToClubDetail(clubId)
            }
        )
    }

    // Club Detail
    composable(
        route = Screen.ClubDetail.route,
        arguments = listOf(
            navArgument("clubId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val clubId = backStackEntry.arguments?.getString("clubId") ?: return@composable
        ClubDetailScreen(
            clubId = clubId,
            userId = userId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEventDetail = { eventId ->
                navController.navigateToEventDetail(eventId)
            }
        )
    }

    // Event Detail
    composable(
        route = Screen.EventDetail.route,
        arguments = listOf(
            navArgument("eventId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
        EventDetailScreen(
            eventId = eventId,
            userId = userId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Student Profile
    composable(Screen.StudentProfile.route) {
        StudentProfileScreen(
            userId = userId,
            onNavigateBack = { navController.popBackStack() },
            onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}