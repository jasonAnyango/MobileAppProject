package com.example.clubapp.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubapp.data.model.Notification
import com.example.clubapp.data.model.Resource
import com.example.clubapp.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadUserNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true // Set loading to true initially
            _error.value = null

            notificationRepository.getNotificationsByUser(userId).collect { resource ->
                // This 'when' block is now exhaustive and handles all cases
                when (resource) {
                    is Resource.Success -> {
                        _isLoading.value = false // Stop loading on success
                        _notifications.value = resource.data ?: emptyList()
                    }
                    is Resource.Error -> {
                        _isLoading.value = false // Stop loading on error
                        _error.value = resource.message
                        _notifications.value = emptyList()
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true // Explicitly handle the loading state
                    }
                }
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            // The real-time listener will handle the UI update automatically
        }
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
            // The real-time listener will handle the UI update automatically
        }
    }
}
