package com.example.clubapp.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubapp.viewmodel.admin.AdminViewModel
import com.example.clubapp.viewmodel.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    viewModel: AdminViewModel,
    onNavigateToManageUsers: () -> Unit,
    onNavigateToManageClubs: () -> Unit,
    onNavigateToClubApproval: () -> Unit,
    onNavigateToManageEvents: () -> Unit,
    onNavigateToSystemAnnouncements: () -> Unit,
    onNavigateToCreateClub: () -> Unit, // NEW: Club creation navigation
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val authViewModel: AuthViewModel = koinViewModel()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Admin Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                actions = {
                    IconButton(onClick = {
                        // Use the proper signOut method
                        authViewModel.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateClub,
                icon = { Icon(Icons.Default.Add, contentDescription = "Create Club") },
                text = { Text("Create Club") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Statistics Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Users",
                    value = state.users.size.toString(),
                    icon = Icons.Default.People
                )
                StatCard(
                    title = "Clubs",
                    value = state.clubs.size.toString(),
                    icon = Icons.Default.Groups
                )
                StatCard(
                    title = "Pending",
                    value = state.pendingClubs.size.toString(),
                    icon = Icons.Default.Pending
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Admin Actions Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(adminActions) { action ->
                    AdminActionCard(
                        action = action,
                        onClick = {
                            when (action.title) {
                                "Manage Users" -> onNavigateToManageUsers()
                                "Manage Clubs" -> onNavigateToManageClubs()
                                "Club Approval" -> onNavigateToClubApproval()
                                "Manage Events" -> onNavigateToManageEvents()
                                "Announcements" -> onNavigateToSystemAnnouncements()
                                "Create Club" -> onNavigateToCreateClub() // NEW
                            }
                        }
                    )
                }
            }
        }
    }
}

data class AdminAction(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

val adminActions = listOf(
    AdminAction(
        title = "Manage Users",
        description = "View and manage all users",
        icon = Icons.Default.People,
        color = Color(0xFF4CAF50)
    ),
    AdminAction(
        title = "Manage Clubs",
        description = "Manage all clubs",
        icon = Icons.Default.Groups,
        color = Color(0xFF2196F3)
    ),
    AdminAction(
        title = "Club Approval",
        description = "Approve or reject clubs",
        icon = Icons.Default.ThumbUp,
        color = Color(0xFFFF9800)
    ),
    AdminAction(
        title = "Manage Events",
        description = "Manage all events",
        icon = Icons.Default.Event,
        color = Color(0xFF9C27B0)
    ),
    AdminAction(
        title = "Announcements",
        description = "Send system announcements",
        icon = Icons.Default.Announcement,
        color = Color(0xFFF44336)
    ),
    AdminAction( // NEW: Create Club action
        title = "Create Club",
        description = "Create a new club",
        icon = Icons.Default.Add,
        color = Color(0xFF009688)
    )
)

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.size(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 12.sp)
        }
    }
}

@Composable
fun AdminActionCard(action: AdminAction, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = action.color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = action.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                fontWeight = FontWeight.Bold,
                color = action.color
            )
            Text(
                text = action.description,
                fontSize = 12.sp,
                color = action.color.copy(alpha = 0.8f)
            )
        }
    }
}