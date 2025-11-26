package com.example.clubapp.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubapp.admin.data.MockAdminRepository

// --- 1. Club Management Parent Screen (Tabs) ---
@Composable
fun ClubManagementScreen(
    onClubClick: (String) -> Unit,
    onApplicationClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Clubs", "Applications")

    // Removed internal Scaffold to avoid double headers (handled by AdminNavGraph)
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Display content based on tab
        when (selectedTab) {
            0 -> AllClubsList(onClubClick)
            1 -> ClubApplicationsList(onApplicationClick)
        }
    }
}

// --- Sub-component: All Clubs List ---
@Composable
fun AllClubsList(onClubClick: (String) -> Unit) {
    // FETCH DATA FROM MOCK REPO
    val clubs = remember { MockAdminRepository.getAllClubs() }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(clubs) { club ->
            ListItem(
                headlineContent = { Text(club.name, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(if(club.isActive) "Active • ${club.memberCount} members" else "Inactive") },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                modifier = Modifier.clickable { onClubClick(club.name) }
            )
            Divider()
        }
    }
}

// --- Sub-component: Applications List ---
@Composable
fun ClubApplicationsList(onApplicationClick: (String) -> Unit) {
    // FETCH DATA FROM MOCK REPO
    val applications = remember { MockAdminRepository.getPendingApplications() }

    if (applications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending applications", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(applications) { app ->
                ListItem(
                    headlineContent = { Text(app.clubName, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Applicant: ${app.applicantName}") },
                    trailingContent = {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("Pending", modifier = Modifier.padding(4.dp), color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    },
                    modifier = Modifier.clickable { onApplicationClick(app.clubName) }
                )
                Divider()
            }
        }
    }
}


// Helpers
@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
