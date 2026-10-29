package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.ui.components.AttendanceKPIBanner
import com.example.ui.components.DateNavigatorBar
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlueTertiary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedContainer
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeContainer
import com.example.util.WhatsAppHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    currentDate: String,
    labourers: List<Labourer>,
    folders: List<Folder>,
    selectedFolderId: Long?,
    dailyAttendance: Map<Long, AttendanceRecord>,
    onSelectFolder: (Long?) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onMarkAttendance: (labourerId: Long, status: AttendanceStatus) -> Unit,
    onUpdateOvertime: (labourerId: Long, otHours: Double) -> Unit,
    onMarkAllPresent: () -> Unit,
    onOpenAddLabourer: () -> Unit
) {
    var overtimeDialogLabourer by remember { mutableStateOf<Labourer?>(null) }

    // Aggregate stats for current view
    val presentCount = labourers.count {
        dailyAttendance[it.id]?.status == AttendanceStatus.PRESENT.name
    }
    val halfDayCount = labourers.count {
        dailyAttendance[it.id]?.status == AttendanceStatus.HALF_DAY.name
    }
    val absentCount = labourers.count {
        dailyAttendance[it.id]?.status == AttendanceStatus.ABSENT.name
    }
    val totalOtHours = labourers.sumOf {
        dailyAttendance[it.id]?.overtimeHours ?: 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Date Navigator
        DateNavigatorBar(
            currentDate = currentDate,
            onPrevious = onPreviousDay,
            onNext = onNextDay,
            onToday = onToday
        )

        // Folder / Site Filter Row ("folder" requested by user)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedFolderId == null,
                    onClick = { onSelectFolder(null) },
                    label = { Text("All Sites (${labourers.size})", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_all_folders")
                )
            }
            items(folders) { folder ->
                FilterChip(
                    selected = selectedFolderId == folder.id,
                    onClick = { onSelectFolder(folder.id) },
                    label = { Text(folder.name, fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_folder_${folder.id}")
                )
            }
        }

        // Summary KPI Banner
        AttendanceKPIBanner(
            totalLabourers = labourers.size,
            presentCount = presentCount,
            halfDayCount = halfDayCount,
            absentCount = absentCount,
            totalOtHours = totalOtHours
        )

        // Bulk action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${labourers.size} Labourers",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onMarkAllPresent,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("mark_all_present_button")
            ) {
                Icon(
                    Icons.Default.DoneAll,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Mark All Present",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Labourer List
        if (labourers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No labourers found in this folder",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenAddLabourer) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Labourer")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(labourers, key = { it.id }) { labourer ->
                    val record = dailyAttendance[labourer.id]
                    val currentStatus = record?.status?.let { AttendanceStatus.fromString(it) }
                    val currentOt = record?.overtimeHours ?: 0.0
                    val folderName = folders.find { it.id == labourer.folderId }?.name ?: "General"

                    LabourerAttendanceCard(
                        labourer = labourer,
                        folderName = folderName,
                        currentStatus = currentStatus,
                        currentOtHours = currentOt,
                        onMarkStatus = { status -> onMarkAttendance(labourer.id, status) },
                        onOpenOtPicker = { overtimeDialogLabourer = labourer },
                        onQuickIncrementOt = { increment ->
                            val newOt = (currentOt + increment).coerceAtLeast(0.0)
                            onUpdateOvertime(labourer.id, newOt)
                        }
                    )
                }
            }
        }
    }

    // Overtime Dialog
    overtimeDialogLabourer?.let { labourer ->
        val record = dailyAttendance[labourer.id]
        var inputOt by remember { mutableDoubleStateOf(record?.overtimeHours ?: 0.0) }

        AlertDialog(
            onDismissRequest = { overtimeDialogLabourer = null },
            title = {
                Text(
                    text = "Overtime (OT) for ${labourer.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "OT Hourly Rate: ${WhatsAppHelper.formatCurrency(labourer.effectiveOtRate())} / hr",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (inputOt >= 0.5) inputOt -= 0.5 },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease OT")
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", inputOt)} hrs",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueTertiary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        IconButton(
                            onClick = { inputOt += 0.5 },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase OT")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Estimated OT Pay: ${WhatsAppHelper.formatCurrency(inputOt * labourer.effectiveOtRate())}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(1.0, 2.0, 3.0, 4.0).forEach { preset ->
                            OutlinedButton(
                                onClick = { inputOt = preset },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("${preset.toInt()}h", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateOvertime(labourer.id, inputOt)
                        overtimeDialogLabourer = null
                    }
                ) {
                    Text("Save OT")
                }
            },
            dismissButton = {
                TextButton(onClick = { overtimeDialogLabourer = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LabourerAttendanceCard(
    labourer: Labourer,
    folderName: String,
    currentStatus: AttendanceStatus?,
    currentOtHours: Double,
    onMarkStatus: (AttendanceStatus) -> Unit,
    onOpenOtPicker: () -> Unit,
    onQuickIncrementOt: (Double) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("labourer_card_${labourer.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Photo, Name, Phone, Rates, Folder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!labourer.photoUri.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = labourer.photoUri,
                            contentDescription = "Photo of ${labourer.name}",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labourer.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = labourer.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = labourer.phone,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${labourer.tradeRole} ($folderName)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Daily Wage rate pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${WhatsAppHelper.formatCurrency(labourer.dailyRate)}/day",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "OT: ${WhatsAppHelper.formatCurrency(labourer.effectiveOtRate())}/h",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance Buttons: P, H, A & Overtime
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present Button (P)
                AttendanceActionButton(
                    label = "Present (P)",
                    shortLabel = "Full Day",
                    isSelected = currentStatus == AttendanceStatus.PRESENT,
                    activeContainer = SuccessGreen,
                    activeContent = Color.White,
                    inactiveContainer = SuccessGreenContainer.copy(alpha = 0.4f),
                    inactiveContent = SuccessGreen,
                    onClick = { onMarkStatus(AttendanceStatus.PRESENT) },
                    modifier = Modifier.weight(1f).testTag("mark_present_${labourer.id}")
                )

                // Half Day Button (H) - Explicitly requested!
                AttendanceActionButton(
                    label = "Half Day (H)",
                    shortLabel = "Half Day",
                    isSelected = currentStatus == AttendanceStatus.HALF_DAY,
                    activeContainer = WarningOrange,
                    activeContent = Color.White,
                    inactiveContainer = WarningOrangeContainer.copy(alpha = 0.4f),
                    inactiveContent = WarningOrange,
                    onClick = { onMarkStatus(AttendanceStatus.HALF_DAY) },
                    modifier = Modifier.weight(1f).testTag("mark_half_day_${labourer.id}")
                )

                // Absent Button (A)
                AttendanceActionButton(
                    label = "Absent (A)",
                    shortLabel = "Absent",
                    isSelected = currentStatus == AttendanceStatus.ABSENT,
                    activeContainer = DangerRed,
                    activeContent = Color.White,
                    inactiveContainer = DangerRedContainer.copy(alpha = 0.4f),
                    inactiveContent = DangerRed,
                    onClick = { onMarkStatus(AttendanceStatus.ABSENT) },
                    modifier = Modifier.weight(1f).testTag("mark_absent_${labourer.id}")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overtime Control Row - Explicitly requested!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BlueTertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Overtime (OT):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (currentOtHours > 0) Color(0xFFDBEAFE) else Color.White,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (currentOtHours > 0) BlueTertiary else Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = if (currentOtHours > 0) "${String.format(Locale.getDefault(), "%.1f", currentOtHours)} hrs (+${WhatsAppHelper.formatCurrency(currentOtHours * labourer.effectiveOtRate())})" else "None (0h)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentOtHours > 0) BlueTertiary else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick +1h button
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { onQuickIncrementOt(1.0) }
                    ) {
                        Text(
                            text = "+1h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueTertiary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Edit button
                    TextButton(
                        onClick = onOpenOtPicker,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Edit OT", fontSize = 11.sp, color = BlueTertiary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceActionButton(
    label: String,
    shortLabel: String,
    isSelected: Boolean,
    activeContainer: Color,
    activeContent: Color,
    inactiveContainer: Color,
    inactiveContent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) activeContainer else inactiveContainer,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, inactiveContent.copy(alpha = 0.3f)),
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = activeContent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = shortLabel,
                color = if (isSelected) activeContent else inactiveContent,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
