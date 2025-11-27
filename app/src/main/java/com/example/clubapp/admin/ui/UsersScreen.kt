package com.example.clubapp.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import com.example.clubapp.admin.data.AdminRepository
import com.example.clubapp.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllUsersScreen(onUserClick: (String) -> Unit) {
    val repository: AdminRepository = get()
    val users by produceState(initialValue = emptyList<User>()) {
        value = repository.getAllUsers()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredUsers = users.filter { user ->
        val matchesSearch = user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.studentId.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            else -> user.role == selectedFilter
        }
        matchesSearch && matchesFilter
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Search Bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search users...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        // --- Filters ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Student", "Club Lead", "Admin")
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            filter,
                            fontWeight = if (selectedFilter == filter) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (selectedFilter == filter) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (filter) {
                            "Student" -> Color(0xFF42A5F5).copy(alpha = 0.9f)
                            "Club Lead" -> Color(0xFFFFB300).copy(alpha = 0.9f)
                            "Admin" -> Color(0xFFE53935).copy(alpha = 0.9f)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        selectedLabelColor = if (filter == "All") {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            Color.White
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = when (filter) {
                            "Student" -> Color(0xFF42A5F5)
                            "Club Lead" -> Color(0xFFFFB300)
                            "Admin" -> Color(0xFFE53935)
                            else -> MaterialTheme.colorScheme.outline
                        },
                        selectedBorderColor = Color.Transparent,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 0.dp
                    )
                )
            }
        }

        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (users.isEmpty()) CircularProgressIndicator()
                else Text("No users found.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredUsers) { user ->
                    ListItem(
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = if (user.isSuspended) Color(0xFFEF5350) else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.fullName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        },
                        headlineContent = { Text(user.fullName, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Column {
                                Text("Student ID: ${user.studentId}")
                                if (user.isSuspended) {
                                    Text(
                                        "SUSPENDED",
                                        color = Color(0xFFEF5350),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = roleColor(user.role),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = user.role,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = Color.White
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = "View Details")
                            }
                        },
                        modifier = Modifier.clickable { onUserClick(user.uid) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun UserDetailsScreen(userId: String) {
    val repository: AdminRepository = get()
    val scope = rememberCoroutineScope()
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val userState = produceState<User?>(initialValue = null, key1 = userId, key2 = refreshTrigger) {
        value = repository.getUserById(userId)
    }

    // Fetch club names for the user's joined clubs
    val clubNamesState = produceState<Map<String, String>>(initialValue = emptyMap(), key1 = userState.value?.clubsJoined, key2 = refreshTrigger) {
        val user = userState.value
        if (user != null && user.clubsJoined.isNotEmpty()) {
            val clubNames = mutableMapOf<String, String>()
            val allClubs = repository.getAllClubs()
            user.clubsJoined.forEach { clubId ->
                val club = allClubs.find { it.id == clubId }
                clubNames[clubId] = club?.name ?: "Unknown Club"
            }
            value = clubNames
        }
    }

    val user = userState.value
    val clubNames = clubNamesState.value

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = if (user.isSuspended) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (user.isSuspended) Icons.Default.Block else Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(user.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))

                    if (user.isSuspended) {
                        Badge(containerColor = Color(0xFFEF5350)) {
                            Text("ACCOUNT SUSPENDED", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Badge(containerColor = roleColor(user.role)) {
                        Text(user.role, style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // User Details Card
            Text("User Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(Icons.Default.Badge, "User ID", user.uid)
                    Spacer(Modifier.height(12.dp))
                    DetailRow(Icons.Default.School, "Student ID", user.studentId)
                    Spacer(Modifier.height(12.dp))
                    DetailRow(Icons.Default.Email, "Email", user.email)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Clubs Joined
            Text("Clubs Joined", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            if (user.clubsJoined.isEmpty()) {
                Text("This user has not joined any clubs yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            } else {
                user.clubsJoined.forEach { clubId ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = clubNames[clubId] ?: "Loading...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "ID: $clubId",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Suspend / Reactivate Button
            Button(
                onClick = {
                    scope.launch {
                        if (user.isSuspended) repository.reactivateUser(user.uid)
                        else repository.suspendUser(user.uid)
                        refreshTrigger++
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.isSuspended) Color(0xFF4CAF50) else Color(0xFFE53935)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                val icon = if (user.isSuspended) Icons.Default.CheckCircle else Icons.Default.Block
                val text = if (user.isSuspended) "Reactivate User" else "Suspend User"
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text)
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// --- Bright Role Colors ---

@Composable
fun roleColor(role: String): Color = when (role) {
    "Student" -> Color(0xFF42A5F5)
    "Club Lead" -> Color(0xFFFFB300)
    "Admin" -> Color(0xFFE53935)
    else -> MaterialTheme.colorScheme.secondaryContainer
}