package com.example.clubapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clubapp.auth.LoginScreen
import com.example.clubapp.auth.RegisterScreen
import com.example.clubapp.admin.navigation.AdminNavGraph
import com.example.clubapp.clubleader.navigation.ClubLeaderNavGraph
import com.google.firebase.auth.FirebaseAuth

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ADMIN_ROOT = "admin_dashboard"
    const val STUDENT_ROOT = "student_dashboard"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) },
                onLoginSuccess = { role ->
                    val destination = if (role == "Admin") AppRoutes.ADMIN_ROOT else AppRoutes.STUDENT_ROOT
                    navController.navigate(destination) { popUpTo(AppRoutes.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }

        composable(AppRoutes.ADMIN_ROOT) {
            // Pass the Logout Logic to the Admin Graph
            AdminNavGraph(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.ADMIN_ROOT) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.STUDENT_ROOT) {
            // Pass the Logout Logic to the Student/Leader Graph
            ClubLeaderNavGraph(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.STUDENT_ROOT) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}