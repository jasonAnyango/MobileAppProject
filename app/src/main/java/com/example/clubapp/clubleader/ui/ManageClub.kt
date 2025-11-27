package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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

        // ---------------- LOADING STATE ----------------
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp) // Increased padding
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ---------------- PAGE TITLE ----------------
            Text(
                text = "Edit Club Profile",
                style = MaterialTheme.typography.headlineLarge, // Larger title
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = uiState.clubName,
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

            val isFormEditable = !uiState.isSaving && uiState.clubId.isNotEmpty()

            // ---------------- CLUB LOGO SECTION ----------------
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    // Subtle elevated look
                    shadowElevation = 4.dp,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.size(120.dp) // Slightly larger logo area
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.club_default), // Placeholder
                        contentDescription = "Club Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* TODO: Open image picker and update ViewModel */ },
                    enabled = isFormEditable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Change Logo")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- INPUT FIELDS ----------------

            // CLUB NAME
            InputField(
                value = clubName,
                onValueChange = { clubName = it },
                label = "Club Name", // <-- FIXED
                icon = Icons.Default.Label,
                enabled = isFormEditable
            )
            Spacer(modifier = Modifier.height(16.dp))

            // CLUB DESCRIPTION
            InputField(
                value = clubDescription,
                onValueChange = { clubDescription = it },
                label = "Club Description", // <-- FIXED
                icon = Icons.Default.Description,
                minLines = 6,
                enabled = isFormEditable
            )
            Spacer(modifier = Modifier.height(16.dp))

            // MEETING TIME
            InputField(
                value = meetingTime,
                onValueChange = { meetingTime = it },
                label = "Meeting Time / Schedule", // <-- FIXED
                icon = Icons.Default.Schedule,
                enabled = isFormEditable
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ---------------- SAVE BUTTON ----------------
            Button(
                onClick = {
                    viewModel.saveClubDetails(clubName, clubDescription, meetingTime)
                },
                enabled = isFormEditable, // Disable while saving or if no valid club ID
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
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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