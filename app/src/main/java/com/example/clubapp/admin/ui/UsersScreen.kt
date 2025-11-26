package com.example.clubapp.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clubapp.admin.data.MockAdminRepository

@Composable
fun AllUsersScreen(onUserClick: (String) -> Unit) {
    val users = remember { MockAdminRepository.getAllUsers() }
    // Scaffold removed to prevent double headers
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(users) { user ->
            ListItem(
                leadingContent = { Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Person, null, Modifier.padding(8.dp)) } },
                headlineContent = { Text(user.name) },
                supportingContent = { Text("${user.role} • ${user.email}") },
                modifier = Modifier.clickable { onUserClick(user.name) }
            )
            Divider()
        }
    }
}

@Composable
fun UserDetailsScreen(userId: String) {
    val user = remember(userId) { MockAdminRepository.getUserById(userId) }
    // Scaffold removed to prevent double headers
    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { 
            Text("User not found") 
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(user.name, style = MaterialTheme.typography.headlineMedium)
            Text(user.email, style = MaterialTheme.typography.bodyLarge)
            Text("Role: ${user.role}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            Text("Clubs Joined:", style = MaterialTheme.typography.titleMedium)
            user.clubsJoined.forEach { Text("• $it") }
            Spacer(Modifier.height(32.dp))
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Block, null); Spacer(Modifier.width(8.dp)); Text("Suspend User")
            }
        }
    }
}