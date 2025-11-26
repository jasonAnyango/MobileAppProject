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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ClubLeaderScreen.AddEvent.route) },
                containerColor = Color(0xFF3B82F6)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event", tint = Color.White)
            }
        },
        bottomBar = { EventsBottomNav(navController) }
    ) { padding ->

        // 🔥 GRADIENT BACKGROUND
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A),
                            Color(0xFF3B82F6),
                            Color(0xFF8B5CF6),
                            Color(0xFFEF4444)
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
            ) {

                // ------------------ TITLE ------------------
                Text(
                    text = "Events",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ------------------ TABS ------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton(
                        title = "Upcoming",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        activeColor = Color(0xFF3B82F6)
                    )

                    TabButton(
                        title = "Past",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        activeColor = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

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
}


@Composable
fun TabButton(title: String, selected: Boolean, onClick: () -> Unit, activeColor: Color) {
    val bgColor = if (selected) activeColor.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.2f)
    val textColor = if (selected) Color.White else Color.White.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 22.dp)
            .clickable { onClick() }
    ) {
        Text(text = title, color = textColor)
    }
}


@Composable
fun EventListItem(eventName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                tint = Color(0xFF3B82F6)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = eventName,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1E1E1E)
            )
        }
    }
}


@Composable
fun EventsBottomNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
