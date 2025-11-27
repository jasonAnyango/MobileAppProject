package com.example.clubapp.student.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.clubapp.student.data.StudentRepository
import com.example.clubapp.model.ClubRegistration // <--- Shared Model

@Composable
fun JoinRequestsScreen(
    studentRepository: StudentRepository
) {
    val coroutineScope = rememberCoroutineScope()
    // Switch to ClubRegistration (Proposals)
    var myProposals by remember { mutableStateOf<List<ClubRegistration>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Fetch the proposals using the new Repo function
            myProposals = studentRepository.getMyClubProposals()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Club Proposals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (myProposals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No proposals submitted", style = MaterialTheme.typography.bodyLarge)
                    Text("Start a new club to see status here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(myProposals) { request ->
                    JoinRequestListItem(request)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun JoinRequestListItem(request: ClubRegistration) {
    val statusColor = when (request.status) {
        "Approved" -> MaterialTheme.colorScheme.primaryContainer
        "Rejected" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val textColor = when (request.status) {
        "Approved" -> MaterialTheme.colorScheme.onPrimaryContainer
        "Rejected" -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    ListItem(
        leadingContent = {
            Icon(Icons.Default.Groups, contentDescription = null)
        },
        headlineContent = {
            Text(request.clubName.ifEmpty { "Unknown Club" }, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(request.mission, maxLines = 1)
        },
        trailingContent = {
            Badge(containerColor = statusColor) {
                Text(
                    text = request.status,
                    modifier = Modifier.padding(4.dp),
                    color = textColor
                )
            }
        }
    )
}

@Composable
fun CreateClubRequestScreen(
    studentRepository: StudentRepository,
    onSubmit: () -> Unit
) {
    var clubName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") } // Renamed from purpose to match Model
    var mission by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch user name for the form
    val userState = produceState<com.example.clubapp.model.User?>(initialValue = null) {
        value = studentRepository.getMyUserProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Propose New Club",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = clubName,
            onValueChange = { clubName = it },
            label = { Text("Club Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mission,
            onValueChange = { mission = it },
            label = { Text("Mission Statement") },
            placeholder = { Text("Short slogan e.g. 'Coding for everyone'") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Full Description") },
            placeholder = { Text("What will your club do?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (clubName.isNotBlank() && description.isNotBlank() && mission.isNotBlank()) {
                    isLoading = true
                    coroutineScope.launch {
                        val request = ClubRegistration(
                            clubName = clubName,
                            description = description,
                            mission = mission,
                            applicantName = userState.value?.fullName ?: "Student",
                            // IDs are handled in Repo
                        )

                        val success = studentRepository.submitClubProposal(request)

                        isLoading = false
                        if (success) {
                            showSuccessDialog = true
                        }
                    }
                }
            },
            enabled = clubName.isNotBlank() && description.isNotBlank() && mission.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Send, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isLoading) "Submitting..." else "Submit Proposal")
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Request Submitted") },
            text = { Text("Your club creation request has been submitted to the Admin. You can check the status in the 'My Requests' tab.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onSubmit()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}