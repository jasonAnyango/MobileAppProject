package com.example.clubapp.student.ui

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
import kotlinx.coroutines.launch
// --- UPDATED IMPORTS ---
import com.example.clubapp.student.data.StudentRepository
import com.example.clubapp.model.User
import com.example.clubapp.model.Event

@Composable
fun StudentDashboardScreen(
    studentRepository: StudentRepository,
    onBrowseClubs: () -> Unit,
    onMyClubs: () -> Unit,
    onBrowseEvents: () -> Unit,
    onMyEvents: () -> Unit,
    onCreateClub: () -> Unit,
    onManageClub: () -> Unit // <--- 1. ADDED MISSING PARAMETER
) {
    val coroutineScope = rememberCoroutineScope()

    // --- 2. UPDATED STATE VARIABLES TO USE SHARED MODELS ---
    var user by remember { mutableStateOf<User?>(null) }
    var myClubsCount by remember { mutableStateOf(0) }
    var upcomingEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    // Note: We don't have 'getStudentEventRegistrations' in the new repo yet,
    // so I'll simplify stats for now to prevent crashes.

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // --- 3. UPDATED REPOSITORY CALLS ---
            val currentUser = studentRepository.getMyUserProfile()
            user = currentUser

            if (currentUser != null) {
                myClubsCount = currentUser.clubsJoined.size
            }

            // Fetch real events
            upcomingEvents = studentRepository.getAllEvents().take(3)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    // --- 4. CHANGED student.name TO user.fullName ---
                    text = "Welcome, ${user?.fullName ?: "Student"}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Explore clubs and events around campus",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. MANAGE CLUB BUTTON (Only for Leaders) ---
        val isClubLeader = user?.role == "Club Lead" || user?.isClubLeader == true

        if (isClubLeader) {
            ActionCard(
                title = "Manage My Club",
                description = "Access Leader Dashboard",
                icon = Icons.Default.Star, // Distinct Icon
                onClick = onManageClub,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Stats
        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardStatCard(
                title = "My Clubs",
                value = myClubsCount.toString(),
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f),
                onClick = onMyClubs
            )
            Spacer(modifier = Modifier.width(8.dp))
            DashboardStatCard(
                title = "Events",
                value = upcomingEvents.size.toString(), // Placeholder until event registration logic exists
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f),
                onClick = onBrowseEvents
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = "Browse All Clubs",
            description = "Discover new clubs to join",
            icon = Icons.Default.Search,
            onClick = onBrowseClubs
        )

        ActionCard(
            title = "Create New Club",
            description = "Start your own club",
            icon = Icons.Default.Add,
            onClick = onCreateClub
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming Events Preview
        if (upcomingEvents.isNotEmpty()) {
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            upcomingEvents.forEach { event ->
                EventPreviewItem(event) {
                    onBrowseEvents()
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface // Default color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor) // Allow custom color
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun EventPreviewItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                // Note: event.date is now a String in the new model, so no need for .toDate()
                Text(text = "${event.date} • ${event.clubName}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}