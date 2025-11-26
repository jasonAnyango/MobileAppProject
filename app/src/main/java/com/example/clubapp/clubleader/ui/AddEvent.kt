package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun AddEventScreen(
    navController: NavHostController,
    onPublish: () -> Unit = {}
) {
    var eventTitle by remember { mutableStateOf("") }
    var eventDateTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { AddEventBottomNav(navController) }
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "Add Event",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------ INPUT FIELDS ------------------
            StyledTextField(
                value = eventTitle,
                onValueChange = { eventTitle = it },
                label = "Event Title"
            )
            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = eventDateTime,
                onValueChange = { eventDateTime = it },
                label = "Date and Time"
            )
            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location"
            )
            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                isLarge = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ------------------ EVENT POSTER ------------------
            Image(
                painter = painterResource(id = R.drawable.tennis_club),
                contentDescription = "Event Poster",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO: open image picker */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Poster")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ------------------ PUBLISH EVENT BUTTON ------------------
            Button(
                onClick = { onPublish() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("Add Event", fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun AddEventBottomNav(navController: NavHostController) {
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
