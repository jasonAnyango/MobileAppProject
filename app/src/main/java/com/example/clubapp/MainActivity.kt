package com.example.clubapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.clubapp.ui.theme.ClubAppTheme
import com.google.firebase.FirebaseApp // <--- 1. IMPORT THIS
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import com.example.clubapp.di.appModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 2. INITIALIZE FIREBASE (MANDATORY) ---
        // This must happen BEFORE setContent
        FirebaseApp.initializeApp(this)

        // --- 3. START KOIN (DEPENDENCY INJECTION) ---
        // We stop first to prevent "Koin already started" crashes during development
        stopKoin()
        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }

        setContent {
            ClubAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}