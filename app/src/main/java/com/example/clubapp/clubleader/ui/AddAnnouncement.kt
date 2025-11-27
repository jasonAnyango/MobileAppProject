package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

    val factory = remember {
        AddAnnouncementViewModelFactory(db = firestore)
    }

    val viewModel: AddAnnouncementViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.navigate(ClubLeaderScreen.Announcements.route) {
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // ... (Title and Error Display) ...

            Text(
                text = "New Announcement",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "for ${uiState.clubName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            uiState.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Check if club data is available to enable the form
            val isFormEnabled = uiState.clubId.isNotEmpty() && !uiState.isSaving

            if (uiState.clubId.isEmpty() && !uiState.isSaving) {
                Text(
                    "Error: Your leader account is not linked to a club. Cannot publish announcements.",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Only show general loading if the club name is the default "Loading..." text
                if (uiState.clubName == "Loading...") CircularProgressIndicator()
            }

            // ------------------ INPUT FIELDS ------------------

            InputField(
                value = title,
                onValueChange = { title = it },
                label = "Announcement Title (Required)", // <--- FIXED HERE
                icon = Icons.Default.Campaign,
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))
            InputField(
                value = description,
                onValueChange = { description = it },
                label = "Message / Details", // <--- FIXED HERE
                icon = Icons.Default.Message,
                minLines = 8,
                enabled = isFormEnabled
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ------------------ PUBLISH BUTTON ------------------
            Button(
                onClick = {
                    viewModel.saveAnnouncement(title, description)
                },
                enabled = isFormEnabled && title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                // This is the correct way to handle the saving state display
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Publish Announcement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// --- SUPPORTING COMPOSABLES ---
// ---------------------------------------------------------------------


@Composable
fun AddAnnouncementBottomNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(selected = currentRoute == ClubLeaderScreen.Dashboard.route, onClick = { navController.navigate(ClubLeaderScreen.Dashboard.route) }, icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text("Dashboard") })
        NavigationBarItem(selected = currentRoute == ClubLeaderScreen.Events.route, onClick = { navController.navigate(ClubLeaderScreen.Events.route) }, icon = { Icon(Icons.Default.Event, contentDescription = null) }, label = { Text("Events") })
        NavigationBarItem(selected = currentRoute == ClubLeaderScreen.Members.route, onClick = { navController.navigate(ClubLeaderScreen.Members.route) }, icon = { Icon(Icons.Default.Group, contentDescription = null) }, label = { Text("Members") })
        NavigationBarItem(selected = currentRoute == ClubLeaderScreen.Announcements.route, onClick = { navController.navigate(ClubLeaderScreen.Announcements.route) }, icon = { Icon(Icons.Default.Notifications, contentDescription = null) }, label = { Text("Announce") })
    }
}