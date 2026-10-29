package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Labourer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlueTertiary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SuccessGreen

@Composable
fun ModeSwitcherDialog(
    adminPhone: String,
    labourers: List<Labourer>,
    onDismiss: () -> Unit,
    onAdminLogin: (pin: String) -> Boolean,
    onWorkerSelect: (phone: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Admin Mode, 1 = Worker View
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var workerPhoneInput by remember { mutableStateOf("") }
    var workerPhoneError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch Account Mode",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Admin Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Worker Portal", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Admin Login Tab
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Registered Admin Mobile:", fontSize = 11.sp, color = Color(0xFF92400E))
                                Text(
                                    text = adminPhone,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = AmberPrimary
                                )
                                Text(
                                    text = "Only the admin mobile number can open and manage the app.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 4) {
                                    pinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("Enter 4-digit Admin PIN") },
                            placeholder = { Text("Default: 1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                        )

                        pinError?.let {
                            Text(it, color = DangerRed, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val success = onAdminLogin(pinInput)
                                if (success) {
                                    onDismiss()
                                } else {
                                    pinError = "Incorrect PIN! (Default is 1234)"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_submit_btn")
                        ) {
                            Text("Unlock Admin Mode")
                        }
                    }
                } else {
                    // Worker View Tab
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Enter worker's mobile number to see their personal attendance, earnings & balance:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = workerPhoneInput,
                            onValueChange = {
                                workerPhoneInput = it
                                workerPhoneError = null
                            },
                            label = { Text("Worker Mobile Number") },
                            placeholder = { Text("e.g. 9876543210") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("worker_phone_input")
                        )

                        workerPhoneError?.let {
                            Text(it, color = DangerRed, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (workerPhoneInput.isBlank()) {
                                    workerPhoneError = "Please enter mobile number"
                                    return@Button
                                }
                                onWorkerSelect(workerPhoneInput)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueTertiary),
                            modifier = Modifier.fillMaxWidth().testTag("worker_lookup_btn")
                        ) {
                            Text("Open Worker Passbook")
                        }

                        if (labourers.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "Or select worker directly:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(labourers) { labourer ->
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onWorkerSelect(labourer.phone)
                                                onDismiss()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(labourer.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(labourer.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                            Text("View >", fontSize = 11.sp, color = BlueTertiary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AdminSettingsDialog(
    currentPhone: String,
    currentPin: String,
    currentBusinessName: String,
    onDismiss: () -> Unit,
    onSave: (phone: String, pin: String, businessName: String) -> Unit
) {
    var phone by remember { mutableStateOf(currentPhone) }
    var pin by remember { mutableStateOf(currentPin) }
    var bName by remember { mutableStateOf(currentBusinessName) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Admin & Business Settings")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure the single Admin mobile number that controls the application:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Admin Mobile Number *") },
                    placeholder = { Text("e.g. 9988776655") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_settings_phone_input")
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text("Admin 4-digit PIN *") },
                    placeholder = { Text("1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_settings_pin_input")
                )

                OutlinedTextField(
                    value = bName,
                    onValueChange = { bName = it },
                    label = { Text("Business / Contractor Name") },
                    placeholder = { Text("e.g. Rajesh Construction") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Text(it, color = DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isBlank()) {
                        error = "Please enter admin mobile number"
                        return@Button
                    }
                    if (pin.length < 4) {
                        error = "PIN must be 4 digits"
                        return@Button
                    }
                    onSave(phone, pin, bName)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_admin_settings_btn")
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
