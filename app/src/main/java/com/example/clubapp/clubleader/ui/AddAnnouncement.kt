package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen

@Composable
fun AddAnnouncementScreen(
    navController: NavHostController,
    onPublish: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { AddAnnouncementBottomNav(navController) }
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
                text = "New Announcement",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------ TITLE INPUT ------------------
            StyledTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------ DESCRIPTION INPUT ------------------
            StyledTextField(
                value = description,
                onValueChange = { description = it },
                label = "Announcement Description",
                isLarge = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ------------------ PUBLISH BUTTON ------------------
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
                Text("Publish", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isLarge: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLarge) 150.dp else 55.dp)
            .background(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = Color.Black,
            focusedContainerColor = Color.White.copy(alpha = 0.9f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        maxLines = if (isLarge) 5 else 1
    )
}


@Composable
fun AddAnnouncementBottomNav(navController: NavHostController) {
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
