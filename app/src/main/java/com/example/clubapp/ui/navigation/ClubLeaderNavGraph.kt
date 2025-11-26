package com.example.clubapp.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.clubapp.ui.screens.clubleader.*

fun NavGraphBuilder.clubLeaderNavGraph(
    navController: NavHostController,
    userId: String,
    clubId: String
) {
    // Club Leader Home
    composable(Screen.ClubLeaderHome.route) {
        ClubLeaderHomeScreen(
            userId = userId,
            clubId = clubId,
            onNavigateToManageClub = {
                navController.navigate(Screen.ManageClub.route)
            },
            onNavigateToManageEvents = {
                navController.navigate(Screen.ManageEventsLeader.route)
            },
            onNavigateToMemberRequests = {
                navController.navigate(Screen.MemberRequests.route)
            },
            onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // Manage Club
    composable(Screen.ManageClub.route) {
        ManageClubScreen(
            userId = userId,
            clubId = clubId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Manage Events
    composable(Screen.ManageEventsLeader.route) {
        ManageEventsScreen(
            userId = userId,
            clubId = clubId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCreateEvent = {
                navController.navigate(Screen.CreateEvent.route)
            },
            onNavigateToEditEvent = { eventId ->
                navController.navigate(Screen.EditEvent.createRoute(eventId))
            }
        )
    }

    // Create Event
    composable(Screen.CreateEvent.route) {
        CreateEventScreen(
            userId = userId,
            clubId = clubId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Edit Event
    composable(
        route = Screen.EditEvent.route,
        arguments = listOf(
            navArgument("eventId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
        EditEventScreen(
            eventId = eventId,
            userId = userId,
            clubId = clubId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Member Requests
    composable(Screen.MemberRequests.route) {
        MemberRequestsScreen(
            userId = userId,
            clubId = clubId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}