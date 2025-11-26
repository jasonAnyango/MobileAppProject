package com.example.clubapp.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- 1. All Events Screen ---
@Composable
fun AllEventsScreen(onEventClick: (String) -> Unit) {
    val events = listOf("Annual Tech Fair", "Music Concert", "Chess Tournament")

    // Scaffold removed to prevent double headers
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(events) { event ->
            ListItem(
                leadingContent = { Icon(Icons.Default.DateRange, null) },
                headlineContent = { Text(event) },
                supportingContent = { Text("Nov 12 • Tech Club") }, // Added Club Name association
                modifier = Modifier.clickable { onEventClick(event) }
            )
            Divider()
        }
    }
}

// --- 2. Event Details Screen ---
@Composable
fun EventDetailsScreen(eventId: String) {
    // Scaffold removed to prevent double headers
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(eventId, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Date: Nov 12, 2024, 10:00 AM")
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Location: Main Hall")
                }
                Spacer(Modifier.height(8.dp))
                Text("Organizer: Tech Club", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("A showcase of the latest student projects and robotics from the university engineering department. Open to all students.")
    }
}