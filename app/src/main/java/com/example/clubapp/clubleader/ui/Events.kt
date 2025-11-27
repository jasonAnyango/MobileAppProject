package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen

@Composable
fun EventsScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val upcomingEvents = listOf("Team Building", "Training", "Leaders Meeting")
    val pastEvents = listOf("Orientation", "Welcome Party")

    Scaffold(
        // Use default theme background color
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ClubLeaderScreen.AddEvent.route) },
                // Use primary container color and onPrimary tint
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },
        bottomBar = { EventsBottomNav(navController) }
    ) { padding ->

        // Removed custom gradient background Box
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp) // Reduced padding
        ) {

            // ------------------ TITLE ------------------
            Text(
                text = "Events",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground // Default text color
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------ TABS ------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Use spacing instead of space evenly
            ) {
                TabButton(
                    title = "Upcoming",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    // Using primary color for active state indication
                    activeColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                TabButton(
                    title = "Past",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    // Using secondary color for active state indication
                    activeColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------ EVENTS LIST ------------------
            val eventsToShow = if (selectedTab == 0) upcomingEvents else pastEvents

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
            ) {
                items(eventsToShow) { event ->
                    EventListItem(event)
                }
            }
        }
    }
}

@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) activeColor else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            // Removed clip
            .background(bgColor)
            .padding(vertical = 10.dp)
            .clickable { onClick() }
            .fillMaxWidth(), // Ensure button fills its weighted space
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun EventListItem(eventName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Use default surface color
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // Use default elevation
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        // Removed custom shape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                // Use default primary color
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = eventName,
                style = MaterialTheme.typography.bodyLarge,
                // Use default onSurface color
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EventsBottomNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // NavigationBar uses default theme colors, so no changes needed here other than icon changes
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Dashboard.route,
            onClick = { navController.navigate(ClubLeaderScreen.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Events.route,
            onClick = { navController.navigate(ClubLeaderScreen.Events.route) },
            icon = { Icon(Icons.Default.Event, contentDescription = null) },
            label = { Text("Events") }
        )

        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Members.route,
            onClick = { navController.navigate(ClubLeaderScreen.Members.route) },
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Members") }
        )

        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Announcements.route,
            onClick = { navController.navigate(ClubLeaderScreen.Announcements.route) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text("Announce") }
        )
    }
}