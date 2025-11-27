package com.example.clubapp.student.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.clubapp.student.data.StudentRepository
import com.example.clubapp.model.User
import com.example.clubapp.model.Event

@Composable
fun StudentDashboardScreen(
    studentRepository: StudentRepository,
    onBrowseClubs: () -> Unit,
    onMyClubs: () -> Unit,
    onBrowseEvents: () -> Unit,
    onMyEvents: () -> Unit,
    onCreateClub: () -> Unit,
    onManageClub: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var myClubsCount by remember { mutableStateOf(0) }
    var upcomingEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val currentUser = studentRepository.getMyUserProfile()
            user = currentUser
            if (currentUser != null) {
                myClubsCount = currentUser.clubsJoined.size
            }
            upcomingEvents = studentRepository.getAllEvents().take(3)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background) // Clean background
            .padding(16.dp)
    ) {
        // --- 1. HERO WELCOME CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Hello, ${user?.fullName?.split(" ")?.firstOrNull() ?: "Student"}!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Ready to explore campus life?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 10.dp, y = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. STATS ROW ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardStatCard(
                title = "My Clubs",
                value = myClubsCount.toString(),
                icon = Icons.Default.Group,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = onMyClubs,
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                title = "Events",
                value = upcomingEvents.size.toString(),
                icon = Icons.Default.Event,
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onBrowseEvents,
                modifier = Modifier.weight(1f)
            )
        }

        // --- 3. LEADER ACTION (Conditional) ---
        val isClubLeader = user?.role == "Club Lead" || user?.isClubLeader == true
        if (isClubLeader) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onManageClub,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Go to Leader Dashboard", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. QUICK ACTIONS GRID ---
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardActionTile(
                title = "Browse Clubs",
                icon = Icons.Default.Search,
                color = MaterialTheme.colorScheme.primary,
                onClick = onBrowseClubs,
                modifier = Modifier.weight(1f)
            )
            DashboardActionTile(
                title = "Browse Events",
                icon = Icons.Default.CalendarToday,
                color = MaterialTheme.colorScheme.secondary,
                onClick = onBrowseEvents,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Row 2
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardActionTile(
                title = "Start a Club",
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onCreateClub,
                modifier = Modifier.weight(1f)
            )
            // Empty placeholder or another action could go here
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. UPCOMING EVENTS (Simplified List) ---
        if (upcomingEvents.isNotEmpty()) {
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            upcomingEvents.forEach { event ->
                EventPreviewItem(event) { onBrowseEvents() }
            }
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = Color.Black.copy(alpha = 0.6f))
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun DashboardActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EventPreviewItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Date Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = event.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = event.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}