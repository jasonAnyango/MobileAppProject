package com.example.clubapp

import android.app.Application
import com.example.clubapp.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ClubApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ClubApplication)
            modules(appModules)
        }
    }
}