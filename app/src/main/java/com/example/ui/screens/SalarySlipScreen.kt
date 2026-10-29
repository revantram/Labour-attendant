package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.data.model.MonthlySlip
import com.example.ui.components.MonthNavigatorBar
import com.example.ui.components.WhatsAppSlipDialog
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlueTertiary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.util.WhatsAppHelper

@Composable
fun SalarySlipScreen(
    monthKey: String,
    labourers: List<Labourer>,
    folders: List<Folder>,
    selectedFolderId: Long?,
    businessName: String,
    adminPhone: String,
    previewSlip: MonthlySlip?,
    onSelectFolder: (Long?) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onComputeSlip: (Labourer, (MonthlySlip) -> Unit) -> Unit,
    onClosePreviewSlip: () -> Unit,
    onSendWhatsApp: (MonthlySlip) -> Unit
) {
    val context = LocalContext.current
    val calculatedSlips = remember(monthKey, labourers) { mutableStateMapOf<Long, MonthlySlip>() }

    // Pre-calculate slips for all labourers in the current view
    LaunchedEffect(monthKey, labourers) {
        labourers.forEach { labourer ->
            onComputeSlip(labourer) { slip ->
                calculatedSlips[labourer.id] = slip
            }
        }
    }

    // Aggregate Dashboard Metrics
    val totalEarned = calculatedSlips.values.sumOf { it.grossEarnings }
    val totalTaken = calculatedSlips.values.sumOf { it.totalAdvanceDeductions }
    val totalBalanceLeft = calculatedSlips.values.sumOf { it.netPayable }

    val totalFullDays = calculatedSlips.values.sumOf { it.fullDays }
    val totalHalfDays = calculatedSlips.values.sumOf { it.halfDays }
    val totalAbsentDays = calculatedSlips.values.sumOf { it.absentDays }
    val totalOtHours = calculatedSlips.values.sumOf { it.totalOtHours }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Month Selector
        MonthNavigatorBar(
            monthKey = monthKey,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )

        // Folder Filter
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
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
            items(folders) { folder ->
                FilterChip(
                    selected = selectedFolderId == folder.id,
                    onClick = { onSelectFolder(folder.id) },
                    label = { Text(folder.name, fontSize = 12.sp) }
                )
            }
        }

        // SEPARATE TOP DASHBOARD: Money Earned, Taken, Balance Left, Attendance & Absent
        SalaryTopDashboardCard(
            totalEarned = totalEarned,
            totalTaken = totalTaken,
            totalBalanceLeft = totalBalanceLeft,
            totalFullDays = totalFullDays,
            totalHalfDays = totalHalfDays,
            totalAbsentDays = totalAbsentDays,
            totalOtHours = totalOtHours
        )

        // Labourer Slip Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Worker Slips & WhatsApp",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Send on WhatsApp linked to mobile",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(labourers, key = { it.id }) { labourer ->
                val slip = calculatedSlips[labourer.id]
                val folder = folders.find { it.id == labourer.folderId }

                WorkerMonthlySlipCard(
                    labourer = labourer,
                    folderName = folder?.name ?: "General",
                    slip = slip,
                    onViewSlip = {
                        onComputeSlip(labourer) { _ -> }
                    },
                    onSendWhatsApp = {
                        if (slip != null) {
                            onSendWhatsApp(slip)
                        } else {
                            onComputeSlip(labourer) { fresh ->
                                onSendWhatsApp(fresh)
                            }
                        }
                    }
                )
            }
        }
    }

    // WhatsApp Slip Preview Modal
    previewSlip?.let { slip ->
        WhatsAppSlipDialog(
            slip = slip,
            businessName = businessName,
            adminPhone = adminPhone,
            onDismiss = onClosePreviewSlip,
            onSendWhatsApp = {
                onSendWhatsApp(slip)
                onClosePreviewSlip()
            }
        )
    }
}

@Composable
fun SalaryTopDashboardCard(
    totalEarned: Double,
    totalTaken: Double,
    totalBalanceLeft: Double,
    totalFullDays: Int,
    totalHalfDays: Int,
    totalAbsentDays: Int,
    totalOtHours: Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("salary_top_dashboard")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "MONTHLY WAGE & ATTENDANCE DASHBOARD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Money Earned, Money Taken, Balance Left Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Earned
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Earned", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                        Text(
                            text = WhatsAppHelper.formatCurrency(totalEarned),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            maxLines = 1
                        )
                    }
                }

                // Money Taken (Advance)
                Surface(
                    color = Color(0xFFFFF1F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Taken (Adv)", fontSize = 10.sp, color = Color(0xFF9F1239), fontWeight = FontWeight.Medium)
                        Text(
                            text = WhatsAppHelper.formatCurrency(totalTaken),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed,
                            maxLines = 1
                        )
                    }
                }

                // Balance Left
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Balance Left", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = WhatsAppHelper.formatCurrency(totalBalanceLeft),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(6.dp))

            // Attendance Breakdown Row: Full Days, Half Days, Absent, OT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Present: ", fontSize = 11.sp, color = Color(0xFF475569))
                    Text("$totalFullDays", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WarningOrange))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Half Day: ", fontSize = 11.sp, color = Color(0xFF475569))
                    Text("$totalHalfDays", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DangerRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Absent: ", fontSize = 11.sp, color = Color(0xFF475569))
                    Text("$totalAbsentDays", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BlueTertiary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OT: ", fontSize = 11.sp, color = Color(0xFF475569))
                    Text("${String.format(java.util.Locale.getDefault(), "%.1f", totalOtHours)}h", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WorkerMonthlySlipCard(
    labourer: Labourer,
    folderName: String,
    slip: MonthlySlip?,
    onViewSlip: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("slip_card_${labourer.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Worker Photo & Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Photo / Avatar
                    if (!labourer.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = labourer.photoUri,
                            contentDescription = "Profile Photo of ${labourer.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = labourer.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "• $folderName", fontSize = 11.sp, color = SlateSecondary)
                        }
                    }
                }

                // Balance remaining badge
                if (slip != null) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Balance Left", fontSize = 9.sp, color = Color(0xFF92400E))
                            Text(
                                text = WhatsAppHelper.formatCurrency(slip.netPayable),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = AmberPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wage calculation breakdown
            if (slip != null) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Earned: ${WhatsAppHelper.formatCurrency(slip.grossEarnings)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                            Text("Taken: -${WhatsAppHelper.formatCurrency(slip.totalAdvanceDeductions)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DangerRed)
                            Text("Net: ${WhatsAppHelper.formatCurrency(slip.netPayable)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("P: ${slip.fullDays}d • H: ${slip.halfDays}d • A: ${slip.absentDays}d • OT: ${String.format(java.util.Locale.getDefault(), "%.1f", slip.totalOtHours)}h", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("@ ${WhatsAppHelper.formatCurrency(labourer.dailyRate)}/d", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: View Details & Send WhatsApp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewSlip,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("preview_slip_${labourer.id}")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Slip", fontSize = 12.sp)
                }

                Button(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.3f).testTag("whatsapp_btn_${labourer.id}")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send on WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
