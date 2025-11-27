package com.example.clubapp.admin.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import com.example.clubapp.admin.data.AdminRepository
import com.example.clubapp.model.*

@Composable
fun AdminDashboardScreen(
    onAllClubs: () -> Unit,
    onAllUsers: () -> Unit,
    onAllEvents: () -> Unit, // Navigation callback for Events screen
    onReports: () -> Unit
) {
    val repository: AdminRepository = get()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State for Dialogs/Loading
    var showAnnounceDialog by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }

    // Load Data
    val clubs by produceState(initialValue = emptyList<Club>()) { value = repository.getAllClubs() }
    val pendingRequests by produceState(initialValue = emptyList<ClubRegistration>()) { value = repository.getPendingApplications() }
    val users by produceState(initialValue = emptyList<User>()) { value = repository.getAllUsers() }
    val upcomingEvents by produceState(initialValue = emptyList<Event>()) { value = repository.getUpcomingEvents() }

    // Calculate Counts
    val clubCount = clubs.size.toString()
    val pendingCount = pendingRequests.size.toString()
    val userCount = users.size.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // -------- SUMMARY CARDS (COORDINATED COLORS) --------
        Row(modifier = Modifier.fillMaxWidth()) {

            // CLUB COUNT (Green/Clubs)
            SummaryCard(
                title = "Total Clubs",
                value = clubCount,
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f),
                color = Color(0xFFE8F5E9), // Soft Green BG
                iconColor = Color(0xFF2E7D32) // Dark Green Accent
            )
            Spacer(modifier = Modifier.width(8.dp))

            // PENDING APPS (Orange/Tertiary)
            SummaryCard(
                title = "Pending Apps",
                value = pendingCount,
                icon = Icons.Default.PendingActions,
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFF3E0), // Soft Amber BG
                iconColor = Color(0xFFEF6C00) // Dark Amber Accent
            )
            Spacer(modifier = Modifier.width(8.dp))

            // TOTAL USERS (Blue/Primary)
            SummaryCard(
                title = "Total Users",
                value = userCount,
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                color = Color(0xFFE3F2FD), // Soft Blue BG
                iconColor = Color(0xFF1565C0) // Dark Blue Accent
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -------- NOTIFICATIONS (Clean Card) --------
        Text(text = "Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                if (pendingRequests.isNotEmpty()) {
                    Text(
                        text = "You have ${pendingRequests.size} new club applications pending approval.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "System is up to date. No new requests.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -------- UPCOMING EVENTS PREVIEW --------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAllEvents() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Happening Now / Upcoming",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = "View all events", tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))


        if (upcomingEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No events scheduled or upcoming.", color = Color.Gray)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                upcomingEvents.forEach { event ->
                    EventPreviewItem(event) { onAllEvents() }
                }
            }
        }


        Spacer(modifier = Modifier.height(30.dp))

        // ---------- QUICK NAVIGATION & ANNOUNCEMENT ----------
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        // 1. Send Announcement Button (Uses Primary Container accent)
        ManagementListItem(
            title = "Send Global Announcement",
            icon = Icons.Default.Campaign,
            onClick = { showAnnounceDialog = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )

        // 2. Standard Management (Uses default surface color)
        ManagementListItem("Club Management", Icons.Default.Business, onAllClubs)
        ManagementListItem("User Management", Icons.Default.ManageAccounts, onAllUsers)
        ManagementListItem("Analytics & Reports", Icons.Default.Analytics, onReports)
    }

    // --- DIALOG: SEND ANNOUNCEMENT FORM ---
    if (showAnnounceDialog) {
        SendAnnouncementDialog(
            repository = repository,
            isSending = isSending,
            onDismiss = { showAnnounceDialog = false },
            onStartSending = { isSending = true },
            onFinishSending = { isSending = false }
        )
    }
}

// ===========================================
// HELPER COMPOSABLES (Keep these defined in the file)
// ===========================================

@Composable
fun SendAnnouncementDialog(
    repository: AdminRepository,
    isSending: Boolean,
    onDismiss: () -> Unit,
    onStartSending: () -> Unit,
    onFinishSending: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Global Announcement") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., System Update)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank() && !isSending) {
                        onStartSending()
                        scope.launch {
                            repository.createGlobalAnnouncement(title, message, "Admin User")
                            Toast.makeText(context, "Announcement Sent!", Toast.LENGTH_SHORT).show()
                            onFinishSending()
                            onDismiss()
                        }
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank() && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            if (!isSending) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun EventPreviewItem(event: Event, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(event.title, fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            Text("${event.date} • ${event.clubName}", style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 4.dp)
    )
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color, iconColor: Color) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = iconColor)
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = iconColor)
                Text(text = title, style = MaterialTheme.typography.bodySmall, lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified, color = iconColor.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ManagementListItem(title: String, icon: ImageVector, onClick: () -> Unit, containerColor: Color = MaterialTheme.colorScheme.surface) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}