package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubapp.R

@Composable
fun MembersScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0 = All Members, 1 = Pending Requests

    Scaffold(
        bottomBar = { MembersBottomNav() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // **Page Title**
            Text(
                text = "Members",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------
            //  **TABS**
            // -------------------
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabButton(
                    title = "All Members",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                TabButton(
                    title = "Pending Requests",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    activeColor = Color(0xFF4CAF50) // green for inactive
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------
            // **CONTENT**
            // -------------------
            if (selectedTab == 0) {
                MembersList(
                    members = listOf(
                        Member("Martin Njenga", "Men's Captain", R.drawable.profile),
                        Member("Ann Mwangi", "Women's Captain", R.drawable.profile),
                        Member("Clifford Chiama", "Men's Vice-Captain", R.drawable.profile),
                        Member("Almah", "Women's Vice-Captain", R.drawable.profile),
                        Member("Ethan Bwibo", "Treasurer", R.drawable.profile)
                    )
                )
            } else {
                MembersList(
                    members = listOf(
                        Member("Allan Muriuki", "Pending", R.drawable.profile),
                        Member("Grace Wanjiku", "Pending", R.drawable.profile)
                    )
                )
            }
        }
    }
}

@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = Color.Black
) {
    TextButton(onClick = onClick) {
        Text(
            text = title,
            color = if (selected) Color.Black else activeColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

data class Member(
    val name: String,
    val role: String,
    val image: Int
)

@Composable
fun MembersList(members: List<Member>) {
    LazyColumn {
        items(members) { member ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = member.image),
                    contentDescription = "Profile",
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(member.name, fontWeight = FontWeight.Bold)
                    Text(member.role, color = Color.Gray)
                }
            }

            Divider()
        }
    }
}

@Composable
fun MembersBottomNav() {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { /* Dashboard */ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Events */ },
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") }
        )

        NavigationBarItem(
            selected = true, // ACTIVE
            onClick = { /* Members */ },
            icon = { Icon(Icons.Default.Group, contentDescription = "Members") },
            label = { Text("Members") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Announcements */ },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Announce") },
            label = { Text("Announcements") }
        )
    }
}
