package com.example.clubapp.student.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JoinRequestsScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var joinRequests by remember { mutableStateOf<List<com.example.clubapp.student.data.ClubJoinRequest>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            joinRequests = studentRepository.getStudentJoinRequests()
        }
    }

    if (joinRequests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No join requests", style = MaterialTheme.typography.bodyLarge)
                Text("Join clubs to see your requests here", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(joinRequests) { request ->
                JoinRequestListItem(request)
                Divider()
            }
        }
    }
}

@Composable
fun JoinRequestListItem(request: com.example.clubapp.student.data.ClubJoinRequest) {
    val statusColor = when (request.status) {
        "Approved" -> MaterialTheme.colorScheme.primaryContainer
        "Rejected" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    ListItem(
        leadingContent = {
            Icon(Icons.Default.Groups, contentDescription = null)
        },
        headlineContent = {
            Text(request.clubName ?: "Unknown Club") // Fixed: Added null check
        },
        supportingContent = {
            Text("Requested on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(request.requestDate.toDate())}")
        },
        trailingContent = {
            Badge(containerColor = statusColor) {
                Text(request.status)
            }
        }
    )
}

@Composable
fun CreateClubRequestScreen(
    studentRepository: com.example.clubapp.student.data.StudentRepository,
    onSubmit: () -> Unit
) {
    var clubName by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var mission by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Create New Club Request",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold // Fixed: Added FontWeight import
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
            value = purpose,
            onValueChange = { purpose = it },
            label = { Text("Purpose") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mission,
            onValueChange = { mission = it },
            label = { Text("Mission Statement") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (clubName.isNotBlank() && purpose.isNotBlank() && mission.isNotBlank()) {
                    isLoading = true
                    coroutineScope.launch {
                        val success = studentRepository.submitClubCreationRequest(
                            clubName = clubName,
                            purpose = purpose,
                            mission = mission
                        )
                        isLoading = false
                        if (success) {
                            showSuccessDialog = true
                        }
                    }
                }
            },
            enabled = clubName.isNotBlank() && purpose.isNotBlank() && mission.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Send, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isLoading) "Submitting..." else "Submit Request")
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Request Submitted") },
            text = { Text("Your club creation request has been submitted for admin approval.") },
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
