package com.example.clubapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.clubapp.clubleader.ui.AddAnnouncementScreen
import com.example.clubapp.clubleader.ui.AddEventScreen
import com.example.clubapp.clubleader.ui.AnnouncementsScreen
import com.example.clubapp.clubleader.ui.EventsScreen
import com.example.clubapp.clubleader.ui.ManageClubScreen
import com.example.clubapp.clubleader.ui.MembersScreen
import com.example.clubapp.ui.theme.ClubAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClubAppTheme {
                AddAnnouncementScreen()
            }
        }
    }
}
