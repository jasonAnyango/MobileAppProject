package com.example.clubapp.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.clubapp.data.model.User
import com.example.clubapp.data.repository.UserRepository
import com.example.clubapp.viewmodel.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    userRepository: UserRepository = koinInject()
) {
    var user by remember { mutableStateOf<User?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    LaunchedEffect(userId) {
        userRepository.getUserById(userId).let { result ->
            if (result is com.example.clubapp.data.model.Resource.Success) {
                user = result.data
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = user!!.profileImageUrl.ifEmpty { "https://via.placeholder.com/150?text=${user!!.fullName.first()}" },
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = user!!.fullName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = user!!.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AssistChip(
                                onClick = { },
                                label = { Text("Student ID: ${user!!.studentId}") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Badge,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile")
                            }
                        }
                    }
                }

                // Personal Information
                item {
                    ProfileSection(title = "Personal Information") {
                        ProfileInfoItem(
                            icon = Icons.Default.Person,
                            label = "Full Name",
                            value = user!!.fullName
                        )
                        Divider()
                        ProfileInfoItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = user!!.email
                        )
                        Divider()
                        ProfileInfoItem(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = user!!.phoneNumber.ifEmpty { "Not provided" }
                        )
                    }
                }

                // Account Settings
                item {
                    ProfileSection(title = "Settings") {
                        ProfileActionItem(
                            icon = Icons.Default.Notifications,
                            label = "Notification Preferences",
                            onClick = { /* TODO */ }
                        )
                        Divider()
                        ProfileActionItem(
                            icon = Icons.Default.Security,
                            label = "Change Password",
                            onClick = { /* TODO */ }
                        )
                        Divider()
                        ProfileActionItem(
                            icon = Icons.Default.Help,
                            label = "Help & Support",
                            onClick = { /* TODO */ }
                        )
                    }
                }

                // Danger Zone
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        ListItem(
                            headlineContent = { Text("Logout") },
                            supportingContent = { Text("Sign out of your account") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

// Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

// Edit Profile Dialog (Simplified)
    if (showEditDialog) {
        var editedName by remember { mutableStateOf(user!!.fullName) }
        var editedPhone by remember { mutableStateOf(user!!.phoneNumber) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            icon = { Icon(Icons.Default.Edit, null) },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Update user profile
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}
@Composable
private fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    ListItem(
        headlineContent = { Text(value) },
        supportingContent = { Text(label) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}
@Composable
private fun ProfileActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}