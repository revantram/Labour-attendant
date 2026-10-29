package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.data.model.MonthlySlip
import com.example.data.model.PaymentRecord
import com.example.data.repository.LabourRepository
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    ATTENDANCE,
    LABOURERS,
    FOLDERS,
    SALARY_SLIPS,
    WORKER_PORTAL
}

enum class AuthMode {
    ADMIN_LOGGED_IN,
    WORKER_VIEW,
    LOGIN_REQUIRED
}

class LabourViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LabourRepository
    private val prefs = application.getSharedPreferences("labour_app_prefs", Context.MODE_PRIVATE)

    // Admin Credentials & Settings
    private val _adminPhone = MutableStateFlow(prefs.getString("admin_phone", "9988776655") ?: "9988776655")
    val adminPhone: StateFlow<String> = _adminPhone.asStateFlow()

    private val _adminPin = MutableStateFlow(prefs.getString("admin_pin", "1234") ?: "1234")
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    private val _businessName = MutableStateFlow(prefs.getString("business_name", "Kisan & Shramik Construction") ?: "Kisan & Shramik Construction")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    // Current Mode: Admin by default (with seamless switcher), or Worker Self-Service
    private val _authMode = MutableStateFlow(AuthMode.ADMIN_LOGGED_IN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    // Selected Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.ATTENDANCE)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Date for Daily Attendance
    private val _selectedDate = MutableStateFlow(DateUtils.todayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Folder Filter (null = All)
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    // Month for Salary Slips
    private val _selectedMonth = MutableStateFlow(DateUtils.currentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Worker View State (Labourer looking up own record via phone)
    private val _workerSearchPhone = MutableStateFlow("")
    val workerSearchPhone: StateFlow<String> = _workerSearchPhone.asStateFlow()

    private val _activeWorker = MutableStateFlow<Labourer?>(null)
    val activeWorker: StateFlow<Labourer?> = _activeWorker.asStateFlow()

    private val _workerSlip = MutableStateFlow<MonthlySlip?>(null)
    val workerSlip: StateFlow<MonthlySlip?> = _workerSlip.asStateFlow()

    // Generated slip for preview / sending via WhatsApp
    private val _previewSlip = MutableStateFlow<MonthlySlip?>(null)
    val previewSlip: StateFlow<MonthlySlip?> = _previewSlip.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = LabourRepository(db.labourDao())

        // Check if initial seeding needed
        viewModelScope.launch {
            val list = repository.allLabourers.firstOrNull()
            if (list.isNullOrEmpty()) {
                repository.seedInitialDataIfEmpty()
            }
        }
    }

    val folders: StateFlow<List<Folder>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLabourers: StateFlow<List<Labourer>> = repository.allLabourers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered labourers based on selected folder
    val displayedLabourers: StateFlow<List<Labourer>> = combine(
        allLabourers,
        _selectedFolderId
    ) { labourers, folderId ->
        if (folderId == null) labourers else labourers.filter { it.folderId == folderId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Attendance for selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyAttendance: StateFlow<Map<Long, AttendanceRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .combine(allLabourers) { records, _ ->
            records.associateBy { it.labourerId }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun previousDay() {
        _selectedDate.value = DateUtils.shiftDate(_selectedDate.value, -1)
    }

    fun nextDay() {
        _selectedDate.value = DateUtils.shiftDate(_selectedDate.value, 1)
    }

    fun setSelectedFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
    }

    fun setSelectedMonth(monthKey: String) {
        _selectedMonth.value = monthKey
    }

    fun previousMonth() {
        _selectedMonth.value = DateUtils.shiftMonth(_selectedMonth.value, -1)
    }

    fun nextMonth() {
        _selectedMonth.value = DateUtils.shiftMonth(_selectedMonth.value, 1)
    }

    // Attendance Actions
    fun markAttendance(labourerId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = dailyAttendance.value[labourerId]
            val otHours = existing?.overtimeHours ?: 0.0
            val notes = existing?.notes ?: ""
            repository.setAttendance(labourerId, date, status, otHours, notes)
        }
    }

    fun updateOvertime(labourerId: Long, otHours: Double) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = dailyAttendance.value[labourerId]
            val currentStatus = existing?.status ?: AttendanceStatus.PRESENT.name
            repository.setOvertime(labourerId, date, currentStatus, otHours)
        }
    }

    fun markAllPresentForToday() {
        viewModelScope.launch {
            val list = displayedLabourers.value
            repository.bulkMarkAttendance(list, _selectedDate.value, AttendanceStatus.PRESENT)
            _statusMessage.value = "All ${list.size} workers marked Present!"
        }
    }

    // Labourer CRUD
    fun saveLabourer(
        id: Long = 0,
        name: String,
        phone: String,
        dailyRate: Double,
        overtimeRate: Double,
        folderId: Long?,
        tradeRole: String,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val labourer = Labourer(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                dailyRate = dailyRate,
                overtimeHourlyRate = overtimeRate,
                folderId = folderId,
                tradeRole = tradeRole.trim().ifBlank { "Labour" },
                photoUri = photoUri
            )
            repository.saveLabourer(labourer)
            _statusMessage.value = if (id == 0L) "Labourer ${labourer.name} added!" else "Updated ${labourer.name}"
        }
    }

    fun deleteLabourer(labourer: Labourer) {
        viewModelScope.launch {
            repository.deleteLabourer(labourer)
            _statusMessage.value = "${labourer.name} removed"
        }
    }

    // Folder CRUD
    fun saveFolder(
        id: Long = 0,
        name: String,
        description: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val folder = Folder(
                id = id,
                name = name.trim(),
                description = description.trim(),
                colorHex = colorHex
            )
            repository.saveFolder(folder)
            _statusMessage.value = "Folder ${folder.name} saved!"
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
            if (_selectedFolderId.value == folder.id) {
                _selectedFolderId.value = null
            }
            _statusMessage.value = "Folder removed"
        }
    }

    // Payments & Advances
    fun addAdvancePayment(labourerId: Long, amount: Double, notes: String, dateString: String) {
        viewModelScope.launch {
            repository.addPayment(
                PaymentRecord(
                    labourerId = labourerId,
                    amount = amount,
                    type = "ADVANCE",
                    dateString = dateString,
                    notes = notes
                )
            )
            _statusMessage.value = "Advance of ₹$amount recorded!"
        }
    }

    fun deletePayment(paymentId: Long) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
        }
    }

    // Monthly Salary Slip Computation & WhatsApp
    fun prepareSalarySlip(labourer: Labourer, onReady: (MonthlySlip) -> Unit) {
        viewModelScope.launch {
            val folderName = folders.value.find { it.id == labourer.folderId }?.name ?: "General Site"
            val slip = repository.computeMonthlySlip(labourer, folderName, _selectedMonth.value)
            _previewSlip.value = slip
            onReady(slip)
        }
    }

    fun closePreviewSlip() {
        _previewSlip.value = null
    }

    fun sendSlipOnWhatsApp(context: Context, slip: MonthlySlip) {
        val text = WhatsAppHelper.generateSlipMessage(
            slip = slip,
            businessName = _businessName.value,
            adminPhone = _adminPhone.value
        )
        WhatsAppHelper.sendSlipOnWhatsApp(context, slip.labourer.phone, text)
    }

    // Admin & Worker Login / Mode
    fun loginAsAdmin(inputPhone: String, inputPin: String): Boolean {
        val cleanInputPhone = inputPhone.replace(Regex("[^0-9]"), "")
        val cleanAdminPhone = _adminPhone.value.replace(Regex("[^0-9]"), "")
        val phoneMatches = cleanInputPhone.endsWith(cleanAdminPhone.takeLast(10)) ||
                cleanAdminPhone.endsWith(cleanInputPhone.takeLast(10)) ||
                cleanInputPhone == cleanAdminPhone

        if (phoneMatches && inputPin == _adminPin.value) {
            _authMode.value = AuthMode.ADMIN_LOGGED_IN
            _statusMessage.value = "Welcome Admin!"
            return true
        }
        return false
    }

    fun updateAdminProfile(phone: String, pin: String, bName: String) {
        _adminPhone.value = phone.trim()
        _adminPin.value = pin.trim()
        _businessName.value = bName.trim()

        prefs.edit()
            .putString("admin_phone", phone.trim())
            .putString("admin_pin", pin.trim())
            .putString("business_name", bName.trim())
            .apply()
        _statusMessage.value = "Admin settings updated!"
    }

    fun switchToWorkerView(workerPhone: String) {
        viewModelScope.launch {
            _workerSearchPhone.value = workerPhone
            val labourer = repository.findLabourerByPhone(workerPhone)
            if (labourer != null) {
                _activeWorker.value = labourer
                _authMode.value = AuthMode.WORKER_VIEW
                loadWorkerSlip(labourer, _selectedMonth.value)
            } else {
                _statusMessage.value = "No labourer found with mobile: $workerPhone"
            }
        }
    }

    fun loadWorkerSlip(labourer: Labourer, monthKey: String) {
        viewModelScope.launch {
            val folderName = folders.value.find { it.id == labourer.folderId }?.name ?: "General Site"
            val slip = repository.computeMonthlySlip(labourer, folderName, monthKey)
            _workerSlip.value = slip
        }
    }

    fun logoutToRoleSelect() {
        _authMode.value = AuthMode.LOGIN_REQUIRED
        _activeWorker.value = null
        _workerSlip.value = null
    }

    fun switchToAdminDirect() {
        _authMode.value = AuthMode.ADMIN_LOGGED_IN
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
