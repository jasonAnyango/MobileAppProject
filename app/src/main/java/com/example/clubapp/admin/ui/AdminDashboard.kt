package com.example.clubapp.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import com.example.clubapp.admin.data.AdminRepository
import com.example.clubapp.model.*

@Composable
fun AdminDashboardScreen(
    onAllClubs: () -> Unit,
    onAllUsers: () -> Unit,
    onAllEvents: () -> Unit,
    onReports: () -> Unit
) {
    // 1. Inject the Repository directly (No separate ViewModel file needed)
    val repository: AdminRepository = get()

    // 2. Load Real Data using produceState (This replaces the Mock calls)

    // Fetch Clubs
    val clubs by produceState(initialValue = emptyList<Club>()) {
        value = repository.getAllClubs()
    }

    // Fetch Pending Requests
    val pendingRequests by produceState(initialValue = emptyList<ClubRegistration>()) {
        value = repository.getPendingApplications()
    }

    // Fetch Users
    val users by produceState(initialValue = emptyList<User>()) {
        value = repository.getAllUsers()
    }

    // 3. Calculate Counts for the UI
    val clubCount = clubs.size.toString()
    val pendingCount = pendingRequests.size.toString()
    val userCount = users.size.toString()

    // 4. Notifications Logic
    val notifications = if (pendingRequests.isNotEmpty()) {
        listOf("You have ${pendingRequests.size} new club applications pending approval.")
    } else {
        listOf("System is up to date.", "No new notifications.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // -------- SUMMARY CARDS --------
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryCard(
                title = "Total Clubs",
                value = clubCount,
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            SummaryCard(
                title = "Pending Apps",
                value = pendingCount,
                icon = Icons.Default.PendingActions,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            SummaryCard(
                title = "Total Users",
                value = userCount,
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -------- NOTIFICATIONS --------
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                notifications.forEach { notif ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = notif, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -------- HAPPENING NOW --------
        Text(
            text = "Happening Now / Upcoming",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        ListItem(
            headlineContent = { Text("Events System") },
            supportingContent = { Text("View all upcoming events") },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAllEvents() }
                .background(MaterialTheme.colorScheme.surface)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // ---------- QUICK NAVIGATION ----------
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        ManagementListItem("Club Management", Icons.Default.Business, onAllClubs)
        ManagementListItem("User Management", Icons.Default.ManageAccounts, onAllUsers)
    }
}

// --- Helper Composables (Unchanged) ---

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = title, style = MaterialTheme.typography.bodySmall, lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified)
            }
        }
    }
}

@Composable
fun ManagementListItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}