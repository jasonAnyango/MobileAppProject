package com.example.clubapp.student.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.clubapp.student.data.StudentRepository
import com.example.clubapp.model.Event

@Composable
fun BrowseEventsScreen(
    studentRepository: StudentRepository,
    onEventClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            events = studentRepository.getAllEvents()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Upcoming Events", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }
        items(events) { event ->
            EventListItem(
                event = event,
                onClick = { onEventClick(event.id) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun MyEventsScreen(
    studentRepository: StudentRepository,
    onEventClick: (String) -> Unit
) {
    // Placeholder - Ideally filter for events where user is registered
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("My Registrations coming soon!")
    }
}

// --- DETAILED EVENT SCREEN ---
@Composable
fun StudentEventDetailsScreen(
    studentRepository: StudentRepository,
    eventId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var event by remember { mutableStateOf<Event?>(null) }
    var registrationStatus by remember { mutableStateOf("Loading") } // "None", "Pending", "Registered"
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(eventId, refreshTrigger) {
        coroutineScope.launch {
            // 1. Get Event Data
            val allEvents = studentRepository.getAllEvents()
            event = allEvents.find { it.id == eventId }

            // 2. Get My Status
            registrationStatus = studentRepository.getEventRegistrationStatus(eventId)
        }
    }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val currentEvent = event!!

        Scaffold(
            floatingActionButton = {
                // FAB Logic based on Status
                ExtendedFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (registrationStatus == "None") {
                                studentRepository.registerForEvent(eventId)
                            } else {
                                // Cancel if Pending or Registered
                                studentRepository.cancelEventRegistration(eventId)
                            }
                            refreshTrigger++ // Refresh status
                        }
                    },
                    containerColor = if (registrationStatus == "None") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (registrationStatus == "None") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onErrorContainer
                ) {
                    val icon = if (registrationStatus == "None") Icons.Default.Event else Icons.Default.Cancel
                    val text = when (registrationStatus) {
                        "None" -> "Register Interest"
                        "Pending" -> "Cancel Request"
                        else -> "Cancel Registration"
                    }
                    Icon(icon, null)
                    Spacer(Modifier.width(8.dp))
                    Text(text)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(currentEvent.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                // Status Badge
                if (registrationStatus != "None" && registrationStatus != "Loading") {
                    Spacer(Modifier.height(8.dp))
                    val badgeColor = if (registrationStatus == "Registered") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                    Badge(containerColor = badgeColor) {
                        val label = if (registrationStatus == "Pending") "Approval Pending" else "Registered"
                        Text(label, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.DateRange, "Date", currentEvent.date)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(Icons.Default.Place, "Location", currentEvent.location)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(Icons.Default.Group, "Organizer", currentEvent.clubName)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(currentEvent.description, style = MaterialTheme.typography.bodyLarge)

                // Extra padding for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun EventListItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        ListItem(
            leadingContent = {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            headlineContent = { Text(event.title, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("${event.date} • ${event.clubName}") },
            trailingContent = { Icon(Icons.Default.ChevronRight, null) }
        )
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}