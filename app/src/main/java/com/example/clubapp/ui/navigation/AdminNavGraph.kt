package com.example.clubapp.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.clubapp.ui.screens.admin.*
import com.example.clubapp.viewmodel.admin.AdminViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.adminNavGraph(
    navController: NavHostController,
    userId: String
) {
    // Admin Home
    composable(Screen.AdminHome.route) {
        val adminViewModel: AdminViewModel = koinViewModel()
        AdminHomeScreen(
            viewModel = adminViewModel,
            onNavigateToManageUsers = {
                navController.navigate(Screen.ManageUsers.route)
            },
            onNavigateToManageClubs = {
                navController.navigate(Screen.ManageClubs.route)
            },
            onNavigateToClubApproval = {
                navController.navigate(Screen.ClubApproval.route)
            },
            onNavigateToManageEvents = {
                navController.navigate(Screen.ManageEventsAdmin.route)
            },
            onNavigateToSystemAnnouncements = {
                navController.navigate(Screen.SystemAnnouncements.route)
            },
            onNavigateToCreateClub = {
                navController.navigate(Screen.CreateClubAdmin.route)
            },
            onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // Manage Users
    composable(Screen.ManageUsers.route) {
        val adminViewModel: AdminViewModel = koinViewModel()
        ManageUsersScreen(
            viewModel = adminViewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Manage Clubs
    composable(Screen.ManageClubs.route) {
        val adminViewModel: AdminViewModel = koinViewModel()
        ManageClubsScreen(
            viewModel = adminViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCreateClub = {
                navController.navigate(Screen.CreateClubAdmin.route)
            }
        )
    }

    // Club Approval
    composable(Screen.ClubApproval.route) {
        val adminViewModel: AdminViewModel = koinViewModel()
        ClubApprovalScreen(
            viewModel = adminViewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Manage Events (Admin version)
    composable(Screen.ManageEventsAdmin.route) {
        val adminViewModel: AdminViewModel = koinViewModel()
        ManageEventsScreen(
            viewModel = adminViewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // System Announcements
    composable(Screen.SystemAnnouncements.route) {
        val adminViewModel: AdminViewModel = koinViewModel()
        SystemAnnouncementsScreen(
            viewModel = adminViewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Create Club (Admin version)
    composable(Screen.CreateClubAdmin.route) {
        val adminViewModel: AdminViewModel = koinViewModel()
        CreateClubScreen(
            viewModel = adminViewModel,
            onNavigateBack = { navController.popBackStack() },
            onCreateSuccess = {
                navController.popBackStack()
            }
        )
    }
}