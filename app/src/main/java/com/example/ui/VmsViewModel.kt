package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.VisitorRecord
import com.example.data.VisitorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class VmsRole(val displayName: String) {
    RECEPTIONIST("Reception Security"),
    VISITOR("Visitor Kiosk"),
    HOST("Host Employee"),
    FACILITY_MANAGER("Facility Manager"),
    ADMIN("Admin Panel")
}

data class VisitorDraft(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val designation: String = "",
    val hostName: String = "",
    val department: String = "",
    val floor: String = "Ground Floor",
    val meetingRoom: String = "Meeting Room A",
    val purpose: String = "Official Meeting",
    val expectedDurationMinutes: Int = 60,
    val photoUri: String = "",
    val idProofUri: String = "",
    val vehicleNumber: String = ""
)

class VmsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VisitorRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VisitorRepository(database.visitorDao())
    }

    // Role state
    private val _currentRole = MutableStateFlow(VmsRole.RECEPTIONIST)
    val currentRole: StateFlow<VmsRole> = _currentRole.asStateFlow()

    // Active host user profile for host review panel
    private val _selectedHostUser = MutableStateFlow("Pushpa")
    val selectedHostUser: StateFlow<String> = _selectedHostUser.asStateFlow()

    // Host list
    private val _hostsList = MutableStateFlow(listOf(
        "Pushpa", "Anjali", "Vikram", "Rajesh", "Sunita", "David"
    ))
    val hostsList: StateFlow<List<String>> = _hostsList.asStateFlow()

    // Departments list
    val departmentsList = listOf(
        "Technology", "Human Resources", "Finance & Accounting",
        "Operations", "Engineering", "Sales & Marketing", "Facilities"
    )

    // Meeting Purposes
    val purposeList = listOf(
        "Official Meeting", "Interview", "Vendor Visit",
        "Delivery", "Maintenance", "Guest Visit"
    )

    // Floyd / room defaults
    val floorList = listOf("Ground Floor", "1st Floor", "2nd Floor", "3rd Floor", "4th Floor", "Penthouse")
    val roomList = listOf("Boardroom Alpha", "Huddle Room 101", "Conference Suite B", "Visitor Lounge", "Engineering Lab", "Cafeteria Meeting Area")

    // Live list of all visitors
    val allVisitorsState: StateFlow<List<VisitorRecord>> = repository.allVisitors
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setRole(role: VmsRole) {
        _currentRole.value = role
    }

    fun setSelectedHostUser(hostName: String) {
        _selectedHostUser.value = hostName
    }

    fun addCustomHost(name: String) {
        if (name.isNotBlank() && !_hostsList.value.contains(name)) {
            _hostsList.value = _hostsList.value + name
        }
    }

    // New visitor registration draft state
    var registrationDraft by mutableStateOf(VisitorDraft())
        private set

    fun updateDraft(updater: (VisitorDraft) -> VisitorDraft) {
        registrationDraft = updater(registrationDraft)
    }

    fun resetDraft() {
        registrationDraft = VisitorDraft(
            hostName = _hostsList.value.firstOrNull() ?: "Pushpa",
            department = departmentsList.firstOrNull() ?: "Technology"
        )
    }

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Filtered visitors
    val filteredVisitors: StateFlow<List<VisitorRecord>> = combine(
        allVisitorsState,
        _searchQuery
    ) { visitors, query ->
        if (query.isBlank()) {
            visitors
        } else {
            visitors.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.visitorId.contains(query, ignoreCase = true) ||
                it.company.contains(query, ignoreCase = true) ||
                it.hostName.contains(query, ignoreCase = true) ||
                it.otpCode.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Core actions
    fun registerVisitor(onComplete: (VisitorRecord) -> Unit = {}) {
        viewModelScope.launch {
            val serialNumber = 1000 + Random.nextInt(9000)
            val vId = "VMS-2026-$serialNumber"
            val otp = String.format("%06d", Random.nextInt(100000, 999999))
            
            val record = VisitorRecord(
                visitorId = vId,
                name = registrationDraft.name.ifBlank { "Guest User" },
                phone = registrationDraft.phone.ifBlank { "000-000-0000" },
                email = registrationDraft.email,
                company = registrationDraft.company.ifBlank { "Independent" },
                designation = registrationDraft.designation.ifBlank { "Visitor" },
                hostName = registrationDraft.hostName.ifBlank { "Pushpa" },
                department = registrationDraft.department.ifBlank { "Technology" },
                floor = registrationDraft.floor,
                meetingRoom = registrationDraft.meetingRoom,
                purpose = registrationDraft.purpose,
                checkInTime = 0L,
                expectedCheckOutTime = System.currentTimeMillis() + (registrationDraft.expectedDurationMinutes * 60 * 1000L),
                actualCheckOutTime = 0L,
                status = "PENDING", // PENDING host review immediately
                photoUri = registrationDraft.photoUri,
                idProofUri = registrationDraft.idProofUri,
                vehicleNumber = registrationDraft.vehicleNumber,
                otpCode = otp
            )

            val id = repository.insertVisitor(record)
            val savedRecord = record.copy(id = id.toInt())
            resetDraft()
            onComplete(savedRecord)
        }
    }

    fun approveVisitor(record: VisitorRecord) {
        viewModelScope.launch {
            repository.updateVisitor(record.copy(status = "APPROVED"))
        }
    }

    fun rejectVisitor(record: VisitorRecord) {
        viewModelScope.launch {
            repository.updateVisitor(record.copy(status = "REJECTED"))
        }
    }

    fun checkInVisitor(record: VisitorRecord) {
        viewModelScope.launch {
            repository.updateVisitor(record.copy(
                status = "CHECKED_IN",
                checkInTime = System.currentTimeMillis()
            ))
        }
    }

    fun checkOutVisitor(record: VisitorRecord) {
        viewModelScope.launch {
            repository.updateVisitor(record.copy(
                status = "CHECKED_OUT",
                actualCheckOutTime = System.currentTimeMillis()
            ))
        }
    }

    fun checkInWithOtp(otp: String, onSuccess: (VisitorRecord) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val visitor = repository.getVisitorByOtp(otp)
            if (visitor == null) {
                onError("Pass code $otp not found. Please register first.")
                return@launch
            }
            if (visitor.status == "PENDING") {
                onError("Pass code found but host approval is still Pending.")
                return@launch
            }
            if (visitor.status == "REJECTED") {
                onError("This Visit Request was Rejected by the Host.")
                return@launch
            }
            if (visitor.status == "CHECKED_IN") {
                onError("Visitor is already Checked In.")
                return@launch
            }
            if (visitor.status == "CHECKED_OUT") {
                onError("Visitor has already Checked Out.")
                return@launch
            }

            val updated = visitor.copy(status = "CHECKED_IN", checkInTime = System.currentTimeMillis())
            repository.updateVisitor(updated)
            onSuccess(updated)
        }
    }

    fun checkOutWithOtp(otp: String, onSuccess: (VisitorRecord) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val visitor = repository.getVisitorByOtp(otp)
            if (visitor == null) {
                onError("Pass code not found.")
                return@launch
            }
            if (visitor.status != "CHECKED_IN") {
                onError("Visitor is not currently Checked In. Use check-in first.")
                return@launch
            }

            val updated = visitor.copy(status = "CHECKED_OUT", actualCheckOutTime = System.currentTimeMillis())
            repository.updateVisitor(updated)
            onSuccess(updated)
        }
    }

    fun deleteRecord(record: VisitorRecord) {
        viewModelScope.launch {
            repository.deleteVisitor(record)
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // Seed realistic sample data for quick demo/review
    fun seedSampleData() {
        viewModelScope.launch {
            val names = listOf(
                Pair("George Reddy", "ABC Technologies"),
                Pair("Sophia Loren", "Global Consulting Co"),
                Pair("Alex Mercer", "Tesla Design Labs"),
                Pair("Meera Nair", "Wipro Systems"),
                Pair("Michael Chang", "Apex Media Group")
            )
            val purposes = listOf("Official Meeting", "Interview", "Vendor Visit", "Guest Visit")
            val designations = listOf("Technical Director", "Candidate", "Senior Consultant", "HR Partner", "PR Specialist")

            names.forEachIndexed { i, (name, company) ->
                val vId = "VMS-2026-${2100 + i}"
                val otp = String.format("%06d", 321520 + i)
                val isPastCheckedIn = i % 2 == 0
                val status = if (isPastCheckedIn) "CHECKED_IN" else if (i == 1) "PENDING" else "APPROVED"
                val checkIn = if (status == "CHECKED_IN") System.currentTimeMillis() - (120 * 60 * 1000L * i) else 0L

                val record = VisitorRecord(
                    visitorId = vId,
                    name = name,
                    phone = "987-555-010$i",
                    email = "${name.lowercase().replace(" ", "")}@domain.com",
                    company = company,
                    designation = designations[i % designations.size],
                    hostName = _hostsList.value[i % _hostsList.value.size],
                    department = departmentsList[i % departmentsList.size],
                    floor = floorList[i % floorList.size],
                    meetingRoom = roomList[i % roomList.size],
                    purpose = purposes[i % purposes.size],
                    checkInTime = checkIn,
                    expectedCheckOutTime = System.currentTimeMillis() + (180 * 60 * 1000L),
                    actualCheckOutTime = 0L,
                    status = status,
                    photoUri = "preset_${i + 1}", // Mark as custom presets
                    idProofUri = "id_proof_preset",
                    vehicleNumber = "KA-03-MK-715$i",
                    otpCode = otp
                )
                repository.insertVisitor(record)
            }
        }
    }
}
