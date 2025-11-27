package com.example.clubapp

import android.app.Application
import com.example.clubapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClubApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ClubApplication)
            modules(appModule)
        }
    }
}