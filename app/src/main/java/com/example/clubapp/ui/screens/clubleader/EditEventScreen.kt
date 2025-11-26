package com.example.clubapp.ui.screens.clubleader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.clubapp.data.model.Event
import com.example.clubapp.data.model.Resource
import com.example.clubapp.data.repository.EventRepository
import com.example.clubapp.ui.components.CustomButton
import com.example.clubapp.viewmodel.clubleader.EventManagementViewModel
import com.google.firebase.Timestamp
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    userId: String,
    clubId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventManagementViewModel = koinViewModel(),
    eventRepository: EventRepository = koinInject()
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var maxAttendees by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(eventId) {
        viewModel.init(userId, clubId)

        when (val result = eventRepository.getEventById(eventId)) {
            is Resource.Success -> {
                event = result.data
                title = result.data?.title ?: ""
                description = result.data?.description ?: ""
                location = result.data?.location ?: ""
                maxAttendees = (result.data?.maxAttendees ?: 0).toString()
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
                title = { Text("Edit Event") },
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
                    Text("Event not found")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Event Details",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // Event Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        leadingIcon = {
                            Icon(Icons.Default.Event, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6
                    )

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Current Date/Time (read-only)
                    OutlinedTextField(
                        value = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                            .format(event!!.startTime.toDate()),
                        onValueChange = { },
                        label = { Text("Current Date & Time") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )

                    // Max Attendees
                    OutlinedTextField(
                        value = maxAttendees,
                        onValueChange = { maxAttendees = it.filter { char -> char.isDigit() } },
                        label = { Text("Max Attendees (0 for unlimited)") },
                        leadingIcon = {
                            Icon(Icons.Default.People, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Current Registrations
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Registrations")
                            Text(
                                text = "${event!!.registeredMemberIds.size}",
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Update Button
                    CustomButton(
                        text = "Update Event",
                        onClick = {
                            isSubmitting = true

                            val updatedEvent = event!!.copy(
                                title = title,
                                description = description,
                                location = location,
                                maxAttendees = maxAttendees.toIntOrNull() ?: 0,
                                updatedAt = Timestamp.now()
                            )

                            viewModel.updateEvent(updatedEvent) { success, message ->
                                isSubmitting = false
                                snackbarMessage = message
                                if (success) {
                                    onNavigateBack()
                                }
                            }
                        },
                        isLoading = isSubmitting,
                        enabled = !isSubmitting && title.isNotBlank() &&
                                description.isNotBlank() && location.isNotBlank(),
                        icon = Icons.Default.Save
                    )
                }
            }
        }
    }
}