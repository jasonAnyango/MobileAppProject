package com.example.clubapp.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubapp.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemAnnouncementsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("System Announcements") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Condition is checked inside the onClick lambda
                    if (announcementTitle.isNotEmpty() && announcementMessage.isNotEmpty() && !isSending) {
                        isSending = true
                        viewModel.sendSystemAnnouncement(
                            title = announcementTitle,
                            message = announcementMessage
                        ) { success, message ->
                            isSending = false
                            if (success) {
                                announcementTitle = ""
                                announcementMessage = ""
                            }
                        }
                    }
                }
                // The 'enabled' parameter has been removed from here.
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send Announcement")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Statistics
            val state by viewModel.state.collectAsState()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Announcement will be sent to:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${state.users.size} users",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Announcement Form
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Input
                Column {
                    Text(
                        text = "Announcement Title",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = announcementTitle,
                        onValueChange = { announcementTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter announcement title...") },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors()
                    )
                }

                // Message Input
                Column {
                    Text(
                        text = "Announcement Message",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = announcementMessage,
                        onValueChange = { announcementMessage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("Enter announcement message...") },
                        singleLine = false,
                        colors = TextFieldDefaults.outlinedTextFieldColors()
                    )
                }

                // Quick Templates
                Text(
                    text = "Quick Templates:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "System Maintenance" to "The system will be undergoing maintenance...",
                        "New Feature" to "We're excited to announce a new feature...",
                        "Important Update" to "Please be aware of an important update..."
                    ).forEach { (title, message) ->
                        TextButton(
                            onClick = {
                                announcementTitle = title
                                announcementMessage = message
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(title, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
