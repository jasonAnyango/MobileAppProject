package com.example.clubapp.clubleader.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen

@Composable
fun ManageClubScreen(navController: NavHostController) {

    Scaffold(
        // Use default theme background color
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { ManageClubBottomNav(navController) }
    ) { innerPadding ->

        var clubName by remember { mutableStateOf("") }
        var clubDescription by remember { mutableStateOf("") }
        var meetingTime by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // ---------------- PAGE TITLE ----------------
            Text(
                text = "Edit Club Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- CLUB NAME ----------------
            BasicTextField(
                value = clubName,
                onValueChange = { clubName = it },
                label = "Club Name"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- CLUB DESCRIPTION ----------------
            BasicTextField(
                value = clubDescription,
                onValueChange = { clubDescription = it },
                label = "Club Description",
                minLines = 4 // Control height via minLines
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- MEETING TIME ----------------
            BasicTextField(
                value = meetingTime,
                onValueChange = { meetingTime = it },
                label = "Meeting Time"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- CLUB LOGO ----------------
            Image(
                painter = painterResource(id = R.drawable.tennis_club),
                contentDescription = "Club Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp) // Reduced size slightly
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape) // Kept for standard avatar shape
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO open image picker */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Change Logo")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------------- SAVE BUTTON ----------------
            Button(
                onClick = { /* Save changes */ },
                // Use default primary color for the main action button
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun ManageClubBottomNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // NavigationBar uses default theme colors
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