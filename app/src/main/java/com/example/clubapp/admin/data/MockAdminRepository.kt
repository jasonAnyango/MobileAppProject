package com.example.clubapp.admin.data

object MockAdminRepository {

    // --- MOCK DATABASE ---
    private val clubs = mutableListOf(
        Club("c1", "Chess Club", "Strategies and Tournaments", 24, true, listOf("John Doe"), "Weekly Match"),
        Club("c2", "Robotics Club", "Building the future", 12, true, listOf("Jane Smith"), "Tech Fair"),
        Club("c3", "Debate Team", "Public speaking events", 30, true, listOf("Bob Jones"), "Regionals"),
        Club("c4", "Music Band", "Jam sessions", 18, true, listOf("Sarah Lee"), "Fall Concert")
    )

    private val applications = mutableListOf(
        ClubApplication("app1", "Photography Club", "Alice Smith", "Pending", "To take photos of events", "Capture moments", "u101"),
        ClubApplication("app2", "Cooking Class", "Gordon R.", "Pending", "Learn to cook basics", "Feed the students", "u102"),
        ClubApplication("app3", "Hiking Group", "Mike T.", "Pending", "Weekly hikes", "Explore nature", "u103")
    )

    private val users = mutableListOf(
        User("u1", "Alice Smith", "alice@uni.edu", "Student", listOf("Debate Team")),
        User("u2", "Bob Jones", "bob@uni.edu", "Club Lead", listOf("Debate Team")),
        User("u3", "Charlie Brown", "charlie@uni.edu", "Student", listOf("Robotics Club")),
        User("u4", "Diana Prince", "diana@uni.edu", "Admin", listOf())
    )

    private val events = mutableListOf(
        Event("e1", "Annual Tech Fair", "Nov 12, 2024", "Main Hall", "Robotics Club", "A showcase of tech."),
        Event("e2", "Music Concert", "Dec 05, 2024", "Auditorium", "Music Band", "End of year concert."),
        Event("e3", "Chess Tournament", "Nov 20, 2024", "Library", "Chess Club", "Open to all students.")
    )

    // --- ACCESS METHODS ---

    // CLUBS
    fun getAllClubs(): List<Club> = clubs
    fun getClubById(id: String): Club? = clubs.find { it.id == id || it.name == id }

    /**
     * Deactivates a club by setting its isActive status to false
     */
    fun deactivateClub(clubId: String) {
        val clubIndex = clubs.indexOfFirst { it.id == clubId || it.name == clubId }
        if (clubIndex != -1) {
            val club = clubs[clubIndex]
            clubs[clubIndex] = club.copy(isActive = false)
        }
    }

    /**
     * Activates a club by setting its isActive status to true
     */
    fun activateClub(clubId: String) {
        val clubIndex = clubs.indexOfFirst { it.id == clubId || it.name == clubId }
        if (clubIndex != -1) {
            val club = clubs[clubIndex]
            clubs[clubIndex] = club.copy(isActive = true)
        }
    }

    // APPLICATIONS
    fun getPendingApplications(): List<ClubApplication> = applications.filter { it.status == "Pending" }
    fun getApplicationById(id: String): ClubApplication? = applications.find { it.id == id || it.clubName == id }

    /**
     * Approves an application by changing its status to "Approved"
     * and optionally creates a new club
     */
    fun approveApplication(appId: String) {
        val appIndex = applications.indexOfFirst { it.id == appId || it.clubName == appId }
        if (appIndex != -1) {
            val app = applications[appIndex]
            // Update the application status
            applications[appIndex] = app.copy(status = "Approved")

            // Optionally create a new club when approved
            val newClub = Club(
                id = "c_new_${System.currentTimeMillis()}",
                name = app.clubName,
                description = app.purpose,
                memberCount = 1,
                isActive = true,
                admins = listOf(app.applicantName),
                recentEvent = "Club Founded"
            )
            clubs.add(newClub)
        }
    }

    /**
     * Rejects an application by changing its status to "Rejected"
     */
    fun rejectApplication(appId: String) {
        val appIndex = applications.indexOfFirst { it.id == appId || it.clubName == appId }
        if (appIndex != -1) {
            val app = applications[appIndex]
            // Update the application status to Rejected
            applications[appIndex] = app.copy(status = "Rejected")
        }
    }

    // USERS
    fun getAllUsers(): List<User> = users
    fun getUserById(id: String): User? = users.find { it.id == id || it.name == id }

    // EVENTS
    fun getAllEvents(): List<Event> = events
    fun getEventById(id: String): Event? = events.find { it.id == id || it.title == id }
}

