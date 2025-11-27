package com.example.clubapp.di

import com.example.clubapp.admin.data.AdminRepository
import com.example.clubapp.auth.AuthViewModel
import com.example.clubapp.student.data.StudentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // 1. Firebase Instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // 2. Repositories
    single { AdminRepository(get()) }
    single { StudentRepository(get(), get()) }

    // 3. ViewModels
    // Only Auth needs a ViewModel right now.
    // Student and Admin use the Repository directly in the UI.
    viewModel { AuthViewModel(get(), get()) }
}