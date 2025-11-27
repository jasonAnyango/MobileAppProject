package com.example.clubapp.student.ui

import androidx.compose.foundation.clickable
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
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun BrowseEventsScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    onEventClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<com.example.clubapp.student.data.Event>>(emptyList()) }
    var registrations by remember { mutableStateOf<List<com.example.clubapp.student.data.EventRegistration>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            events = studentRepository.getAllEvents()
            registrations = studentRepository.getStudentEventRegistrations()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(events) { event ->
            val isRegistered = registrations.any { it.eventId == event.id }

            EventListItem(
                event = event,
                isRegistered = isRegistered,
                onClick = { onEventClick(event.id) }
            )
            Divider()
        }
    }
}

@Composable
fun MyEventsScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    onEventClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<com.example.clubapp.student.data.Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val registrations = studentRepository.getStudentEventRegistrations()
            val eventIds = registrations.map { it.eventId }
            val allEvents = studentRepository.getAllEvents()
            events = allEvents.filter { it.id in eventIds }
        }
    }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("You haven't registered for any events", style = MaterialTheme.typography.bodyLarge)
                Text("Browse events to get started!", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(events) { event ->
                EventListItem(
                    event = event,
                    isRegistered = true,
                    onClick = { onEventClick(event.id) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun StudentEventDetailsScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    eventId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var event by remember { mutableStateOf<com.example.clubapp.student.data.Event?>(null) }
    var isRegistered by remember { mutableStateOf(false) }
    var showRegistrationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        coroutineScope.launch {
            event = studentRepository.getEventById(eventId)
            isRegistered = studentRepository.isRegisteredForEvent(eventId)
        }
    }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event not found")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(event!!.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            // Event details card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(Icons.Default.DateRange, "Date",
                        SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                            .format(event!!.date.toDate()))
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.Place, "Location", event!!.location)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.Group, "Organizer", event!!.clubName)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.People, "Participants",
                        "${event!!.currentParticipants}/${event!!.maxParticipants}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(event!!.description)

            Spacer(modifier = Modifier.height(32.dp))

            // Registration button
            if (isRegistered) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            studentRepository.unregisterFromEvent(eventId)
                            isRegistered = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unregister from Event")
                }
            } else {
                Button(
                    onClick = { showRegistrationDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register for Event")
                }
            }

            // Registration Dialog
            if (showRegistrationDialog) {
                AlertDialog(
                    onDismissRequest = { showRegistrationDialog = false },
                    title = { Text("Register for Event") },
                    text = { Text("Are you sure you want to register for \"${event!!.title}\"?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    studentRepository.registerForEvent(eventId)
                                    isRegistered = true
                                    showRegistrationDialog = false
                                }
                            }
                        ) {
                            Text("Register")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRegistrationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EventListItem(
    event: com.example.clubapp.student.data.Event,
    isRegistered: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        leadingContent = {
            Icon(Icons.Default.Event, contentDescription = null)
        },
        headlineContent = {
            Text(event.title)
        },
        supportingContent = {
            Text("${SimpleDateFormat("MMM dd", Locale.getDefault()).format(event.date.toDate())} • ${event.clubName}")
        },
        trailingContent = {
            if (isRegistered) {
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("Registered")
                }
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("$label: $value", style = MaterialTheme.typography.bodyMedium)
    }
}