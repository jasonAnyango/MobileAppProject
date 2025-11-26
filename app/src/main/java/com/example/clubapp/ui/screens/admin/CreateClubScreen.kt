package com.example.clubapp.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubapp.data.model.Club
import com.example.clubapp.data.model.ClubStatus
import com.example.clubapp.data.model.User
import com.example.clubapp.data.model.UserRole
import com.example.clubapp.viewmodel.admin.AdminViewModel
import com.google.firebase.Timestamp
import com.google.firebase.Timestamp.now

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onCreateSuccess: () -> Unit
) {
    var clubName by remember { mutableStateOf("") }
    var clubDescription by remember { mutableStateOf("") }
    var clubCategory by remember { mutableStateOf("") }
    var selectedLeader by remember { mutableStateOf<User?>(null) }
    var showLeaderSelection by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    // Available leaders (users with CLUB_LEADER role or STUDENTS who can be promoted)
    val availableLeaders = state.users.filter { it.role == UserRole.CLUB_LEADER } +
            state.users.filter { it.role == UserRole.STUDENT }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create New Club") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Club Creation Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Club Name
                OutlinedTextField(
                    value = clubName,
                    onValueChange = { clubName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Club Name") },
                    placeholder = { Text("Enter club name") },
                    singleLine = true
                )

                // Club Description
                OutlinedTextField(
                    value = clubDescription,
                    onValueChange = { clubDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { Text("Club Description") },
                    placeholder = { Text("Enter club description") },
                    singleLine = false
                )

                // Club Category
                OutlinedTextField(
                    value = clubCategory,
                    onValueChange = { clubCategory = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category") },
                    placeholder = { Text("e.g., Sports, Academic, Arts") },
                    singleLine = true
                )

                // Leader Selection
                Column {
                    Text(
                        text = "Club Leader",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (selectedLeader != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = selectedLeader!!.fullName,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = selectedLeader!!.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Current Role: ${selectedLeader!!.role.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { selectedLeader = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showLeaderSelection = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select Club Leader")
                        }
                    }
                }

                // Quick Category Suggestions
                Text(
                    text = "Popular Categories:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Sports", "Academic", "Arts", "Technology", "Cultural", "Social").forEach { category ->
                        FilterChip(
                            selected = clubCategory == category,
                            onClick = { clubCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            // Create Button
            Button(
                onClick = {
                    if (clubName.isNotEmpty() && clubDescription.isNotEmpty() && selectedLeader != null) {
                        isLoading = true

                        val newClub = Club(
                            name = clubName,
                            description = clubDescription,
                            category = clubCategory,
                            leaderId = selectedLeader!!.id,
                            leaderName = selectedLeader!!.fullName,
                            status = ClubStatus.APPROVED, // Admin-created clubs are auto-approved
                            memberCount = 0,
                            memberIds = emptyList(),
                            coverImageUrl = "",
                            additionalImages = emptyList(),
                            createdAt = now(),
                            updatedAt = now()
                        )

                        viewModel.createClub(newClub) { success, message ->
                            isLoading = false
                            if (success) {
                                onCreateSuccess()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = clubName.isNotEmpty() && clubDescription.isNotEmpty() && selectedLeader != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Club", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Leader Selection Dialog
        if (showLeaderSelection) {
            AlertDialog(
                onDismissRequest = { showLeaderSelection = false },
                title = { Text("Select Club Leader") },
                text = {
                    Column(
                        modifier = Modifier.height(300.dp)
                    ) {
                        if (availableLeaders.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No available users to assign as leader")
                            }
                        } else {
                            LazyColumn {
                                items(availableLeaders) { user ->
                                    Card(
                                        onClick = {
                                            selectedLeader = user
                                            showLeaderSelection = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = user.fullName,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = user.email,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Role: ${user.role.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (user.role == UserRole.STUDENT) {
                                                Text(
                                                    text = "Will be promoted to Club Leader",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLeaderSelection = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}