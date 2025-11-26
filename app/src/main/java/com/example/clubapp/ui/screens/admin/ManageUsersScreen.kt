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
import com.example.clubapp.data.model.User
import com.example.clubapp.data.model.UserRole
import com.example.clubapp.viewmodel.admin.AdminViewModel
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showPromoteDialog by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf<User?>(null) }
    var showClubSelectionDialog by remember { mutableStateOf<User?>(null) }
    var selectedClubId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Users") },
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
        ) {
            // Filter chips
            var selectedFilter by remember { mutableStateOf("All") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Students", "Leaders", "Admins").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            // Users list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredUsers = when (selectedFilter) {
                    "Students" -> state.users.filter { it.role == UserRole.STUDENT }
                    "Leaders" -> state.users.filter { it.role == UserRole.CLUB_LEADER }
                    "Admins" -> state.users.filter { it.role == UserRole.ADMIN }
                    else -> state.users
                }

                if (filteredUsers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No users found")
                        }
                    }
                } else {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onPromoteClick = { showPromoteDialog = user },
                            onDeleteClick = { showDeleteDialog = user }
                        )
                    }
                }
            }
        }

        // Promote Dialog
        showPromoteDialog?.let { user ->
            PromoteUserDialog(
                user = user,
                onDismiss = { showPromoteDialog = null },
                onPromoteToLeader = { showClubSelectionDialog = user },
                onPromoteToAdmin = {
                    viewModel.promoteToAdmin(user.id) { success, message ->
                        if (success) viewModel.clearMessages()
                    }
                    showPromoteDialog = null
                }
            )
        }

        // Club Selection Dialog for Leader Promotion
        showClubSelectionDialog?.let { user ->
            ClubSelectionDialog(
                clubs = state.clubs.filter { it.status.name == "APPROVED" },
                onDismiss = {
                    showClubSelectionDialog = null
                    selectedClubId = ""
                },
                onClubSelected = { clubId ->
                    viewModel.promoteToClubLeader(user.id, clubId) { success, message ->
                        if (success) viewModel.clearMessages()
                    }
                    showClubSelectionDialog = null
                    selectedClubId = ""
                }
            )
        }

        // Delete Dialog
        showDeleteDialog?.let { user ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete User") },
                text = { Text("Are you sure you want to delete ${user.fullName}? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteUser(user.id) { success, message ->
                                if (success) viewModel.clearMessages()
                            }
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onPromoteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Role: ${user.role.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (user.studentId.isNotEmpty()) {
                        Text(
                            text = "Student ID: ${user.studentId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (user.clubId != null) {
                        Text(
                            text = "Club ID: ${user.clubId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    if (user.role != UserRole.ADMIN) {
                        IconButton(onClick = onPromoteClick) {
                            Icon(Icons.Default.Star, contentDescription = "Promote")
                        }
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun PromoteUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onPromoteToLeader: () -> Unit,
    onPromoteToAdmin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Promote ${user.fullName}") },
        text = {
            Column {
                Text("Select new role for ${user.fullName}:")
                Spacer(modifier = Modifier.height(8.dp))
                if (user.role == UserRole.STUDENT) {
                    Text("• Club Leader: Can manage one club")
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text("• Admin: Full system access")
            }
        },
        confirmButton = {
            Column {
                if (user.role == UserRole.STUDENT) {
                    TextButton(
                        onClick = onPromoteToLeader
                    ) {
                        Text("Promote to Club Leader")
                    }
                }
                TextButton(
                    onClick = onPromoteToAdmin
                ) {
                    Text("Promote to Admin")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClubSelectionDialog(
    clubs: List<com.example.clubapp.data.model.Club>,
    onDismiss: () -> Unit,
    onClubSelected: (String) -> Unit
) {
    var selectedClub by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Club") },
        text = {
            Column {
                Text("Select a club for this user to lead:")
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(clubs) { club ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedClub == club.id,
                                onClick = { selectedClub = club.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(club.name, fontWeight = FontWeight.Medium)
                                Text(club.description, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (selectedClub.isNotEmpty()) onClubSelected(selectedClub) },
                enabled = selectedClub.isNotEmpty()
            ) {
                Text("Assign Club")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}