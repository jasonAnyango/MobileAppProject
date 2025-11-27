package com.example.clubapp.student.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.clubapp.student.data.StudentRepository
import com.example.clubapp.model.Club
import com.example.clubapp.model.User

@Composable
fun StudentClubDetailsScreen(
    studentRepository: StudentRepository,
    clubId: String
) {
    val coroutineScope = rememberCoroutineScope()

    // State
    var club by remember { mutableStateOf<Club?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var isMember by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) } // To force refresh

    LaunchedEffect(clubId, refreshTrigger) {
        coroutineScope.launch {
            // 1. Fetch Data
            val allClubs = studentRepository.getActiveClubs()
            club = allClubs.find { it.id == clubId }

            user = studentRepository.getMyUserProfile()

            // 2. Check Membership
            if (user != null && club != null) {
                isMember = user!!.clubsJoined.contains(clubId)
            }
        }
    }

    if (club == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Club Image Section
            ClubImageSection(club = club!!)

            Spacer(modifier = Modifier.height(16.dp))

            // Club Info Section
            ClubInfoSection(
                club = club!!,
                isMember = isMember,
                onJoinToggle = {
                    coroutineScope.launch {
                        if (isMember) {
                            studentRepository.leaveClub(clubId)
                        } else {
                            studentRepository.joinClub(clubId)
                        }
                        refreshTrigger++ // Reload UI
                    }
                }
            )
        }
    }
}

@Composable
fun ClubImageSection(club: Club) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        // Since we removed ImageUrl for now, we show a nice Placeholder with Initials
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = club.name.take(2).uppercase(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ClubInfoSection(
    club: Club,
    isMember: Boolean,
    onJoinToggle: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = club.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Status badge
            if (isMember) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("Member", modifier = Modifier.padding(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Join/Leave Button
        Button(
            onClick = onJoinToggle,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMember) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(if (isMember) Icons.Default.ExitToApp else Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isMember) "Leave Club" else "Join Club")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details
        DetailItem("Mission", club.mission)
        DetailItem("Description", club.description)
        DetailItem("Members", "${club.memberIds.size} Active Members")
        DetailItem("Leader ID", club.leaderId.ifEmpty { "N/A" })
    }
}

@Composable
fun ClubListItem(
    club: Club,
    isMember: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = club.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(club.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = club.mission,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = Color.Gray
                )
            }

            if (isMember) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Joined", tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

// --- SCREEN: BROWSE CLUBS ---
@Composable
fun BrowseClubsScreen(
    studentRepository: StudentRepository,
    onClubClick: (String) -> Unit
) {
    var clubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var user by remember { mutableStateOf<User?>(null) }

    // Fetch Data
    LaunchedEffect(Unit) {
        clubs = studentRepository.getActiveClubs()
        user = studentRepository.getMyUserProfile()
    }

    if (clubs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active clubs found.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            item {
                Text("All Active Clubs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }
            items(clubs) { club ->
                val isMember = user?.clubsJoined?.contains(club.id) == true
                ClubListItem(club = club, isMember = isMember, onClick = { onClubClick(club.id) })
            }
        }
    }
}

// --- SCREEN: MY CLUBS ---
@Composable
fun MyClubsScreen(
    studentRepository: StudentRepository,
    onClubClick: (String) -> Unit
) {
    var myClubs by remember { mutableStateOf<List<Club>>(emptyList()) }

    LaunchedEffect(Unit) {
        val user = studentRepository.getMyUserProfile()
        val allClubs = studentRepository.getActiveClubs()

        if (user != null) {
            // Filter locally for now
            myClubs = allClubs.filter { user.clubsJoined.contains(it.id) }
        }
    }

    if (myClubs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.GroupOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("You haven't joined any clubs yet.")
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Text("My Memberships", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }
            items(myClubs) { club ->
                ClubListItem(club = club, isMember = true, onClick = { onClubClick(club.id) })
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
}