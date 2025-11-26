package com.example.clubapp.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubapp.data.model.*
import com.example.clubapp.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ClubBrowseState(
    val isLoading: Boolean = false,
    val clubs: List<Club> = emptyList(),
    val filteredClubs: List<Club> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All", "Sports", "Academic", "Arts", "Technology", "Community"),
    val error: String? = null
)

class ClubBrowseViewModel(
    private val clubRepository: ClubRepository,
    private val membershipRepository: MembershipRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClubBrowseState())
    val state: StateFlow<ClubBrowseState> = _state.asStateFlow()

    private var currentUserId: String = ""

    init {
        loadClubs()
    }

    fun setUserId(userId: String) {
        currentUserId = userId
    }

    private fun loadClubs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            clubRepository.getApprovedClubs().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val clubs = result.data ?: emptyList()
                        _state.value = _state.value.copy(
                            clubs = clubs,
                            filteredClubs = clubs,
                            isLoading = false
                        )
                        filterClubs()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        filterClubs()
    }

    fun onCategorySelected(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
        filterClubs()
    }

    private fun filterClubs() {
        val query = _state.value.searchQuery.lowercase()
        val category = _state.value.selectedCategory

        val filtered = _state.value.clubs.filter { club ->
            val matchesSearch = club.name.lowercase().contains(query) ||
                    club.description.lowercase().contains(query)
            val matchesCategory = category == "All" || club.category == category

            matchesSearch && matchesCategory
        }

        _state.value = _state.value.copy(filteredClubs = filtered)
    }

    fun requestToJoinClub(club: Club, onResult: (Boolean, String?) -> Unit) {
        if (currentUserId.isEmpty()) {
            onResult(false, "User not authenticated")
            return
        }

        viewModelScope.launch {
            val request = MembershipRequest(
                clubId = club.id,
                clubName = club.name,
                studentId = currentUserId,
                studentName = "",
                studentEmail = "",
                message = "I would like to join ${club.name}"
            )

            when (val result = membershipRepository.createRequest(request)) {
                is Resource.Success -> {
                    onResult(true, "Join request sent successfully!")
                }
                is Resource.Error -> {
                    onResult(false, result.message)
                }
                else -> {}
            }
        }
    }

    fun getClubById(clubId: String, onResult: (Club?) -> Unit) {
        viewModelScope.launch {
            when (val result = clubRepository.getClubById(clubId)) {
                is Resource.Success -> {
                    onResult(result.data)
                }
                is Resource.Error -> {
                    onResult(null)
                }
                else -> {}
            }
        }
    }
}