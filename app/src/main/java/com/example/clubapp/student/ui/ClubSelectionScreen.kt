package com.example.clubapp.student.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clubapp.model.Club
import com.example.clubapp.student.data.StudentRepository

@Composable
fun ClubSelectionScreen(
    studentRepository: StudentRepository,
    onClubSelected: (String) -> Unit // Callback with the selected Club ID
) {
    // Fetch the clubs managed by this user
    val managedClubs by produceState<List<Club>>(initialValue = emptyList()) {
        value = studentRepository.getClubsManagedByMe()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Select Club to Manage",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You are a leader of multiple clubs. Choose one to access the leader dashboard.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (managedClubs.isEmpty()) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(managedClubs) { club ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onClubSelected(club.id) },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            headlineContent = { Text(club.name, style = MaterialTheme.typography.titleMedium) },
                            supportingContent = { Text(club.mission, maxLines = 1) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, null) }
                        )
                    }
                }
            }
        }
    }
}