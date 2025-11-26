package com.example.clubapp.ui.screens.clubleader

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
import com.example.clubapp.data.model.MembershipRequest
import com.example.clubapp.ui.components.EmptyState
import com.example.clubapp.ui.components.toFormattedDate
import com.example.clubapp.viewmodel.clubleader.ClubLeaderViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberRequestsScreen(
    userId: String,
    clubId: String,
    onNavigateBack: () -> Unit,
    viewModel: ClubLeaderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<MembershipRequest?>(null) }

    LaunchedEffect(userId, clubId) {
        viewModel.init(userId, clubId)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Membership Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.membershipRequests.isEmpty()) {
            EmptyState(
                icon = Icons.Default.CheckCircle,
                message = "No pending membership requests",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${state.membershipRequests.size} Pending Requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(state.membershipRequests) { request ->
                    MembershipRequestCard(
                        request = request,
                        onApprove = {
                            selectedRequest = request
                            showApproveDialog = true
                        },
                        onReject = {
                            selectedRequest = request
                            showRejectDialog = true
                        }
                    )
                }
            }
        }
    }

    // Approve Dialog
    if (showApproveDialog && selectedRequest != null) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Approve Member") },
            text = {
                Text("Approve ${selectedRequest!!.studentName} to join ${state.club?.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveMembershipRequest(
                            selectedRequest!!.id,
                            selectedRequest!!.studentId
                        ) { success, message ->
                            snackbarMessage = message
                            showApproveDialog = false
                            selectedRequest = null
                        }
                    }
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reject Dialog
    if (showRejectDialog && selectedRequest != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reject Request") },
            text = {
                Text("Reject ${selectedRequest!!.studentName}'s request to join ${state.club?.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectMembershipRequest(selectedRequest!!.id) { success, message ->
                            snackbarMessage = message
                            showRejectDialog = false
                            selectedRequest = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MembershipRequestCard(
    request: MembershipRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.studentEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Requested: ${request.createdAt.toFormattedDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (request.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = request.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}