package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddAnnouncementScreen(
   // onPublish: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { AddAnnouncementBottomNav() }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "New Announcement",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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
                onClick = { /* onPublish */ },
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
fun AddAnnouncementBottomNav() {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { /* Dashboard */ },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Events */ },
            icon = { Icon(Icons.Default.Event, contentDescription = null) },
            label = { Text("Events") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Members */ },
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Members") }
        )

        NavigationBarItem(
            selected = true, // Announce active
            onClick = { /* Announcements */ },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text("Announce") }
        )
    }
}


