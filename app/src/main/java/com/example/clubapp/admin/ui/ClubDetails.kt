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
import com.example.clubapp.admin.data.MockAdminRepository

@Composable
fun ClubDetailsScreen(clubId: String) {
    var club by remember { mutableStateOf(MockAdminRepository.getClubById(clubId)) }

    fun refreshClub() { club = MockAdminRepository.getClubById(clubId) }

    // No Scaffold here - handled by parent NavGraph
    if (club == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Club Not Found") }
    } else {
        val currentClub = club!!
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(currentClub.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            val statusColor = if(currentClub.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val statusText = if(currentClub.isActive) "Active" else "Inactive"

            Spacer(Modifier.height(8.dp))
            Badge(containerColor = statusColor) {
                Text(statusText, Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(Modifier.height(24.dp))
            DetailItem("Description", currentClub.description)
            DetailItem("Admin(s)", currentClub.admins.joinToString())
            DetailItem("Members", "${currentClub.memberCount}")
            DetailItem("Recent Event", currentClub.recentEvent ?: "None")

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (currentClub.isActive) MockAdminRepository.deactivateClub(currentClub.id)
                    else MockAdminRepository.activateClub(currentClub.id)
                    refreshClub()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (currentClub.isActive) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth()
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
    val application = remember(applicationId) { MockAdminRepository.getApplicationById(applicationId) }
    var currentStatus by remember { mutableStateOf(application?.status ?: "Unknown") }

    // No Scaffold here
    if (application == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Application Not Found") }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Proposed: ${application.clubName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            DetailItem("Applicant", application.applicantName)
            DetailItem("Mission", application.mission)
            DetailItem("Purpose", application.purpose)
            Spacer(Modifier.height(32.dp))

            if (currentStatus == "Pending") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { MockAdminRepository.approveApplication(application.id); currentStatus = "Approved" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) { Text("Approve") }

                    Button(
                        onClick = { MockAdminRepository.rejectApplication(application.id); currentStatus = "Rejected" },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) { Text("Reject") }
                }
            } else {
                Text("Status: $currentStatus", style = MaterialTheme.typography.titleLarge)
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