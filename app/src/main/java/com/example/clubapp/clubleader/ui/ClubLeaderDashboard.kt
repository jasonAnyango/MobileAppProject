package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen
import com.example.clubapp.ui.theme.ClubCard
import com.example.clubapp.ui.theme.ClubPrimary
import com.example.clubapp.ui.theme.ClubSecondary

@Composable
fun ClubLeaderDashboardScreen(
    navController: NavHostController
) {

    Scaffold(
        bottomBar = { ClubLeaderBottomNavigation(navController) },
        containerColor = Color.Transparent
    ) { innerPadding ->

        // GRADIENT BACKGROUND
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A),   // Deep academic blue
                            Color(0xFF3B82F6),   // Bright blue
                            Color(0xFF8B5CF6),   // Purple
                            Color(0xFFEF4444)    // Sporty red
                        )
                    )
                )
        ) {

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                item {
                    Text(
                        text = "Club Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // CLUB IMAGE & NAME
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.tennis_club),
                            contentDescription = "Club Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Strathmore Tennis Team",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.W600,
                            color = Color.White
                        )
                    }
                }

                // DASHBOARD CARDS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardCard(
                            title = "Members",
                            value = "48",
                            modifier = Modifier.weight(1f)
                        )
                        DashboardCard(
                            title = "Events",
                            value = "3",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // MANAGEMENT TITLE
                item {
                    Text(
                        text = "Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // MANAGEMENT LIST ITEMS
                items(listOf(
                    Triple("Manage Members", Icons.Default.Group, ClubPrimary),
                    Triple("Create Event", Icons.Default.AddCircle, ClubSecondary),
                    Triple("Edit Club Info", Icons.Default.Edit, Color(0xFF10B981)),
                    Triple("Club Announcements", Icons.Default.Notifications, Color(0xFF8B5CF6))
                )) { (title, icon, color) ->
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




// MODERN DASHBOARD CARD
@Composable
fun DashboardCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = ClubCard),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, color = ClubPrimary, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ClubSecondary
            )
        }
    }
}



// MODERN MANAGEMENT LIST ITEM
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
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



// BOTTOM NAV
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
