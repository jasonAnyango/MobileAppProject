package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen
import com.example.clubapp.clubleader.state.MembersUiState
import com.example.clubapp.clubleader.viewmodel.MembersViewModel
import com.example.clubapp.clubleader.viewmodel.MembersViewModelFactory
import com.example.clubapp.clubleader.state.ClubMember
import com.example.clubapp.model.ClubRegistration
import com.google.firebase.firestore.FirebaseFirestore

// Removed placeholder data classes, relying on Models and UiState

@Composable
fun MembersScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    // ⚠️ HARDCODED CLUB ID FOR TESTING WORKFLOW ⚠️
    val hardcodedClubId = "cQqHqt95G2xmiCRxlrP2"

    val firestore = remember { FirebaseFirestore.getInstance() }
    val factory = remember {
        MembersViewModelFactory(
            clubId = hardcodedClubId,
            db = firestore
        )
    }
    val viewModel: MembersViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { MembersBottomNav(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Text(
                text = "Members",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.clubName.isNotEmpty() && !uiState.isLoading) {
                Text(
                    text = "${uiState.clubName} Management",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------- TABS -------------------
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabButton(
                    title = "All Members (${uiState.allMembers.size})",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Pending (${uiState.pendingRequests.size})",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------- DATA LIST & LOADING/ERROR -------------------

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedTab == 0) {
                        items(uiState.allMembers) { member ->
                            MemberListItem(member)
                        }
                    } else {
                        items(uiState.pendingRequests) { request ->
                            PendingRequestItem(
                                request = request,
                                onApprove = { viewModel.approveRequest(request.id) },
                                onReject = { viewModel.rejectRequest(request.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------- LIST ITEMS -------------------

@Composable
fun MemberListItem(member: ClubMember) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
        ) {
            // Placeholder Image (Should load actual user profile image in production)
            Image(
                painter = painterResource(id = R.drawable.profile), // Placeholder
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                // Display the user's full name
                Text(member.user.fullName, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                // Display the club-specific role (Officer role or "Member")
                Text(member.clubRole, color = MaterialTheme.colorScheme.primary)
                // Display the global user role (Student/Admin)
                Text(member.user.role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PendingRequestItem(
    request: ClubRegistration,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.applicantName, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("Requested to join", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Approve Button
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Approve", modifier = Modifier.size(20.dp))
                }

                // Reject Button
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// --- Reused Composable functions ---

@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = title,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun MembersBottomNav(navController: NavHostController) {
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