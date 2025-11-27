package com.example.clubapp.clubleader.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen
import com.example.clubapp.clubleader.viewmodel.ManageClubViewModel
import com.example.clubapp.clubleader.viewmodel.ManageClubViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageClubScreen(navController: NavHostController) {

    val firestore = remember { FirebaseFirestore.getInstance() }

    // 💡 Simplified Factory instantiation 💡
    val factory = remember { ManageClubViewModelFactory(db = firestore) }

    val viewModel: ManageClubViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    // Local state to hold form input (initialized from UiState)
    var clubName by remember { mutableStateOf("") }
    var clubDescription by remember { mutableStateOf("") }
    var meetingTime by remember { mutableStateOf("") }

    // Update local state when ViewModel state changes (i.e., when data is loaded)
    LaunchedEffect(uiState.clubName, uiState.clubDescription, uiState.meetingTime) {
        // Only initialize local state once after data is successfully loaded and not when it's just a blank state
        if (!uiState.isLoading && uiState.error == null) {
            clubName = uiState.clubName
            clubDescription = uiState.clubDescription
            meetingTime = uiState.meetingTime
        }
    }

    // State for Snackbar feedback
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success feedback
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Club details saved successfully!",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Attach SnackbarHost
        bottomBar = { ManageClubBottomNav(navController) }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ---------------- PAGE TITLE ----------------
            Text(
                text = "Edit Club Profile",
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


            // ---------------- CLUB NAME ----------------
            OutlinedTextField(
                value = clubName,
                onValueChange = { clubName = it },
                label = { Text("Club Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- CLUB DESCRIPTION ----------------
            OutlinedTextField(
                value = clubDescription,
                onValueChange = { clubDescription = it },
                label = { Text("Club Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- MEETING TIME ----------------
            OutlinedTextField(
                value = meetingTime,
                onValueChange = { meetingTime = it },
                label = { Text("Meeting Time") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- CLUB LOGO ----------------
            Image(
                painter = painterResource(id = R.drawable.club_default), // Placeholder
                contentDescription = "Club Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO: Open image picker and update ViewModel */ },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !uiState.isSaving
            ) {
                Text("Change Logo")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------------- SAVE BUTTON ----------------
            Button(
                onClick = {
                    viewModel.saveClubDetails(clubName, clubDescription, meetingTime)
                },
                enabled = !uiState.isSaving && uiState.clubId.isNotEmpty(), // Disable while saving or if no valid club ID
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
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun ManageClubBottomNav(navController: NavHostController) {
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