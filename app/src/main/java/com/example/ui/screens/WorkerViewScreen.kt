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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AttendanceStatus
import com.example.data.model.Labourer
import com.example.data.model.MonthlySlip
import com.example.ui.components.MonthNavigatorBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlueTertiary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import java.util.Locale

@Composable
fun WorkerViewScreen(
    worker: Labourer,
    slip: MonthlySlip?,
    selectedMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenAdminLogin: () -> Unit,
    onSendSlipWhatsApp: (MonthlySlip) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Worker Header Card with Profile Photo & Details
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Photo
                    if (!worker.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = worker.photoUri,
                            contentDescription = "Profile Photo of ${worker.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = worker.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = worker.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "${worker.tradeRole} • Rate: ${WhatsAppHelper.formatCurrency(worker.dailyRate)}/day",
                            fontSize = 11.sp,
                            color = SlateSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Switch to Admin Button
                IconButton(
                    onClick = onOpenAdminLogin,
                    modifier = Modifier.testTag("worker_screen_admin_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Login",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Month Selector
        MonthNavigatorBar(
            monthKey = selectedMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )

        // SEPARATE DASHBOARD ABOVE (Money Earned, Money Taken, Balance Left, Attendance & Absent)
        if (slip != null) {
            WorkerDashboardCard(slip = slip)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // WhatsApp Slip Quick Action
        if (slip != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Attendance Log",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = { onSendSlipWhatsApp(slip) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("worker_send_slip_whatsapp")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("My Slip on WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Day-by-Day Attendance List
        val attendanceList = slip?.dailyAttendanceList ?: emptyList()

        if (attendanceList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No attendance marked for this month yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                items(attendanceList, key = { it.id }) { record ->
                    val status = AttendanceStatus.fromString(record.status)
                    val dayOfWeek = DateUtils.getDayOfWeek(record.dateString)

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = DateUtils.formatDisplayDate(record.dateString),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (record.overtimeHours > 0) {
                                    Text(
                                        text = "Overtime: ${String.format(Locale.getDefault(), "%.1f", record.overtimeHours)} hrs (+${WhatsAppHelper.formatCurrency(record.overtimeHours * worker.effectiveOtRate())})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueTertiary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = status)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerDashboardCard(slip: MonthlySlip) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("worker_dashboard_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY EARNINGS & ATTENDANCE SUMMARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${slip.monthName} ${slip.year}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Money Earned, Money Taken, Balance Left Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // How much money earned this month
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Earned This Month", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                        Text(
                            text = WhatsAppHelper.formatCurrency(slip.grossEarnings),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            maxLines = 1
                        )
                    }
                }

                // How much money taken (Advance)
                Surface(
                    color = Color(0xFFFFF1F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Money Taken", fontSize = 10.sp, color = Color(0xFF9F1239), fontWeight = FontWeight.Medium)
                        Text(
                            text = WhatsAppHelper.formatCurrency(slip.totalAdvanceDeductions),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed,
                            maxLines = 1
                        )
                    }
                }

                // How much money was left (Balance)
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Balance Left", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = WhatsAppHelper.formatCurrency(slip.netPayable),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))

            // How much attendance marked and how many absent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${slip.fullDays}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text("Present (P)", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }

                // Half Day
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${slip.halfDays}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange
                    )
                    Text("Half Day (H)", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }

                // Absent
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${slip.absentDays}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                    Text("Absent (A)", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }

                // Overtime Hours
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", slip.totalOtHours)}h",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueTertiary
                    )
                    Text("Overtime", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
