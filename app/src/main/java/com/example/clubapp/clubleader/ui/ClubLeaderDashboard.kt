package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import com.example.clubapp.clubleader.viewmodel.ClubDashboardViewModel
import com.example.clubapp.clubleader.viewmodel.ClubDashboardViewModelFactory

@Composable
fun ClubLeaderDashboardScreen(
    navController: NavHostController
) {
    // 1. Get the Firestore instance
    val firestore = remember { FirebaseFirestore.getInstance() }

    // 2. Create the custom factory (only needs Firestore now)
    val factory = remember {
        ClubDashboardViewModelFactory(
            db = firestore
        )
    }

    // 3. Instantiate the ViewModel using the custom factory
    val viewModel: ClubDashboardViewModel = viewModel(factory = factory)

    // Collect the StateFlow from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { ClubLeaderBottomNavigation(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        val managementItems = listOf(
            Triple("Manage Members", Icons.Default.Group, MaterialTheme.colorScheme.primary),
            Triple("Create Event", Icons.Default.AddCircle, MaterialTheme.colorScheme.secondary),
            Triple("Edit Club Info", Icons.Default.Edit, Color(0xFF10B981)), // Use a distinct color
            Triple("Club Announcements", Icons.Default.Notifications, MaterialTheme.colorScheme.tertiary)
        )

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    text = "Club Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // --- Loading and Error State Handling ---
            if (uiState.isLoading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Loading club data...", color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                // --- Successful Data Display ---

                // 💡 NEW HEADER CARD WITH BACKGROUND IMAGE
                item {
                    ClubHeaderCard(uiState = uiState)
                }

                // MANAGEMENT TITLE
                item {
                    Text(
                        text = "Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // MANAGEMENT LIST ITEMS
                items(managementItems) { (title, icon, color) ->
                    ManagementListItem(
                        title = title,
                        icon = icon,
                        iconColor = color,
                        onClick = {
                            when (title) {
                                "Manage Members" -> navController.navigate(ClubLeaderScreen.Members.route)
                                "Create Event" -> navController.navigate(ClubLeaderScreen.AddEvent.route)
                                "Edit Club Info" -> navController.navigate(ClubLeaderScreen.ManageClub.route)
                                "Club Announcements" -> navController.navigate(ClubLeaderScreen.Announcements.route)
                            }
                        }
                    )
                }
            }
        }
    }
}


// --- NEW COMPOSABLES ---

@Composable
fun ClubHeaderCard(uiState: com.example.clubapp.clubleader.viewmodel.ClubDashboardUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Fixed height for visual impact
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Base color
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. Background Image (Subtle and Blended)
            Image(
                painter = painterResource(id = R.drawable.club_default), // Placeholder for the actual logo/image URL
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.2f, // Subtle opacity
                modifier = Modifier.matchParentSize()
            )

            // 2. Gradient Overlay for Readability (Fading to the base color)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Start semi-transparent
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)  // End more solid
                            )
                        )
                    )
            )

            // 3. Foreground Content (Name and Counts)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Club Name and Logo at Top
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Defined Logo
                    Image(
                        painter = painterResource(id = R.drawable.club_default),
                        contentDescription = "Club Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = uiState.clubName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dashboard Cards (Placed at the bottom of the Card)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardInfoPill(
                        title = "Members",
                        value = uiState.memberCount.toString(),
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Group,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                    DashboardInfoPill(
                        title = "Events",
                        value = uiState.eventCount.toString(),
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Event,
                        iconColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardInfoPill(title: String, value: String, modifier: Modifier = Modifier, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// --- SUPPORTING COMPOSABLES (Modified/Removed) ---

// REMOVED old DashboardCard
// The functionality is now integrated into ClubHeaderCard and DashboardInfoPill

@Composable
fun ManagementListItem(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon container with a subtle background for pop
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Trailing arrow for navigation hint
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ClubLeaderBottomNavigation(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == ClubLeaderScreen.Dashboard.route,
            onClick = { navController.navigate(ClubLeaderScreen.Dashboard.route) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
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