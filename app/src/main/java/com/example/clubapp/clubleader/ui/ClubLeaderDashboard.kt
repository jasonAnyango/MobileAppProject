package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen

@Composable
fun ClubLeaderDashboardScreen(
    navController: NavHostController
) {

    Scaffold(
        bottomBar = { ClubLeaderBottomNavigation(navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "Club Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------ CLUB LOGO ------------------
            Image(
                painter = painterResource(id = R.drawable.tennis_club),
                contentDescription = "Club Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ------------------ CLUB NAME ------------------
            Text(
                text = "Strathmore Tennis Team",  // Replace dynamically later
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ------------------ TWO DASHBOARD CARDS ------------------
            Row(modifier = Modifier.fillMaxWidth()) {

                DashboardCard(
                    title = "Members",
                    value = "48",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                DashboardCard(
                    title = "Upcoming Events",
                    value = "3",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ------------------ MANAGEMENT SECTION ------------------
            Spacer(modifier = Modifier.height(12.dp))

            ManagementListItem(
                title = "Manage Members",
                icon = Icons.Default.Group,
                onClick = { navController.navigate(ClubLeaderScreen.Members.route) }
            )

            ManagementListItem(
                title = "Create Event",
                icon = Icons.Default.AddCircle,
                onClick = { navController.navigate(ClubLeaderScreen.AddEvent.route) }
            )

            ManagementListItem(
                title = "Edit Club Info",
                icon = Icons.Default.Edit,
                onClick = { navController.navigate(ClubLeaderScreen.ManageClub.route) }
            )

            ManagementListItem(
                title = "Club Announcements",
                icon = Icons.Default.Notifications,
                onClick = { navController.navigate(ClubLeaderScreen.Announcements.route) }
            )
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}


@Composable
fun ManagementListItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() } // make card clickable
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
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

