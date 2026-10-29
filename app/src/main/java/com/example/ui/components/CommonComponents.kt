package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.MonthlySlip
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlueTertiary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedContainer
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeContainer
import com.example.ui.viewmodel.AuthMode
import com.example.ui.viewmodel.ScreenTab
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourTopAppBar(
    businessName: String,
    authMode: AuthMode,
    adminPhone: String,
    activeWorkerName: String?,
    onSwitchMode: () -> Unit,
    onOpenSettings: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Column {
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isWorker = authMode == AuthMode.WORKER_VIEW
                    val badgeColor = if (isWorker) BlueTertiary else AmberPrimary
                    val label = if (isWorker) "Worker View: ${activeWorkerName ?: "Passbook"}" else "Admin: $adminPhone"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = label,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onSwitchMode,
                modifier = Modifier.testTag("switch_mode_button")
            ) {
                Icon(
                    imageVector = if (authMode == AuthMode.ADMIN_LOGGED_IN) Icons.Default.Person else Icons.Default.AdminPanelSettings,
                    contentDescription = "Switch Mode",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (authMode == AuthMode.ADMIN_LOGGED_IN) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Admin Settings"
                    )
                }
            }
        }
    )
}

@Composable
fun LabourBottomNavigation(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentTab == ScreenTab.ATTENDANCE,
            onClick = { onTabSelected(ScreenTab.ATTENDANCE) },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Attendance") },
            label = { Text("Attendance", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_attendance")
        )
        NavigationBarItem(
            selected = currentTab == ScreenTab.LABOURERS,
            onClick = { onTabSelected(ScreenTab.LABOURERS) },
            icon = { Icon(Icons.Default.People, contentDescription = "Labourers") },
            label = { Text("Labourers", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_labourers")
        )
        NavigationBarItem(
            selected = currentTab == ScreenTab.FOLDERS,
            onClick = { onTabSelected(ScreenTab.FOLDERS) },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Folders/Sites") },
            label = { Text("Folders", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_folders")
        )
        NavigationBarItem(
            selected = currentTab == ScreenTab.SALARY_SLIPS,
            onClick = { onTabSelected(ScreenTab.SALARY_SLIPS) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Salary Slips") },
            label = { Text("Slips & Pay", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_slips")
        )
    }
}

@Composable
fun DateNavigatorBar(
    currentDate: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(36.dp).testTag("date_prev_button")
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = DateUtils.formatDisplayDate(currentDate),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                val isToday = currentDate == DateUtils.todayDateString()
                if (!isToday) {
                    Text(
                        text = "Jump to Today",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onToday() }
                    )
                }
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp).testTag("date_next_button")
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
            }
        }
    }
}

@Composable
fun MonthNavigatorBar(
    monthKey: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(36.dp).testTag("month_prev_button")
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = DateUtils.formatMonthDisplay(monthKey),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(36.dp).testTag("month_next_button")
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
    }
}

@Composable
fun AttendanceKPIBanner(
    totalLabourers: Int,
    presentCount: Int,
    halfDayCount: Int,
    absentCount: Int,
    totalOtHours: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBadge(
            label = "Present",
            count = "$presentCount",
            containerColor = SuccessGreenContainer,
            contentColor = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        StatBadge(
            label = "Half Day",
            count = "$halfDayCount",
            containerColor = WarningOrangeContainer,
            contentColor = WarningOrange,
            modifier = Modifier.weight(1f)
        )
        StatBadge(
            label = "Absent",
            count = "$absentCount",
            containerColor = DangerRedContainer,
            contentColor = DangerRed,
            modifier = Modifier.weight(1f)
        )
        StatBadge(
            label = "Overtime",
            count = "${String.format(java.util.Locale.getDefault(), "%.1f", totalOtHours)}h",
            containerColor = Color(0xFFDBEAFE),
            contentColor = BlueTertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatBadge(
    label: String,
    count: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = contentColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusBadge(status: AttendanceStatus) {
    val (bgColor, textColor, label) = when (status) {
        AttendanceStatus.PRESENT -> Triple(SuccessGreenContainer, SuccessGreen, "Full Day (P)")
        AttendanceStatus.HALF_DAY -> Triple(WarningOrangeContainer, WarningOrange, "Half Day (H)")
        AttendanceStatus.ABSENT -> Triple(DangerRedContainer, DangerRed, "Absent (A)")
        AttendanceStatus.PAID_LEAVE -> Triple(Color(0xFFE0E7FF), Color(0xFF4338CA), "Paid Leave")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WhatsAppSlipDialog(
    slip: MonthlySlip,
    businessName: String,
    adminPhone: String,
    onDismiss: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    val context = LocalContext.current
    val messageText = remember(slip, businessName, adminPhone) {
        WhatsAppHelper.generateSlipMessage(slip, businessName, adminPhone)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Salary Slip • ${slip.monthName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = slip.labourer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SlateSecondary
                        )
                        Text(
                            text = "Mobile: ${slip.labourer.phone} • ${slip.folderName}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Full Days (${slip.fullDays}d @ ${WhatsAppHelper.formatCurrency(slip.labourer.dailyRate)}):", fontSize = 12.sp)
                            Text(WhatsAppHelper.formatCurrency(slip.fullDays * slip.labourer.dailyRate), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        if (slip.halfDays > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Half Days (${slip.halfDays}d @ 50%):", fontSize = 12.sp)
                                Text(WhatsAppHelper.formatCurrency(slip.halfDays * (slip.labourer.dailyRate / 2.0)), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        if (slip.totalOtHours > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Overtime (${String.format(java.util.Locale.getDefault(), "%.1f", slip.totalOtHours)}h @ ${WhatsAppHelper.formatCurrency(slip.labourer.effectiveOtRate())}):", fontSize = 12.sp)
                                Text(WhatsAppHelper.formatCurrency(slip.overtimePay), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BlueTertiary)
                            }
                        }

                        if (slip.totalAdvanceDeductions > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Advance Deductions:", fontSize = 12.sp, color = DangerRed)
                                Text("-${WhatsAppHelper.formatCurrency(slip.totalAdvanceDeductions)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DangerRed)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Payable:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                WhatsAppHelper.formatCurrency(slip.netPayable),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SuccessGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "WhatsApp message will be sent to ${slip.labourer.phone} with full breakdown.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSendWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier.testTag("send_whatsapp_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send on WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
