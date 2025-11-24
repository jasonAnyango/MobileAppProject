package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EventsScreen(
    // onAddEvent: () -> Unit = {} // navigate to AddEventScreen
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Upcoming, 1 = Past

    val upcomingEvents = listOf("Team Building", "Training", "Leaders Meeting")
    val pastEvents = listOf("Orientation", "Welcome Party")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* onAddEvent() */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        },
        bottomBar = { EventsBottomNav() }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "Events",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------ TABS ------------------
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabButton(
                    title = "Upcoming",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                TabButton(
                    title = "Past",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    activeColor = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------ EVENTS LIST ------------------
            val eventsToShow = if (selectedTab == 0) upcomingEvents else pastEvents

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(eventsToShow) { event ->
                    EventListItem(event)
                }
            }
        }
    }
}

@Composable
fun EventListItem(eventName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 20.dp)  // ✔ Bigger, balanced padding
        ) {
            Icon(
                Icons.Default.Event,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = eventName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
fun EventsBottomNav() {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { /* Dashboard */ },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = true, // Events active
            onClick = { /* Events */ },
            icon = { Icon(Icons.Default.Event, contentDescription = null) },
            label = { Text("Events") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Members */ },
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Members") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Announcements */ },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text("Announce") }
        )
    }
}
