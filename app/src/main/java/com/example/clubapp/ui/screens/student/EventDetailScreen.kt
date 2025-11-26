package com.example.clubapp.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.clubapp.data.model.Event
import com.example.clubapp.data.model.Resource
import com.example.clubapp.data.repository.EventRepository
import com.example.clubapp.ui.components.toFormattedDate
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    userId: String,
    onNavigateBack: () -> Unit,
    eventRepository: EventRepository = koinInject()
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRegistered by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(eventId) {
        when (val result = eventRepository.getEventById(eventId)) {
            is Resource.Success -> {
                event = result.data
                isRegistered = result.data?.registeredMemberIds?.contains(userId) == true
                isLoading = false
            }
            is Resource.Error -> {
                isLoading = false
            }
            else -> {}
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
                title = { Text("Event Details") },
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
        floatingActionButton = {
            if (event != null && !isRegistered) {
                ExtendedFloatingActionButton(
                    onClick = { showRegisterDialog = true },
                    icon = { Icon(Icons.Default.EventAvailable, "Register") },
                    text = { Text("Register") },
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            event == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Event not found",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Event Image
                    item {
                        AsyncImage(
                            model = event!!.imageUrl.ifEmpty { "https://via.placeholder.com/400x250?text=Event" },
                            contentDescription = event!!.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Event Info Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Club Name
                                AssistChip(
                                    onClick = { },
                                    label = { Text(event!!.clubName) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Groups,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Event Title
                                Text(
                                    text = event!!.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Date & Time
                                EventInfoRow(
                                    icon = Icons.Default.CalendarToday,
                                    label = "Date & Time",
                                    value = event!!.startTime.toFormattedDate()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Location
                                EventInfoRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Location",
                                    value = event!!.location
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Attendees
                                EventInfoRow(
                                    icon = Icons.Default.People,
                                    label = "Attendees",
                                    value = if (event!!.maxAttendees > 0) {
                                        "${event!!.registeredMemberIds.size} / ${event!!.maxAttendees}"
                                    } else {
                                        "${event!!.registeredMemberIds.size} registered"
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Status
                                EventInfoRow(
                                    icon = Icons.Default.Info,
                                    label = "Status",
                                    value = event!!.status.name.replace("_", " ")
                                )

                                // Registration Status
                                if (isRegistered) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "You're registered for this event",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Description Section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "About This Event",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = event!!.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Register Dialog
    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            icon = { Icon(Icons.Default.EventAvailable, null) },
            title = { Text("Register for Event") },
            text = {
                Column {
                    Text("Do you want to register for this event?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event!!.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (event!!.maxAttendees > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Spots available: ${event!!.maxAttendees - event!!.registeredMemberIds.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Register for event
                        kotlinx.coroutines.GlobalScope.launch {
                            when (eventRepository.registerForEvent(eventId, userId)) {
                                is Resource.Success -> {
                                    isRegistered = true
                                    snackbarMessage = "Successfully registered!"
                                }
                                is Resource.Error -> {
                                    snackbarMessage = "Failed to register"
                                }
                                else -> {}
                            }
                        }
                        showRegisterDialog = false
                    },
                    enabled = event!!.maxAttendees == 0 ||
                            event!!.registeredMemberIds.size < event!!.maxAttendees
                ) {
                    Text("Register")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EventInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}