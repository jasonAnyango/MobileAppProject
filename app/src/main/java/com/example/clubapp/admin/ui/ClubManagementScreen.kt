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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get // Inject Koin
import com.example.clubapp.admin.data.AdminRepository
import com.example.clubapp.model.Club
import com.example.clubapp.model.ClubRegistration

// --- 1. Club Management Parent Screen (Tabs + Search + Filter) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubManagementScreen(
    onClubClick: (String) -> Unit,
    onApplicationClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Clubs", "Applications")

    // State for Search and Filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Reset filters when switching tabs
    LaunchedEffect(selectedTab) {
        searchQuery = ""
        selectedFilter = "All"
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- Search Bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search by name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        // --- Filter Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = if (selectedTab == 0) {
                listOf("All", "Active", "Inactive")
            } else {
                listOf("All", "Pending")
            }

            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    leadingIcon = if (selectedFilter == filter) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        // --- Tabs ---
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // --- Content ---
        when (selectedTab) {
            0 -> AllClubsList(
                onClubClick = onClubClick,
                searchQuery = searchQuery,
                filterStatus = selectedFilter
            )
            1 -> ClubApplicationsList(
                onApplicationClick = onApplicationClick,
                searchQuery = searchQuery,
                filterStatus = selectedFilter
            )
        }
    }
}

// --- Sub-component: All Clubs List ---
@Composable
fun AllClubsList(
    onClubClick: (String) -> Unit,
    searchQuery: String,
    filterStatus: String
) {
    // 1. Inject Repository
    val repository: AdminRepository = get()

    // 2. Load Real Data from Firebase
    val allClubs by produceState(initialValue = emptyList<Club>()) {
        value = repository.getAllClubs()
    }

    // Filter Logic
    val filteredClubs = allClubs.filter { club ->
        val matchesSearch = club.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterStatus) {
            "Active" -> club.isActive
            "Inactive" -> !club.isActive
            else -> true
        }
        matchesSearch && matchesFilter
    }

    if (filteredClubs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (allClubs.isEmpty()) {
                CircularProgressIndicator() // Show loading if data hasn't arrived
            } else {
                Text("No clubs found matching criteria", color = MaterialTheme.colorScheme.outline)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredClubs) { club ->
                ListItem(
                    headlineContent = { Text(club.name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        val status = if(club.isActive) "Active" else "Inactive"
                        // Note: memberIds.size because memberCount is calculated now
                        Text("$status • ${club.memberIds.size} members")
                    },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                    modifier = Modifier.clickable { onClubClick(club.id) }
                )
                Divider()
            }
        }
    }
}

// --- Sub-component: Applications List ---
@Composable
fun ClubApplicationsList(
    onApplicationClick: (String) -> Unit,
    searchQuery: String,
    filterStatus: String
) {
    // 1. Inject Repository
    val repository: AdminRepository = get()

    // 2. Load Real Data from Firebase
    val allApplications by produceState(initialValue = emptyList<ClubRegistration>()) {
        value = repository.getPendingApplications()
    }

    // Filter Logic
    val filteredApps = allApplications.filter { app ->
        val matchesSearch = app.clubName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterStatus) {
            "Pending" -> app.status == "Pending"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    if (filteredApps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (allApplications.isEmpty()) {
                // If the list is empty, it might be loading OR there are actually no requests.
                // For a smoother UI, usually, we'd check a loading state, but for now:
                Text("No pending applications", color = MaterialTheme.colorScheme.outline)
            } else {
                Text("No applications match search", color = MaterialTheme.colorScheme.outline)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredApps) { app ->
                ListItem(
                    headlineContent = { Text(app.clubName, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Applicant: ${app.applicantName}") },
                    trailingContent = {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("Pending", modifier = Modifier.padding(4.dp), color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    },
                    modifier = Modifier.clickable { onApplicationClick(app.id) }
                )
                Divider()
            }
        }
    }
}

// Helpers (Unchanged)
@Composable
fun ActionButton(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
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