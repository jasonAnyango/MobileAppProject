package com.example.clubapp.ui.screens.clubleader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.clubapp.ui.components.CustomButton
import com.example.clubapp.viewmodel.clubleader.ClubLeaderViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClubScreen(
    userId: String,
    clubId: String,
    onNavigateBack: () -> Unit,
    viewModel: ClubLeaderViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var clubName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Sports", "Academic", "Arts", "Technology", "Community")

    LaunchedEffect(userId, clubId) {
        viewModel.init(userId, clubId)
    }

    LaunchedEffect(state.club) {
        state.club?.let { club ->
            clubName = club.name
            description = club.description
            category = club.category
        }
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
                title = { Text("Manage Club") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.club == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Club Image
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AsyncImage(
                            model = state.club!!.coverImageUrl.ifEmpty { "https://via.placeholder.com/400x200?text=Club+Image" },
                            contentDescription = "Club Cover",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )

                        if (isEditing) {
                            Button(
                                onClick = { /* TODO: Image picker */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Change Cover Image")
                            }
                        }
                    }
                }

                // Club Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (state.club!!.status.name) {
                            "APPROVED" -> MaterialTheme.colorScheme.primaryContainer
                            "PENDING" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (state.club!!.status.name) {
                                "APPROVED" -> Icons.Default.CheckCircle
                                "PENDING" -> Icons.Default.HourglassEmpty
                                else -> Icons.Default.Cancel
                            },
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Status: ${state.club!!.status.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (state.club!!.status.name) {
                                    "APPROVED" -> "Your club is live and visible to students"
                                    "PENDING" -> "Waiting for admin approval"
                                    else -> "Your club was not approved"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Club Details Form
                OutlinedTextField(
                    value = clubName,
                    onValueChange = { clubName = it },
                    label = { Text("Club Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Groups, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing,
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing,
                    minLines = 4,
                    maxLines = 6
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    label = { Text("Category") },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { showCategoryDialog = true },
                            enabled = isEditing
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true
                )

                // Club Stats
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Members")
                            Text(
                                text = "${state.club!!.memberCount}",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Events")
                            Text(
                                text = "${state.events.size}",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pending Requests")
                            Text(
                                text = "${state.membershipRequests.size}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action Buttons
                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Reset values
                                clubName = state.club!!.name
                                description = state.club!!.description
                                category = state.club!!.category
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        CustomButton(
                            text = "Save Changes",
                            onClick = {
                                viewModel.updateClub(clubName, description, category) { success, message ->
                                    snackbarMessage = message
                                    if (success) {
                                        isEditing = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = clubName.isNotBlank() &&
                                    description.isNotBlank() &&
                                    category.isNotBlank(),
                            icon = Icons.Default.Save
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Category Selection Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    categories.forEach { cat ->
                        ListItem(
                            headlineContent = { Text(cat) },
                            leadingContent = {
                                RadioButton(
                                    selected = cat == category,
                                    onClick = {
                                        category = cat
                                        showCategoryDialog = false
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}