package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen

data class Announcement(val title: String, val date: String)

@Composable
fun AnnouncementsScreen(navController: NavHostController) {

    val pastAnnouncements = remember {
        listOf(
            Announcement("Team Meeting", "2025-11-10"),
            Announcement("Club Outing", "2025-11-05"),
            Announcement("New Member Induction", "2025-10-28")
        )
    }

    Scaffold(
        bottomBar = { AnnouncementsBottomNav(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0D47A1),
                            Color(0xFF42A5F5)
                        )
                    )
                )
                .padding(padding)
                .padding(16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "Announcements",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------ CREATE ANNOUNCEMENT BUTTON ------------------
            Button(
                onClick = { navController.navigate(ClubLeaderScreen.AddAnnouncement.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("Create Announcement", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ------------------ PAST ANNOUNCEMENTS ------------------
            Text(
                text = "Past Announcements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pastAnnouncements) { announcement ->
                    AnnouncementItem(announcement)
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(announcement: Announcement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = announcement.title,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = announcement.date,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AnnouncementsBottomNav(navController: NavHostController) {
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
