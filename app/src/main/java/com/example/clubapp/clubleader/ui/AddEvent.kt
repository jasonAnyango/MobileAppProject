package com.example.clubapp.clubleader.ui

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
import com.example.clubapp.clubleader.viewmodel.AddEventViewModel
import com.example.clubapp.clubleader.viewmodel.AddEventViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddEventScreen(
    navController: NavHostController,
) {
    val firestore = remember { FirebaseFirestore.getInstance() }

    val factory = remember {
        AddEventViewModelFactory(db = firestore)
    }

    val viewModel: AddEventViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    // Form state variables
    var eventTitle by remember { mutableStateOf("") }
    var eventDateTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Side effect to handle navigation after successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            // Navigate back to the Events list and pop this screen
            navController.navigate(ClubLeaderScreen.Events.route) {
                // Ensure AddEvent screen is removed from the back stack
                popUpTo(ClubLeaderScreen.Events.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { AddEventBottomNav(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "Create Event",
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

            // Error Message Display
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

            if (!isFormEnabled && uiState.clubId.isEmpty() && !uiState.isSaving) {
                Text(
                    "Error: Your leader account is not linked to a club. Please contact support.",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.isSaving) CircularProgressIndicator()
            }

            // ------------------ INPUT FIELDS ------------------

            // Event Title
            InputField(
                value = eventTitle,
                onValueChange = { eventTitle = it },
                label = "Event Title (Required)", // <-- FIXED: Simple String
                icon = Icons.Default.NewLabel,
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Date & Time
            InputField(
                value = eventDateTime,
                onValueChange = { eventDateTime = it },
                label = "Date and Time (YYYY-MM-DD)", // <-- FIXED: Simple String
                icon = Icons.Default.Schedule,
                enabled = isFormEnabled
            )
            // NOTE: In a real app, this should be replaced by a Date/Time Picker component.
            Text(
                "Format: YYYY-MM-DD. Time can be added manually after the date if needed.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Location
            InputField(
                value = location,
                onValueChange = { location = it },
                label = "Location / Venue", // <-- FIXED: Simple String
                icon = Icons.Default.LocationOn,
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Description
            InputField(
                value = description,
                onValueChange = { description = it },
                label = "Detailed Description", // <-- FIXED: Simple String
                icon = Icons.Default.Description,
                minLines = 6, // Larger input area for description
                enabled = isFormEnabled
            )
            Spacer(modifier = Modifier.height(32.dp))

            // ------------------ PUBLISH EVENT BUTTON ------------------
            Button(
                onClick = {
                    viewModel.saveEvent(eventTitle, eventDateTime, location, description)
                },
                // Ensure required fields are not empty before enabling
                enabled = isFormEnabled && eventTitle.isNotBlank() && eventDateTime.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Publish Event", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- UPDATED REUSABLE COMPOSABLES ---

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String, // <-- FIXED: Changed to simple String
    icon: ImageVector,
    minLines: Int = 1,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) }, // <-- Now passes the String variable to Text
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        singleLine = minLines == 1,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
        )
    )
}


@Composable
fun AddEventBottomNav(navController: NavHostController) {
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