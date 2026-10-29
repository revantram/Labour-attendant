package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.ui.components.LabourBottomNavigation
import com.example.ui.components.LabourTopAppBar
import com.example.ui.screens.AdminSettingsDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.LabourersScreen
import com.example.ui.screens.ModeSwitcherDialog
import com.example.ui.screens.RegisterWorkerScreen
import com.example.ui.screens.SalarySlipScreen
import com.example.ui.screens.WorkerViewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthMode
import com.example.ui.viewmodel.LabourViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {

    private val viewModel: LabourViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                LabourAttendanceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LabourAttendanceApp(viewModel: LabourViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val authMode by viewModel.authMode.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val adminPhone by viewModel.adminPhone.collectAsStateWithLifecycle()
    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()

    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val allLabourers by viewModel.allLabourers.collectAsStateWithLifecycle()
    val displayedLabourers by viewModel.displayedLabourers.collectAsStateWithLifecycle()
    val dailyAttendance by viewModel.dailyAttendance.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val activeWorker by viewModel.activeWorker.collectAsStateWithLifecycle()
    val workerSlip by viewModel.workerSlip.collectAsStateWithLifecycle()
    val previewSlip by viewModel.previewSlip.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showModeSwitcher by remember { mutableStateOf(false) }
    var showAdminSettings by remember { mutableStateOf(false) }
    var showQuickAddLabourer by remember { mutableStateOf(false) }

    // Handle back button when in Worker View
    BackHandler(enabled = authMode == AuthMode.WORKER_VIEW) {
        showModeSwitcher = true
    }

    // Show Snackbars on status update
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LabourTopAppBar(
                businessName = businessName,
                authMode = authMode,
                adminPhone = adminPhone,
                activeWorkerName = activeWorker?.name,
                onSwitchMode = { showModeSwitcher = true },
                onOpenSettings = { showAdminSettings = true }
            )
        },
        bottomBar = {
            if (authMode == AuthMode.ADMIN_LOGGED_IN) {
                LabourBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { tab -> viewModel.setTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (authMode) {
                AuthMode.WORKER_VIEW -> {
                    val worker = activeWorker
                    if (worker != null) {
                        WorkerViewScreen(
                            worker = worker,
                            slip = workerSlip,
                            selectedMonth = selectedMonth,
                            onPreviousMonth = {
                                viewModel.previousMonth()
                                viewModel.loadWorkerSlip(worker, viewModel.selectedMonth.value)
                            },
                            onNextMonth = {
                                viewModel.nextMonth()
                                viewModel.loadWorkerSlip(worker, viewModel.selectedMonth.value)
                            },
                            onOpenAdminLogin = { showModeSwitcher = true },
                            onSendSlipWhatsApp = { slip ->
                                viewModel.sendSlipOnWhatsApp(context, slip)
                            }
                        )
                    } else {
                        // Fallback if no worker selected
                        showModeSwitcher = true
                    }
                }

                AuthMode.ADMIN_LOGGED_IN, AuthMode.LOGIN_REQUIRED -> {
                    when (currentTab) {
                        ScreenTab.ATTENDANCE -> {
                            AttendanceScreen(
                                currentDate = selectedDate,
                                labourers = displayedLabourers,
                                folders = folders,
                                selectedFolderId = selectedFolderId,
                                dailyAttendance = dailyAttendance,
                                onSelectFolder = { viewModel.setSelectedFolder(it) },
                                onPreviousDay = { viewModel.previousDay() },
                                onNextDay = { viewModel.nextDay() },
                                onToday = { viewModel.setSelectedDate(com.example.util.DateUtils.todayDateString()) },
                                onMarkAttendance = { labourerId, status ->
                                    viewModel.markAttendance(labourerId, status)
                                },
                                onUpdateOvertime = { labourerId, otHours ->
                                    viewModel.updateOvertime(labourerId, otHours)
                                },
                                onMarkAllPresent = { viewModel.markAllPresentForToday() },
                                onOpenAddLabourer = { showQuickAddLabourer = true }
                            )
                        }

                        ScreenTab.LABOURERS -> {
                            LabourersScreen(
                                labourers = allLabourers,
                                folders = folders,
                                onSaveLabourer = { id, name, phone, dailyRate, otRate, folderId, tradeRole, photoUri ->
                                    viewModel.saveLabourer(id, name, phone, dailyRate, otRate, folderId, tradeRole, photoUri)
                                },
                                onDeleteLabourer = { viewModel.deleteLabourer(it) },
                                onAddAdvancePayment = { labourerId, amount, notes, dateStr ->
                                    viewModel.addAdvancePayment(labourerId, amount, notes, dateStr)
                                },
                                onGenerateSlip = { labourer ->
                                    viewModel.prepareSalarySlip(labourer) { /* opened in preview */ }
                                }
                            )
                        }

                        ScreenTab.FOLDERS -> {
                            FoldersScreen(
                                folders = folders,
                                labourers = allLabourers,
                                onSaveFolder = { id, name, desc, colorHex ->
                                    viewModel.saveFolder(id, name, desc, colorHex)
                                },
                                onDeleteFolder = { viewModel.deleteFolder(it) },
                                onSelectFolderToView = { folderId ->
                                    viewModel.setSelectedFolder(folderId)
                                    viewModel.setTab(ScreenTab.ATTENDANCE)
                                }
                            )
                        }

                        ScreenTab.SALARY_SLIPS -> {
                            SalarySlipScreen(
                                monthKey = selectedMonth,
                                labourers = displayedLabourers,
                                folders = folders,
                                selectedFolderId = selectedFolderId,
                                businessName = businessName,
                                adminPhone = adminPhone,
                                previewSlip = previewSlip,
                                onSelectFolder = { viewModel.setSelectedFolder(it) },
                                onPreviousMonth = { viewModel.previousMonth() },
                                onNextMonth = { viewModel.nextMonth() },
                                onComputeSlip = { labourer, callback ->
                                    viewModel.prepareSalarySlip(labourer, callback)
                                },
                                onClosePreviewSlip = { viewModel.closePreviewSlip() },
                                onSendWhatsApp = { slip ->
                                    viewModel.sendSlipOnWhatsApp(context, slip)
                                }
                            )
                        }

                        ScreenTab.WORKER_PORTAL -> {
                            // Can switch to worker lookup
                            showModeSwitcher = true
                        }
                    }
                }
            }
        }
    }

    // Mode Switcher Dialog (Admin PIN login vs Worker Mobile lookup)
    if (showModeSwitcher) {
        ModeSwitcherDialog(
            adminPhone = adminPhone,
            labourers = allLabourers,
            onDismiss = { showModeSwitcher = false },
            onAdminLogin = { pin ->
                val result = viewModel.loginAsAdmin(adminPhone, pin)
                if (result) {
                    showModeSwitcher = false
                }
                result
            },
            onWorkerSelect = { workerPhone ->
                viewModel.switchToWorkerView(workerPhone)
                showModeSwitcher = false
            }
        )
    }

    // Admin Settings Dialog (edit admin phone, PIN, business name)
    if (showAdminSettings) {
        AdminSettingsDialog(
            currentPhone = adminPhone,
            currentPin = adminPin,
            currentBusinessName = businessName,
            onDismiss = { showAdminSettings = false },
            onSave = { newPhone, newPin, newBusinessName ->
                viewModel.updateAdminProfile(newPhone, newPin, newBusinessName)
            }
        )
    }

    // Quick Register Worker Form (from attendance empty state)
    if (showQuickAddLabourer) {
        RegisterWorkerScreen(
            editingLabourer = null,
            folders = folders,
            onDismiss = { showQuickAddLabourer = false },
            onSave = { id, name, phone, dailyRate, otRate, folderId, tradeRole, photoUri ->
                viewModel.saveLabourer(id, name, phone, dailyRate, otRate, folderId, tradeRole, photoUri)
                showQuickAddLabourer = false
            }
        )
    }
}
