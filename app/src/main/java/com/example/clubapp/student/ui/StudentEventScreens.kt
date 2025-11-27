package com.example.clubapp.student.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
// --- UPDATED IMPORTS ---
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(events) { event ->
            EventListItem(
                event = event,
                onClick = { onEventClick(event.id) }
            )
            Divider()
        }
    }
}

@Composable
fun MyEventsScreen(
    studentRepository: StudentRepository,
    onEventClick: (String) -> Unit
) {
    // Note: We removed complex event registration for the MVP to simplify things.
    // For now, this will just show a placeholder or you can list all events.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Event Registration Feature coming soon!", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun StudentEventDetailsScreen(
    studentRepository: StudentRepository,
    eventId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var event by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(eventId) {
        coroutineScope.launch {
            // Filter locally for now
            val allEvents = studentRepository.getAllEvents()
            event = allEvents.find { it.id == eventId }
        }
    }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(event!!.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            // Event details card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(Icons.Default.DateRange, "Date", event!!.date)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.Place, "Location", event!!.location)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.Group, "Organizer", event!!.clubName)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(event!!.description)
        }
    }
}

@Composable
fun EventListItem(
    event: Event,
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
            Text("${event.date} • ${event.clubName}")
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
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