package com.example.clubapp.di

import com.example.clubapp.data.repository.*
import com.example.clubapp.viewmodel.admin.AdminViewModel
import com.example.clubapp.viewmodel.auth.AuthViewModel
import com.example.clubapp.viewmodel.clubleader.ClubLeaderViewModel
import com.example.clubapp.viewmodel.clubleader.EventManagementViewModel
import com.example.clubapp.viewmodel.notification.NotificationViewModel
import com.example.clubapp.viewmodel.student.ClubBrowseViewModel
import com.example.clubapp.viewmodel.student.StudentHomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Firebase Module
val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
}

// Repository Module
val repositoryModule = module {
    single { AuthRepository(get(), get()) }
    single { UserRepository(get(), get()) }
    single { ClubRepository(get(), get()) }
    single { EventRepository(get(), get()) }
    single { MembershipRepository(get()) }
    single { NotificationRepository(get()) }
}

// ViewModel Module
val viewModelModule = module {
    // Auth ViewModels
    viewModel { AuthViewModel(get(), get()) }

    // Student ViewModels
    viewModel { StudentHomeViewModel(get(), get(), get(), get()) }
    viewModel { ClubBrowseViewModel(get(), get()) }
    viewModel { NotificationViewModel(get()) }

    // Club Leader ViewModels
    viewModel { ClubLeaderViewModel(get(), get(), get(), get()) }
    viewModel { EventManagementViewModel(get(), get()) }

    // Admin ViewModels
    viewModel { AdminViewModel(get(), get(), get(), get(), get()) }
}

// All modules combined
val appModules = listOf(
    firebaseModule,
    repositoryModule,
    viewModelModule
)
