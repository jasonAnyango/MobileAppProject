package com.example.clubapp.admin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportsScreen() {
    // Scaffold removed to prevent double headers
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Admin Reports", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Engagement charts and statistics will appear here.")
        // Placeholder for charts
        Spacer(Modifier.height(32.dp))
        Button(onClick = { /* Export Logic */ }) {
            Text("Export Data as CSV")
        }
    }
}