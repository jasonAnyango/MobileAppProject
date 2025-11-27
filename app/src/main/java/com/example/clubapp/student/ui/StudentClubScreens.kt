package com.example.clubapp.student.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch

@Composable
fun StudentClubDetailsScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    clubId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var club by remember { mutableStateOf<com.example.clubapp.student.data.Club?>(null) }
    var isMember by remember { mutableStateOf(false) }
    var hasPendingRequest by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(clubId) {
        coroutineScope.launch {
            club = studentRepository.getClubById(clubId)
            isMember = club?.members?.contains(studentRepository.currentUserId) == true
            hasPendingRequest = studentRepository.hasPendingRequest(clubId)
        }
    }

    if (club == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Club not found")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Club Image Section
            ClubImageSection(club = club!!)

            Spacer(modifier = Modifier.height(16.dp))

            // Club Info Section
            ClubInfoSection(
                club = club!!,
                isMember = isMember,
                hasPendingRequest = hasPendingRequest,
                onJoinClick = { showJoinDialog = true }
            )

            // Join Dialog
            if (showJoinDialog) {
                AlertDialog(
                    onDismissRequest = { showJoinDialog = false },
                    title = { Text("Join ${club!!.name}") },
                    text = { Text("Are you sure you want to send a join request to this club?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    studentRepository.sendJoinRequest(clubId)
                                    hasPendingRequest = true
                                    showJoinDialog = false
                                }
                            }
                        ) {
                            Text("Send Request")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showJoinDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ClubImageSection(club: com.example.clubapp.student.data.Club) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (club.imageUrl.isNotEmpty()) {
            // Display club image from URL
            Image(
                painter = rememberImagePainter(
                    data = club.imageUrl,
                    builder = {
                        crossfade(true)
                        error(androidx.compose.ui.res.painterResource(com.example.clubapp.R.drawable.ic_club_placeholder))
                    }
                ),
                contentDescription = "Club Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Display placeholder with club initial
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = club.name.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ClubInfoSection(
    club: com.example.clubapp.student.data.Club,
    isMember: Boolean,
    hasPendingRequest: Boolean,
    onJoinClick: () -> Unit
) {
    Column {
        Text(club.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        // Status badge
        when {
            isMember -> {
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("Member", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            hasPendingRequest -> {
                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text("Pending Approval", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            else -> {
                if (club.isActive) {
                    Button(onClick = onJoinClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Join Club")
                    }
                } else {
                    Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                        Text("Club Inactive", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Club details
        DetailItem("Description", club.description)
        DetailItem("Category", club.category)
        DetailItem("Members", "${club.memberCount} members")
        DetailItem("Admins", club.admins.joinToString())
    }
}

@Composable
fun ClubListItem(
    club: com.example.clubapp.student.data.Club,
    isMember: Boolean,
    hasPendingRequest: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Club Image/Icon
            ClubThumbnail(club = club)

            Spacer(modifier = Modifier.width(16.dp))

            // Club Info
            Column(modifier = Modifier.weight(1f)) {
                Text(club.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("${club.memberCount} members • ${club.description.take(50)}...",
                    style = MaterialTheme.typography.bodySmall)
            }

            // Status indicator
            ClubStatusIndicator(isMember = isMember, hasPendingRequest = hasPendingRequest, isActive = club.isActive)
        }
    }
}

@Composable
fun ClubThumbnail(club: com.example.clubapp.student.data.Club) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (club.imageUrl.isNotEmpty()) {
            Image(
                painter = rememberImagePainter(
                    data = club.imageUrl,
                    builder = {
                        crossfade(true)
                        error(androidx.compose.ui.res.painterResource(com.example.clubapp.R.drawable.ic_club_placeholder))
                    }
                ),
                contentDescription = "Club Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = club.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BrowseClubsScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    onClubClick: (String) -> Unit
) {
    // Implementation for BrowseClubsScreen
    val coroutineScope = rememberCoroutineScope()
    var clubs by remember { mutableStateOf<List<com.example.clubapp.student.data.Club>>(emptyList()) }
    var joinRequests by remember { mutableStateOf<List<com.example.clubapp.student.data.ClubJoinRequest>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            clubs = studentRepository.getAllClubs()
            joinRequests = studentRepository.getStudentJoinRequests()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(clubs) { club ->
            val pendingRequest = joinRequests.any { it.clubId == club.id && it.status == "Pending" }
            val isMember = club.members.contains(studentRepository.currentUserId)

            ClubListItem(
                club = club,
                isMember = isMember,
                hasPendingRequest = pendingRequest,
                onClick = { onClubClick(club.id) }
            )
            Divider()
        }
    }
}

@Composable
fun MyClubsScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    onClubClick: (String) -> Unit
) {
    // Implementation for MyClubsScreen
    val coroutineScope = rememberCoroutineScope()
    var clubs by remember { mutableStateOf<List<com.example.clubapp.student.data.Club>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            clubs = studentRepository.getStudentClubs()
        }
    }

    if (clubs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("You haven't joined any clubs yet", style = MaterialTheme.typography.bodyLarge)
                Text("Browse clubs to get started!", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(clubs) { club ->
                ClubListItem(
                    club = club,
                    isMember = true,
                    hasPendingRequest = false,
                    onClick = { onClubClick(club.id) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ClubStatusIndicator(isMember: Boolean, hasPendingRequest: Boolean, isActive: Boolean) {
    when {
        isMember -> {
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text("Member")
            }
        }
        hasPendingRequest -> {
            Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Text("Pending")
            }
        }
        else -> {
            if (isActive) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            } else {
                Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                    Text("Inactive")
                }
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
    Divider()
}
