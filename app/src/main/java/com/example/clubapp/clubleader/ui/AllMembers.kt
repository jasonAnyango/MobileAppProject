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
        // Use default theme background color
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { MembersBottomNav(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                // Removed custom gradient background
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Members",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground // Default text color
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------- TABS -------------------
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabButton(
                    title = "All Members",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    // Removed custom active/inactive colors
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Pending Requests",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    // Removed custom active/inactive colors
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val membersToShow = if (selectedTab == 0) allMembers else pendingMembers

            // Scrollable list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            // Use default surface color
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
        ) {
            // NOTE: The `Image` Composable requires the `clip(CircleShape)` for profile image styling.
            // This is a style, but is often essential for profile avatars, so it's left minimal.
            Image(
                painter = painterResource(id = member.profileRes),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(member.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(member.role, color = MaterialTheme.colorScheme.onSurfaceVariant) // Use secondary text color
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
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = title,
            // Use primary for selected, onSurface for unselected
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.labelLarge
        )
    }
}