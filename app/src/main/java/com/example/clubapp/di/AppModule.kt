package com.example.clubapp.di

import com.example.clubapp.admin.data.AdminRepository
//import com.example.clubapp.student.data.StudentRepository
import com.example.clubapp.auth.AuthViewModel
//import com.example.clubapp.student.ui.StudentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // --- 1. FIREBASE INSTANCES ---
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // --- 2. REPOSITORIES ---

    // Fixes the current crash!
    single { AdminRepository(get()) }

    // Prepares you for the Student pages
    //single { StudentRepository(get(), get()) }

    // --- 3. VIEW MODELS ---
    viewModel { AuthViewModel(get(), get()) }

    // For the Student Dashboard
    //viewModel { StudentViewModel(get()) }
}