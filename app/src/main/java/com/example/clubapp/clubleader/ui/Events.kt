package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen
import com.example.clubapp.model.Event
import com.example.clubapp.clubleader.viewmodel.EventsViewModel
import com.example.clubapp.clubleader.viewmodel.EventsViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@Composable
fun EventsScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val firestore = remember { FirebaseFirestore.getInstance() }

    val factory = remember {
        EventsViewModelFactory(db = firestore)
    }

    val viewModel: EventsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(ClubLeaderScreen.AddEvent.route)
                },
                containerColor = MaterialTheme.colorScheme.primary, // Using primary for visibility
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        },
        bottomBar = { EventsBottomNav(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            // ------------------ TITLE ------------------
            Text(
                text = "Events",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle showing club name
            if (uiState.clubName.isNotEmpty()) {
                Text(
                    text = "Events for ${uiState.clubName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------ TABS ------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    title = "Upcoming (${uiState.upcomingEvents.size})",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    activeColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                TabButton(
                    title = "Past (${uiState.pastEvents.size})",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    activeColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------ EVENTS LIST ------------------
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                val eventsToShow = if (selectedTab == 0) uiState.upcomingEvents else uiState.pastEvents

                if (eventsToShow.isEmpty()) {
                    Text(
                        text = "No ${if (selectedTab == 0) "upcoming" else "past"} events found for ${uiState.clubName}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
                    ) {
                        items(eventsToShow) { event ->
                            EventListItem(event, isUpcoming = selectedTab == 0)
                        }
                    }
                }
            }
        }
    }
}

// --- UPDATED COMPOSABLES ---

@Composable
fun EventListItem(event: Event, isUpcoming: Boolean) {
    // Determine color based on tab
    val primaryColor = if (isUpcoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val containerColor = if (isUpcoming) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val onContainerColor = if (isUpcoming) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    // Attempt to parse date for display, falling back if format is incorrect
    val dateParts = try {
        // Assuming date is stored as YYYY-MM-DD
        val date = LocalDate.parse(event.date)
        Pair(date.dayOfMonth.toString(), date.month.toString().substring(0, 3))
    } catch (e: Exception) {
        Pair("?", "Date")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to Event Details */ },
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.8f)), // Use primary/secondary container color
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp) // Nicer rounded shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp), // Padding on the right
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 1. DATE BLOCK (Emphasized Left Side)
            Column(
                modifier = Modifier
                    .width(80.dp) // Fixed width for date block
                    .fillMaxHeight()
                    .background(primaryColor) // Solid active color background
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dateParts.first, // Day number
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = dateParts.second, // Month abbreviation
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. DETAILS (Right Side)
            Column(
                modifier = Modifier
                    .weight(1f) // Takes remaining space
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onContainerColor // Use the correct "on" color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = onContainerColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainerColor.copy(alpha = 0.8f)
                    )
                }

                // Optional: Short description snippet
                event.description.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = desc.substringBefore('\n').take(50) + if (desc.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
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

    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title, color = textColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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