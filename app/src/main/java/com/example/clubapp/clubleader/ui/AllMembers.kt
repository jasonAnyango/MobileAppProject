package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import kotlin.random.Random

// Removed placeholder data classes, relying on Models and UiState

@Composable
fun MembersScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val firestore = remember { FirebaseFirestore.getInstance() }

    val factory = remember {
        MembersViewModelFactory(db = firestore)
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
                style = MaterialTheme.typography.headlineLarge, // Slightly larger title
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Dynamic club name used here
            if (uiState.clubName.isNotEmpty() && !uiState.isLoading) {
                Text(
                    text = "${uiState.clubName} Management",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary // Highlight club name
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
                Spacer(modifier = Modifier.width(8.dp)) // Space between tabs
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
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) // Better loading indicator for full list
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading member data...", color = MaterialTheme.colorScheme.primary)
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { // Increased spacing
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

// -----------------------------------------------------------------------------------
// --- LIST ITEMS (Updated with Avatar) ---
// -----------------------------------------------------------------------------------

@Composable
fun MemberListItem(member: ClubMember) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), // Lighter surface color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // 💡 USER AVATAR IMPLEMENTATION 💡
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder: Use a generic Person icon or first letter of the name
                val firstLetter = member.user.fullName.firstOrNull()?.toString() ?: "?"

                Text(
                    text = firstLetter,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                // If you had a real image URL, you would use Coil/Glide here.
            }

            Spacer(modifier = Modifier.width(16.dp)) // Increased space

            Column {
                Text(
                    text = member.user.fullName,
                    fontWeight = FontWeight.Bold, // Made name bolder
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Highlight the Club Role
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = member.clubRole,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Display the global user role (Student/Admin)
                Text(
                    text = "System Role: ${member.user.role}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        // Use a prominent color to demand attention for pending actions
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(16.dp) // Increased padding
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.applicantName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Request status pill
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.error, // Use error color for urgent action
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(
                        text = "PENDING REVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { // Increased button spacing
                // Approve Button (Primary color for affirmation)
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Approve", modifier = Modifier.size(20.dp))
                }

                // Reject Button (Error color for rejection/deletion)
                IconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// --- Reused Composable functions (Unchanged) ---
// -----------------------------------------------------------------------------------

@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        modifier = modifier
            .height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp) // Removed vertical padding as height is fixed
            )
        }
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