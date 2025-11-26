package com.example.clubapp.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubapp.ui.components.*
import com.example.clubapp.viewmodel.student.StudentHomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    userId: String,
    onNavigateToClubDetail: (String) -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToClubBrowse: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: StudentHomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Clubs", "Events", "Profile")

    LaunchedEffect(userId) {
        viewModel.init(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ClubApp",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.notifications.count { !it.isRead } > 0) {
                                Badge {
                                    Text("${state.notifications.count { !it.isRead }}")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Groups
                                    2 -> Icons.Default.Event
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (index) {
                                1 -> onNavigateToClubBrowse()
                                3 -> onNavigateToProfile()
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTab(
                state = state,
                onClubClick = onNavigateToClubDetail,
                onEventClick = onNavigateToEventDetail,
                onSeeAllClubs = onNavigateToClubBrowse,
                modifier = Modifier.padding(padding)
            )
            2 -> EventsTab(
                events = state.events,
                onEventClick = onNavigateToEventDetail,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun HomeTab(
    state: com.example.clubapp.viewmodel.student.StudentHomeState,
    onClubClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onSeeAllClubs: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Welcome Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WavingHand,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Welcome Back!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Discover and join amazing clubs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Stats
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "My Clubs",
                    value = "${state.membershipRequests.count { it.status.name == "APPROVED" }}",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending",
                    value = "${state.membershipRequests.count { it.status.name == "PENDING" }}",
                    icon = Icons.Default.HourglassEmpty,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Events",
                    value = "${state.events.size}",
                    icon = Icons.Default.Event,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Featured Clubs Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Clubs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onSeeAllClubs) {
                    Text("See All")
                }
            }
        }

        item {
            if (state.clubs.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Groups,
                    message = "No clubs available yet",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.clubs.take(5)) { club ->
                        ClubCard(
                            club = club,
                            onClick = { onClubClick(club.id) },
                            modifier = Modifier.width(300.dp)
                        )
                    }
                }
            }
        }

        // Upcoming Events Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.events.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Event,
                    message = "No upcoming events",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        } else {
            items(state.events.take(3)) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EventsTab(
    events: List<com.example.clubapp.data.model.Event>,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Event,
            message = "No events available",
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event.id) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}