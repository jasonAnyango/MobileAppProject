package com.example.clubapp.ui.screens.clubleader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.clubapp.ui.components.CustomButton
import com.example.clubapp.viewmodel.clubleader.ClubLeaderViewModel
import com.example.clubapp.viewmodel.clubleader.EventManagementViewModel
import com.google.firebase.Timestamp
import org.koin.androidx.compose.koinViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    userId: String,
    clubId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventManagementViewModel = koinViewModel(),
    clubViewModel: ClubLeaderViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var maxAttendees by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val clubState by clubViewModel.state.collectAsState()

    LaunchedEffect(userId, clubId) {
        viewModel.init(userId, clubId)
        clubViewModel.init(userId, clubId)
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
                title = { Text("Create Event") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Event Details",
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

            // Date Picker Button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedDate != null) {
                        "Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate!!))}"
                    } else {
                        "Select Date"
                    }
                )
            }

            // Time Input (simplified)
            OutlinedTextField(
                value = selectedTime,
                onValueChange = { selectedTime = it },
                label = { Text("Time (HH:MM, e.g., 14:00)") },
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("14:00") }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            CustomButton(
                text = "Create Event",
                onClick = {
                    isSubmitting = true

                    // Validate inputs
                    if (title.isBlank() || description.isBlank() || location.isBlank() ||
                        selectedDate == null || selectedTime.isBlank()) {
                        snackbarMessage = "Please fill all required fields"
                        isSubmitting = false
                        return@CustomButton
                    }

                    // Parse time
                    val timeParts = selectedTime.split(":")
                    if (timeParts.size != 2) {
                        snackbarMessage = "Invalid time format. Use HH:MM"
                        isSubmitting = false
                        return@CustomButton
                    }

                    val hour = timeParts[0].toIntOrNull() ?: 0
                    val minute = timeParts[1].toIntOrNull() ?: 0

                    // Create timestamp
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDate!!
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                    }

                    val startTime = Timestamp(calendar.time)
                    val endTime = Timestamp(Date(calendar.timeInMillis + (2 * 60 * 60 * 1000))) // +2 hours

                    viewModel.createEvent(
                        title = title,
                        description = description,
                        location = location,
                        startTime = startTime,
                        endTime = endTime,
                        maxAttendees = maxAttendees.toIntOrNull() ?: 0,
                        clubName = clubState.club?.name ?: "",
                        imageUrl = ""
                    ) { success, message ->
                        isSubmitting = false
                        snackbarMessage = message
                        if (success) {
                            onNavigateBack()
                        }
                    }
                },
                isLoading = isSubmitting,
                enabled = !isSubmitting,
                icon = Icons.Default.Add
            )
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}