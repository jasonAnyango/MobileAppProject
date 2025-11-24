package com.example.clubapp.clubleader.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubapp.R

@Composable
fun ManageClubScreen() {

    Scaffold(
        bottomBar = { ClubLeaderBottomNavigation() }
    ) { innerPadding ->

        var clubName by remember { mutableStateOf("") }
        var clubDescription by remember { mutableStateOf("") }
        var meetingTime by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // ---------------- PAGE TITLE ----------------
            Text(
                text = "Edit Club Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---------------- CLUB NAME ----------------
            StyledTextField(
                value = clubName,
                onValueChange = { clubName = it },
                label = "Club Name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- CLUB DESCRIPTION ----------------
            StyledTextField(
                value = clubDescription,
                onValueChange = { clubDescription = it },
                label = "Club Description",
                isLarge = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- MEETING TIME ----------------
            StyledTextField(
                value = meetingTime,
                onValueChange = { meetingTime = it },
                label = "Meeting Time"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- CLUB LOGO ----------------
            Image(
                painter = painterResource(id = R.drawable.tennis_club),
                contentDescription = "Club Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO open image picker */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Change Logo")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------------- SAVE BUTTON ----------------
            Button(
                onClick = { /* Save changes */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // Green
                    contentColor = Color.Black          // Black text
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isLarge: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLarge) 160.dp else 60.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF81C784),   // green
            unfocusedBorderColor = Color(0xFFB2DFDB), // light green
            cursorColor = Color(0xFF4CAF50)
        ),
        maxLines = if (isLarge) 6 else 1
    )
}
