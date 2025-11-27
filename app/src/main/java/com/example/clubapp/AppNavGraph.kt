package com.example.clubapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.get // <--- 1. Import for Koin Injection

// Screen Imports
import com.example.clubapp.auth.LoginScreen
import com.example.clubapp.auth.RegisterScreen
import com.example.clubapp.admin.navigation.AdminNavGraph
import com.example.clubapp.student.navigation.StudentNavGraph
import com.example.clubapp.student.data.StudentRepository // <--- 2. Import Repository
import com.example.clubapp.clubleader.navigation.ClubLeaderNavGraph

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val ADMIN_ROOT = "admin_portal"
    const val STUDENT_ROOT = "student_portal"
    const val LEADER_ROOT = "leader_portal"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {

    fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        navController.navigate(AppRoutes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {

        // 1. LOGIN
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) },
                onLoginSuccess = { role ->
                    // Logic: Admins go to Admin.
                    // Everyone else (Students & Leaders) goes to Student Portal first.
                    // Leaders can then click the "Star" button to switch.
                    val destination = if (role == "Admin") {
                        AppRoutes.ADMIN_ROOT
                    } else {
                        AppRoutes.STUDENT_ROOT
                    }

                    navController.navigate(destination) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 2. REGISTER
        composable(AppRoutes.REGISTER) {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }

        // 3. ADMIN PORTAL
        composable(AppRoutes.ADMIN_ROOT) {
            AdminNavGraph(onLogout = { performLogout() })
        }

        // 4. STUDENT PORTAL (Updated)
        composable(AppRoutes.STUDENT_ROOT) {
            // Inject the repo here so the Graph doesn't need to worry about Koin
            val studentRepo: StudentRepository = get()

            StudentNavGraph(
                studentRepository = studentRepo,
                onLogout = { performLogout() },
                onSwitchToLeader = {
                    // This is the magic switch!
                    navController.navigate(AppRoutes.LEADER_ROOT)
                }
            )
        }

        // 5. CLUB LEADER PORTAL (Kept as requested)
        composable(AppRoutes.LEADER_ROOT) {
            ClubLeaderNavGraph(
                onLogout = { performLogout() }
                // Optional: Add a way to go back to Student view?
                // onSwitchToStudent = { navController.popBackStack() }
            )
        }
    }
}