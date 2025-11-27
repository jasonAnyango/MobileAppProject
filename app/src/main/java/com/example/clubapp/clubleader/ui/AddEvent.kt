package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen
import com.example.clubapp.clubleader.viewmodel.AddEventViewModel
import com.example.clubapp.clubleader.viewmodel.AddEventViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore

@Composable
fun AddEventScreen(
    navController: NavHostController,
) {
    // ⚠️ HARDCODED DATA FOR TESTING ⚠️
    val hardcodedClubId = "cQqHqt95G2xmiCRxlrP2"
    val hardcodedClubName = "IEEE Student Branch" // Assuming a name for the club ID

    val firestore = remember { FirebaseFirestore.getInstance() }
    val factory = remember {
        AddEventViewModelFactory(
            clubId = hardcodedClubId,
            clubName = hardcodedClubName,
            db = firestore
        )
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
                // Clears the stack up to the Events screen (inclusive or exclusive depending on your desired flow)
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
                .padding(16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "Add Event for ${uiState.clubName}",
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

            // ------------------ INPUT FIELDS ------------------
            BasicTextField(
                value = eventTitle,
                onValueChange = { eventTitle = it },
                label = "Event Title"
            )
            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = eventDateTime,
                onValueChange = { eventDateTime = it },
                label = "Date (YYYY-MM-DD)" // Encouraging a sortable format
            )
            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location"
            )
            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                minLines = 4
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ------------------ PUBLISH EVENT BUTTON ------------------
            Button(
                onClick = {
                    viewModel.saveEvent(eventTitle, eventDateTime, location, description)
                },
                enabled = !uiState.isSaving, // Disable while saving
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
                    Text("Add Event", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Custom Composable to mimic the behavior of a labeled text field without custom styling
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun AddEventBottomNav(navController: NavHostController) {
    // Re-using the NavBar is usually fine, but ensure the current route logic is sound if navigating off-path
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