package com.example.clubapp.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get // Inject Koin
import com.example.clubapp.admin.data.AdminRepository
import com.example.clubapp.model.Event

// --- 1. All Events Screen ---
@Composable
fun AllEventsScreen(onEventClick: (String) -> Unit) {
    val repository: AdminRepository = get()

    // Fetch Real Events from Firebase
    val events by produceState(initialValue = emptyList<Event>()) {
        value = repository.getAllEvents()
    }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Note: This shows loading initially, then "No events" if empty.
            // A proper loading state variable would be smoother, but this works for MVP.
            Text("No events found or loading...", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(events) { event ->
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    headlineContent = { Text(event.title, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        // Showing Date • Organizer
                        Text("${event.date} • ${event.clubName}")
                    },
                    modifier = Modifier.clickable { onEventClick(event.id) }
                )
                Divider()
            }
        }
    }
}

// --- 2. Event Details Screen ---
@Composable
fun EventDetailsScreen(eventId: String) {
    val repository: AdminRepository = get()

    // Fetch the specific event details
    val eventState = produceState<Event?>(initialValue = null, key1 = eventId) {
        // Since we don't have a 'getEventById' in Repo, we filter the list locally for now.
        // Ideally, add 'getEventById(id)' to your Repository later for efficiency.
        value = repository.getAllEvents().find { it.id == eventId }
    }

    val event = eventState.value

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
            Text(event.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Date: ${event.date}")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Location: ${event.location}")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null) // Club Icon
                        Spacer(Modifier.width(8.dp))
                        Text("Organizer: ", fontWeight = FontWeight.SemiBold)
                        Text(event.clubName, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = event.description.ifEmpty { "No description provided." },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}