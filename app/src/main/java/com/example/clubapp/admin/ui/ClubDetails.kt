package com.example.clubapp.admin.ui

import androidx.compose.foundation.layout.*
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

@Composable
fun ClubDetailsScreen(clubId: String) {
    val repository: AdminRepository = get()
    val scope = rememberCoroutineScope()

    // 1. Fetch the specific club (We filter the full list for now)
    val clubState = produceState<Club?>(initialValue = null, key1 = clubId) {
        // Since we don't have a getClubById in repo yet, we find it in the list
        value = repository.getAllClubs().find { it.id == clubId }
    }

    val currentClub = clubState.value

    // No Scaffold here - handled by parent NavGraph
    if (currentClub == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

            // Header
            Text(currentClub.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            val statusColor = if(currentClub.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val statusText = if(currentClub.isActive) "Active" else "Inactive"

            Spacer(Modifier.height(8.dp))
            Badge(containerColor = statusColor) {
                Text(statusText, Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Details (Updated to match your new Data Model)
            DetailItem("Description", currentClub.description)
            DetailItem("Mission", currentClub.mission)
            DetailItem("Leader ID", currentClub.leaderId.ifEmpty { "N/A" })
            DetailItem("Members Count", "${currentClub.memberIds.size}")

            // Note: recentEvent was removed from your new model, so I removed it here to prevent errors.

            Spacer(Modifier.height(32.dp))

            // Action Button
            // Note: We need to add toggleClubStatus to your Repository for this to work perfectly.
            // For now, I have commented out the logic to prevent crashing until you add that function.
            Button(
                onClick = {
                    scope.launch {
                        // TODO: Add toggleClubStatus(id, boolean) to AdminRepository
                        // repository.toggleClubStatus(currentClub.id, !currentClub.isActive)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (currentClub.isActive) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Disabled until repo function is added
            ) {
                val icon = if (currentClub.isActive) Icons.Default.Block else Icons.Default.CheckCircle
                val text = if (currentClub.isActive) "Deactivate Club" else "Activate Club"
                Icon(icon, null); Spacer(Modifier.width(8.dp)); Text(text)
            }
        }
    }
}

@Composable
fun ClubApplicationDetailsScreen(applicationId: String) {
    val repository: AdminRepository = get()
    val scope = rememberCoroutineScope()

    // 1. Fetch the application
    val appState = produceState<ClubRegistration?>(initialValue = null, key1 = applicationId) {
        value = repository.getPendingApplications().find { it.id == applicationId }
    }

    val application = appState.value

    // Local state to update UI immediately after clicking approve/reject
    var uiStatus by remember { mutableStateOf<String?>(null) }
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
                    Button(
                        onClick = {
                            scope.launch {
                                repository.approveApplication(application)
                                uiStatus = "Approved"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) { Text("Approve") }

                    Button(
                        onClick = {
                            scope.launch {
                                repository.rejectApplication(application.id)
                                uiStatus = "Rejected"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) { Text("Reject") }
                }
            } else {
                Text("Status: $displayStatus", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
    Divider()
}