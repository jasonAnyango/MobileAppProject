package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen
import com.example.clubapp.clubleader.viewmodel.AddAnnouncementViewModel
import com.example.clubapp.clubleader.viewmodel.AddAnnouncementViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddAnnouncementScreen(
    navController: NavHostController
) {
    val firestore = remember { FirebaseFirestore.getInstance() }

    // 💡 Simplified Factory instantiation 💡
    val factory = remember {
        AddAnnouncementViewModelFactory(db = firestore)
    }

    val viewModel: AddAnnouncementViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    // Form state variables
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Side effect to handle navigation after successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            // Navigate back to the Announcements list
            navController.navigate(ClubLeaderScreen.Announcements.route) {
                // Remove this screen from the back stack
                popUpTo(ClubLeaderScreen.Announcements.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { AddAnnouncementBottomNav(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "New Announcement for ${uiState.clubName}", // Uses dynamic club name
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Display
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Check if club data is available to enable the form
            val isFormEnabled = uiState.clubId.isNotEmpty() && !uiState.isSaving

            if (!isFormEnabled && uiState.clubId.isEmpty()) {
                Text(
                    "Error: Your leader account is not linked to a club. Cannot publish announcements.",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }


            // ------------------ TITLE INPUT ------------------
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                enabled = isFormEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ------------------ DESCRIPTION INPUT ------------------
            BasicTextField(
                value = description,
                onValueChange = { description = it },
                label = "Announcement Description",
                minLines = 6,
                enabled = isFormEnabled
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ------------------ PUBLISH BUTTON ------------------
            Button(
                onClick = {
                    viewModel.saveAnnouncement(title, description)
                },
                enabled = isFormEnabled, // Disable while saving or if clubId is missing
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddAnnouncementBottomNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Dashboard.route,
            onClick = { navController.navigate(ClubLeaderScreen.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Events.route,
            onClick = { navController.navigate(ClubLeaderScreen.Events.route) },
            icon = { Icon(Icons.Default.Event, contentDescription = null) },
            label = { Text("Events") }
        )

        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Members.route,
            onClick = { navController.navigate(ClubLeaderScreen.Members.route) },
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Members") }
        )

        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Announcements.route,
            onClick = { navController.navigate(ClubLeaderScreen.Announcements.route) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text("Announce") }
        )
    }
}