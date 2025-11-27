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

@Composable
fun StudentDashboardScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    onBrowseClubs: () -> Unit,
    onMyClubs: () -> Unit,
    onBrowseEvents: () -> Unit,
    onMyEvents: () -> Unit,
    onCreateClub: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var student by remember { mutableStateOf<com.example.clubapp.student.data.Student?>(null) }
    var myClubsCount by remember { mutableStateOf(0) }
    var myEventsCount by remember { mutableStateOf(0) }
    var pendingRequests by remember { mutableStateOf(0) }
    var upcomingEvents by remember { mutableStateOf<List<com.example.clubapp.student.data.Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            student = studentRepository.getCurrentStudent()
            myClubsCount = studentRepository.getStudentClubs().size
            myEventsCount = studentRepository.getStudentEventRegistrations().size
            pendingRequests = studentRepository.getStudentJoinRequests().count { it.status == "Pending" }
            upcomingEvents = studentRepository.getEventsForStudentClubs().take(2)
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
                    text = "Welcome, ${student?.name ?: "Student"}!",
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
                title = "My Events",
                value = myEventsCount.toString(),
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f),
                onClick = onMyEvents
            )
            Spacer(modifier = Modifier.width(8.dp))
            DashboardStatCard(
                title = "Pending",
                value = pendingRequests.toString(),
                icon = Icons.Default.Pending,
                modifier = Modifier.weight(1f),
                onClick = { /* Navigate to requests */ }
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
            title = "Browse Events",
            description = "Find upcoming events",
            icon = Icons.Default.CalendarToday,
            onClick = onBrowseEvents
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun EventPreviewItem(
    event: com.example.clubapp.student.data.Event,
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
                Text(text = "${event.date.toDate()} • ${event.clubName}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}