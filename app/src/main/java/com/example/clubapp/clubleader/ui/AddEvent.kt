package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubapp.R

@Composable
fun AddEventScreen() {

    var eventTitle by remember { mutableStateOf("") }
    var eventDateTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { AddEventBottomNav() }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // ------------------ PAGE TITLE ------------------
            Text(
                text = "Add Event",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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

            // ------------------ OPTIONAL POSTER ------------------
            Image(
                painter = painterResource(id = R.drawable.tennis_club),
                contentDescription = "Event Poster",
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.medium)
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

            // ------------------ ADD EVENT BUTTON ------------------
            Button(
                onClick = { /* TODO: Add event logic */ },
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
fun AddEventBottomNav() {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { /* Dashboard */ },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = true, // Events active
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
            selected = false,
            onClick = { /* Announce */ },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text("Announce") }
        )
    }
}

