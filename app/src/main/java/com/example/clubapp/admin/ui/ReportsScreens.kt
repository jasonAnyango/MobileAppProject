package com.example.clubapp.admin.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import com.example.clubapp.admin.data.AdminRepository
import java.io.File
import java.io.FileOutputStream

@Composable
fun ReportsScreen() {
    val repository: AdminRepository = get()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    var stats by remember { mutableStateOf<AdminRepository.DashboardStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load Data
    LaunchedEffect(Unit) {
        stats = repository.getDashboardStats()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (stats != null) {
            val data = stats!!

            // Use a LazyColumn so the screen is scrollable
            LazyColumn(
                modifier = Modifier.weight(1f), // Takes remaining space
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. KEY METRICS ROW
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Students", data.totalStudents.toString(), Icons.Default.Person, Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f))
                        StatCard("Clubs", data.totalClubs.toString(), Icons.Default.Groups, Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f))
                        StatCard("Events", data.totalEvents.toString(), Icons.Default.Event, Color(0xFFFFF3E0), Color(0xFFEF6C00), Modifier.weight(1f))
                    }
                }

                // 2. CLUB HEALTH (Active vs Inactive)
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Club Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))

                            // Visual Bar
                            val total = data.totalClubs.toFloat().coerceAtLeast(1f)
                            val activePct = data.activeClubs / total

                            Row(Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp))) {
                                Box(Modifier.weight(activePct).fillMaxHeight().background(Color(0xFF4CAF50)))
                                if (data.inactiveClubs > 0) {
                                    Box(Modifier.weight(1f - activePct).fillMaxHeight().background(Color(0xFFE57373)))
                                }
                            }

                            // Legend
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${data.activeClubs} Active", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Text("${data.inactiveClubs} Inactive", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. TOP PERFORMING CLUBS (Bar Chart Logic)
                item {
                    Text("Top 5 Popular Clubs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(data.topClubs) { club ->
                    val maxMembers = data.topClubs.maxOfOrNull { it.memberIds.size } ?: 1
                    val progress = club.memberIds.size.toFloat() / maxMembers.toFloat()

                    Column(Modifier.padding(vertical = 4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(club.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("${club.memberIds.size} members", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. EXPORT BUTTON
            Button(
                onClick = {
                    scope.launch {
                        shareCsvData(context, repository.generateCsvExport())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export Club Data (CSV)")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, bgColor: Color, iconColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = iconColor)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = iconColor)
            Text(title, style = MaterialTheme.typography.bodySmall, color = iconColor.copy(alpha = 0.8f))
        }
    }
}

// Helper to share the CSV file
fun shareCsvData(context: Context, csvData: String) {
    try {
        // 1. Create File
        val fileName = "club_reports.csv"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { it.write(csvData.toByteArray()) }

        // 2. Get Uri
        // Note: This requires a FileProvider in AndroidManifest. If simpler sharing is needed, just share text.
        // For simplicity in this snippet, we will share as Plain Text Intent first.

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvData) // Sharing text content directly for MVP
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Club App Report")
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Report")
        context.startActivity(shareIntent)

    } catch (e: Exception) {
        Toast.makeText(context, "Error exporting data", Toast.LENGTH_SHORT).show()
    }
}