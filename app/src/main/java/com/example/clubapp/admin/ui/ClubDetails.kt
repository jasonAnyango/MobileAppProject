package com.example.clubapp.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import com.example.clubapp.admin.data.AdminRepository
import com.example.clubapp.model.Club
import com.example.clubapp.model.ClubRegistration
import com.example.clubapp.model.User

@Composable
fun ClubDetailsScreen(clubId: String) {
    val repository: AdminRepository = get()
    val scope = rememberCoroutineScope()

    // --- STATE MANAGEMENT ---
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showMembersDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // --- DATA HOLDERS ---
    var currentClub by remember { mutableStateOf<Club?>(null) }
    var leaderUser by remember { mutableStateOf<User?>(null) }
    var memberList by remember { mutableStateOf<List<User>>(emptyList()) }

    // --- DATA FETCHING ---
    LaunchedEffect(clubId, refreshTrigger) {
        // 1. Get Club
        val allClubs = repository.getAllClubs()
        val foundClub = allClubs.find { it.id == clubId }
        currentClub = foundClub

        if (foundClub != null) {
            // 2. Get All Users to resolve Names from IDs
            val allUsers = repository.getAllUsers()

            // 3. Resolve Leader
            leaderUser = allUsers.find { it.uid == foundClub.leaderId }

            // 4. Resolve Members
            memberList = allUsers.filter { foundClub.memberIds.contains(it.uid) }
        }
    }

    // --- UI CONTENT ---
    if (currentClub == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val club = currentClub!!

        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

            // 1. Header Section
            Text(club.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            val statusColor = if (club.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val statusText = if (club.isActive) "Active" else "Inactive"

            Spacer(Modifier.height(8.dp))
            Badge(containerColor = statusColor) {
                Text(statusText, Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(Modifier.height(24.dp))

            // 2. Club Details
            DetailItem("Description", club.description)
            DetailItem("Mission", club.mission)

            Spacer(Modifier.height(16.dp))

            // 3. Leader Information
            Text("Club Leader", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            if (leaderUser != null) {
                ListItem(
                    headlineContent = { Text(leaderUser!!.fullName, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Student ID: ${leaderUser!!.uid}") },
                    leadingContent = {
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp))
                        }
                    }
                )
            } else {
                Text("Unknown Leader (ID: ${club.leaderId})", style = MaterialTheme.typography.bodyMedium)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 4. Members List (Clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMembersDialog = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Members", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text("${club.memberIds.size} Active Members", style = MaterialTheme.typography.titleMedium)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "View Members", tint = Color.Gray)
            }
            Divider()

            Spacer(Modifier.height(32.dp))

            // 5. Action Button (Activate/Deactivate)
            Button(
                onClick = { showStatusDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (club.isActive) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    val text = if (club.isActive) "Deactivate Club" else "Activate Club"
                    Text(text)
                }
            }
        }

        // --- DIALOG: MEMBER LIST ---
        if (showMembersDialog) {
            AlertDialog(
                onDismissRequest = { showMembersDialog = false },
                title = { Text("Club Members") },
                text = {
                    // Constrain height so it scrolls
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        if (memberList.isEmpty()) {
                            Text("No members found.")
                        } else {
                            LazyColumn {
                                items(memberList) { member ->
                                    // Check for special roles in the 'officers' map
                                    val roleTitle = club.officers[member.uid] ?: "Member"

                                    ListItem(
                                        headlineContent = { Text(member.fullName) },
                                        supportingContent = { Text(roleTitle) },
                                        leadingContent = {
                                            if (roleTitle != "Member") {
                                                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                                            } else {
                                                Icon(Icons.Default.Person, null, tint = Color.Gray)
                                            }
                                        }
                                    )
                                    Divider()
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMembersDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // --- DIALOG: CHANGE STATUS ---
        if (showStatusDialog) {
            val action = if (currentClub!!.isActive) "Deactivate" else "Activate"

            AlertDialog(
                onDismissRequest = { if (!isProcessing) showStatusDialog = false },
                title = { Text("$action Club?") },
                text = { Text("Are you sure you want to $action '${currentClub!!.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                repository.toggleClubStatus(currentClub!!.id, !currentClub!!.isActive)
                                refreshTrigger++ // Force reload
                                isProcessing = false
                                showStatusDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentClub!!.isActive) MaterialTheme.colorScheme.error else Color(0xFF4CAF50))
                    ) {
                        Text("Yes, $action")
                    }
                },
                dismissButton = {
                    if (!isProcessing) {
                        TextButton(onClick = { showStatusDialog = false }) { Text("Cancel") }
                    }
                }
            )
        }
    }
}

// ==========================================
//   PART 2: CLUB APPLICATION DETAILS
// ==========================================

@Composable
fun ClubApplicationDetailsScreen(applicationId: String) {
    val repository: AdminRepository = get()
    val scope = rememberCoroutineScope()

    // States
    var uiStatus by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Fetch Logic
    val appState = produceState<ClubRegistration?>(initialValue = null, key1 = applicationId) {
        value = repository.getPendingApplications().find { it.id == applicationId }
    }

    val application = appState.value
    val displayStatus = uiStatus ?: application?.status ?: "Loading..."

    if (application == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Proposed: ${application.clubName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            DetailItem("Applicant Name", application.applicantName)
            DetailItem("Applicant ID", application.applicantId)
            DetailItem("Mission", application.mission)
            DetailItem("Description", application.description)

            Spacer(Modifier.height(32.dp))

            if (displayStatus == "Pending") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

                    // Approve Button
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) { Text("Approve") }

                    // Reject Button
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                repository.rejectApplication(application.id)
                                uiStatus = "Rejected"
                                isProcessing = false
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) { Text(if(isProcessing) "..." else "Reject") }
                }
            } else {
                val color = if (displayStatus == "Approved") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                Card(colors = CardDefaults.cardColors(containerColor = color), modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Status: $displayStatus", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dialog: Approve
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { if(!isProcessing) showConfirmDialog = false },
                icon = { Icon(Icons.Default.Verified, contentDescription = null) },
                title = { Text("Approve Club?") },
                text = {
                    Text("This will create '${application.clubName}' as an active club and promote ${application.applicantName} to Club Leader.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                repository.approveApplication(application)
                                uiStatus = "Approved"
                                isProcessing = false
                                showConfirmDialog = false
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        if(isProcessing) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Text("Confirm")
                    }
                },
                dismissButton = {
                    if(!isProcessing) {
                        TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
                    }
                }
            )
        }
    }
}

// --- Helper Composable ---
@Composable
fun DetailItem(label: String, value: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
}