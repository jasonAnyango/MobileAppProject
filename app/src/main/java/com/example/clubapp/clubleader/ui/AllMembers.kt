package com.example.clubapp.clubleader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clubapp.R
import com.example.clubapp.clubleader.navigation.ClubLeaderScreen

data class Member(val name: String, val role: String, val profileRes: Int)

@Composable
fun MembersScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val allMembers = listOf(
        Member("Martin Njenga", "Men's Captain", R.drawable.profile),
        Member("Ann Mwangi", "Women's Captain", R.drawable.profile),
        Member("Clifford Chiama", "Men's Vice-Captain", R.drawable.profile),
        Member("Almah", "Women's Vice-Captain", R.drawable.profile),
        Member("Ethan Bwibo", "Treasurer", R.drawable.profile)
    )

    val pendingMembers = listOf(
        Member("Allan Muriuki", "Pending", R.drawable.profile),
        Member("Grace Wanjiku", "Pending", R.drawable.profile)
    )

    Scaffold(
        bottomBar = { MembersBottomNav(navController) }
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

            Text(
                text = "Members",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------- TABS -------------------
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabButton(
                    title = "All Members",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    activeColor = Color.White,
                    inactiveColor = Color(0xFFB3E5FC)
                )
                TabButton(
                    title = "Pending Requests",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    activeColor = Color.White,
                    inactiveColor = Color(0xFFB3E5FC)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val membersToShow = if (selectedTab == 0) allMembers else pendingMembers

            // Scrollable list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(membersToShow) { member ->
                    MemberListItem(member)
                }
            }
        }
    }
}

@Composable
fun MemberListItem(member: Member) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = member.profileRes),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(member.name, fontWeight = FontWeight.Bold)
                Text(member.role, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MembersBottomNav(navController: NavHostController) {
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

@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.LightGray
) {
    TextButton(onClick = onClick) {
        Text(
            text = title,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
