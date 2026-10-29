package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlueTertiary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterWorkerScreen(
    editingLabourer: Labourer? = null,
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, phone: String, dailyRate: Double, otRate: Double, folderId: Long?, tradeRole: String, photoUri: String?) -> Unit
) {
    BackHandler { onDismiss() }

    var name by remember { mutableStateOf(editingLabourer?.name ?: "") }
    var phone by remember { mutableStateOf(editingLabourer?.phone ?: "") }
    var photoUri by remember { mutableStateOf<String?>(editingLabourer?.photoUri) }
    var dailyRateStr by remember { mutableStateOf(editingLabourer?.dailyRate?.toInt()?.toString() ?: "600") }
    var otRateStr by remember {
        mutableStateOf(editingLabourer?.overtimeHourlyRate?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }
    var selectedFolderId by remember { mutableStateOf<Long?>(editingLabourer?.folderId ?: folders.firstOrNull()?.id) }
    var tradeRole by remember { mutableStateOf(editingLabourer?.tradeRole ?: "Mason") }
    var isFolderDropdownExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var rateError by remember { mutableStateOf<String?>(null) }

    // Android zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val popularTrades = listOf(
        "Mason", "Helper", "Carpenter", "Painter",
        "Electrician", "Plumber", "Tile Layer", "Welder", "Supervisor"
    )

    val ratePresets = listOf("450", "500", "600", "700", "800", "1000")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (editingLabourer == null) "Register New Worker" else "Edit Worker Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Fill profile photo, mobile & daily wage rate",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("register_worker_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. PROFILE PHOTO SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WORKER PROFILE PHOTO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Large circular photo preview with interactive camera badge
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .border(3.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("worker_profile_photo_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Worker Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Upload Photo",
                                    tint = SlateSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Add Photo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("select_photo_btn")
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (photoUri == null) "Select Photo" else "Change Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (photoUri != null) {
                            TextButton(
                                onClick = { photoUri = null },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("remove_photo_btn")
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove", color = DangerRed, fontSize = 12.sp)
                            }
                        }
                    }

                    Text(
                        text = "Displayed on attendance ledger, wage slips & worker portal",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 2. NAME & MOBILE NUMBER SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "WORKER IDENTITY & MOBILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    // Worker Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = null
                        },
                        label = { Text("Worker Full Name *") },
                        placeholder = { Text("e.g. Ramesh Kumar Sharma") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                Text(nameError!!, color = DangerRed)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_worker_name_input")
                    )

                    // Worker Mobile Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            if (it.isNotBlank()) phoneError = null
                        },
                        label = { Text("Mobile Number (WhatsApp) *") },
                        placeholder = { Text("e.g. 9876543210") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = null,
                                tint = SuccessGreen
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = phoneError != null,
                        supportingText = {
                            if (phoneError != null) {
                                Text(phoneError!!, color = DangerRed)
                            } else {
                                Text(
                                    "Used for sending monthly WhatsApp slip & worker self-view login",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_worker_phone_input")
                    )
                }
            }

            // 3. WAGE RATES & OVERTIME
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "WAGE RATE & OVERTIME RATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Daily Rate Input
                        OutlinedTextField(
                            value = dailyRateStr,
                            onValueChange = {
                                dailyRateStr = it
                                if (it.isNotBlank()) rateError = null
                            },
                            label = { Text("Daily Rate (₹) *") },
                            placeholder = { Text("600") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = SuccessGreen
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = rateError != null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("register_worker_daily_rate_input")
                        )

                        // Overtime Hourly Rate Input
                        OutlinedTextField(
                            value = otRateStr,
                            onValueChange = { otRateStr = it },
                            label = { Text("OT Rate (₹/h)") },
                            placeholder = {
                                val daily = dailyRateStr.toDoubleOrNull() ?: 600.0
                                Text("₹${(daily / 8.0).toInt()}/h")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            supportingText = {
                                Text("Leave empty for auto (Rate÷8)", fontSize = 10.sp)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("register_worker_ot_rate_input")
                        )
                    }

                    if (rateError != null) {
                        Text(rateError!!, color = DangerRed, fontSize = 12.sp)
                    }

                    // Quick rate preset chips
                    Column {
                        Text(
                            text = "Quick Daily Rate Presets:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ratePresets.forEach { preset ->
                                FilterChip(
                                    selected = dailyRateStr == preset,
                                    onClick = {
                                        dailyRateStr = preset
                                        rateError = null
                                    },
                                    label = { Text("₹$preset", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. TRADE & FOLDER/SITE ASSIGNMENT
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TRADE ROLE & SITE / FOLDER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    // Popular Trade Role Chips
                    Text(
                        text = "Select Trade Role:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        popularTrades.forEach { trade ->
                            FilterChip(
                                selected = tradeRole == trade,
                                onClick = { tradeRole = trade },
                                label = { Text(trade, fontSize = 11.sp) },
                                leadingIcon = if (tradeRole == trade) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    // Custom Trade Role Input
                    OutlinedTextField(
                        value = tradeRole,
                        onValueChange = { tradeRole = it },
                        label = { Text("Trade / Designation") },
                        placeholder = { Text("e.g. Head Mason, Helper, Supervisor") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Site / Folder Assignment Dropdown
                    if (folders.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = isFolderDropdownExpanded,
                            onExpandedChange = { isFolderDropdownExpanded = !isFolderDropdownExpanded }
                        ) {
                            val currentFolderName = folders.find { it.id == selectedFolderId }?.name ?: "General / Unassigned"
                            OutlinedTextField(
                                value = currentFolderName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assign to Site / Folder") },
                                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, tint = BlueTertiary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFolderDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = isFolderDropdownExpanded,
                                onDismissRequest = { isFolderDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("General / Unassigned") },
                                    onClick = {
                                        selectedFolderId = null
                                        isFolderDropdownExpanded = false
                                    }
                                )
                                folders.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(f.name) },
                                        onClick = {
                                            selectedFolderId = f.id
                                            isFolderDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. SUBMIT / CANCEL BUTTONS
            Button(
                onClick = {
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = "Please enter the worker's name"
                        hasError = true
                    }
                    if (phone.isBlank()) {
                        phoneError = "Please enter a valid mobile number"
                        hasError = true
                    } else if (phone.filter { it.isDigit() }.length < 10) {
                        phoneError = "Mobile number must be at least 10 digits"
                        hasError = true
                    }
                    val dailyRate = dailyRateStr.toDoubleOrNull()
                    if (dailyRate == null || dailyRate <= 0) {
                        rateError = "Please enter a valid daily rate"
                        hasError = true
                    }
                    if (hasError) return@Button

                    val otRate = otRateStr.toDoubleOrNull() ?: 0.0

                    onSave(
                        editingLabourer?.id ?: 0L,
                        name.trim(),
                        phone.trim(),
                        dailyRate!!,
                        otRate,
                        selectedFolderId,
                        tradeRole.trim().ifBlank { "Labour" },
                        photoUri
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_worker_submit_btn")
            ) {
                Icon(
                    imageVector = if (editingLabourer == null) Icons.Default.PersonAdd else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingLabourer == null) "Register Worker" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("register_worker_cancel_btn")
            ) {
                Text("Cancel", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
