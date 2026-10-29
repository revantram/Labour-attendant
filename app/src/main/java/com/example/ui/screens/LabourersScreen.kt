package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper

@Composable
fun LabourersScreen(
    labourers: List<Labourer>,
    folders: List<Folder>,
    onSaveLabourer: (id: Long, name: String, phone: String, dailyRate: Double, otRate: Double, folderId: Long?, tradeRole: String, photoUri: String?) -> Unit,
    onDeleteLabourer: (Labourer) -> Unit,
    onAddAdvancePayment: (labourerId: Long, amount: Double, notes: String, dateString: String) -> Unit,
    onGenerateSlip: (Labourer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingLabourer by remember { mutableStateOf<Labourer?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var advanceDialogLabourer by remember { mutableStateOf<Labourer?>(null) }
    var deleteConfirmLabourer by remember { mutableStateOf<Labourer?>(null) }

    val filteredList = remember(labourers, searchQuery) {
        if (searchQuery.isBlank()) labourers
        else labourers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery) ||
                    it.tradeRole.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, phone, trade...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_labourers_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Register New Worker Banner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAddDialogOpen = true }
                    .testTag("register_worker_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Register New Worker",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Add name, mobile number & profile photo",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Button(
                        onClick = { isAddDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Add Worker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Workers (${filteredList.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Individual Rates & Photos",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No labourers found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredList, key = { it.id }) { labourer ->
                        val folder = folders.find { it.id == labourer.folderId }

                        LabourerDetailCard(
                            labourer = labourer,
                            folderName = folder?.name ?: "No Folder Assigned",
                            onEdit = { editingLabourer = labourer },
                            onDelete = { deleteConfirmLabourer = labourer },
                            onAddAdvance = { advanceDialogLabourer = labourer },
                            onGenerateSlip = { onGenerateSlip(labourer) }
                        )
                    }
                }
            }
        }

        // FAB to add new Labourer
        FloatingActionButton(
            onClick = { isAddDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_labourer_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Labourer")
        }
    }

    // Register or Edit Worker Form UI
    if (isAddDialogOpen || editingLabourer != null) {
        RegisterWorkerScreen(
            editingLabourer = editingLabourer,
            folders = folders,
            onDismiss = {
                isAddDialogOpen = false
                editingLabourer = null
            },
            onSave = { id, name, phone, dailyRate, otRate, folderId, tradeRole, photoUri ->
                onSaveLabourer(
                    id,
                    name,
                    phone,
                    dailyRate,
                    otRate,
                    folderId,
                    tradeRole,
                    photoUri
                )
                isAddDialogOpen = false
                editingLabourer = null
            }
        )
    }

    // Advance Payment Dialog
    advanceDialogLabourer?.let { labourer ->
        AdvancePaymentDialog(
            labourer = labourer,
            onDismiss = { advanceDialogLabourer = null },
            onConfirm = { amount, notes, dateStr ->
                onAddAdvancePayment(labourer.id, amount, notes, dateStr)
                advanceDialogLabourer = null
            }
        )
    }

    // Delete Confirmation Dialog
    deleteConfirmLabourer?.let { labourer ->
        AlertDialog(
            onDismissRequest = { deleteConfirmLabourer = null },
            title = { Text("Delete Labourer?") },
            text = { Text("Are you sure you want to remove ${labourer.name}? This will remove past attendance records.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteLabourer(labourer)
                        deleteConfirmLabourer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmLabourer = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LabourerDetailCard(
    labourer: Labourer,
    folderName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddAdvance: () -> Unit,
    onGenerateSlip: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("worker_item_${labourer.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Photo
                    if (!labourer.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = labourer.photoUri,
                            contentDescription = "Photo of ${labourer.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labourer.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 16.sp
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
                            Icon(
                                Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = labourer.phone,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${labourer.tradeRole} • $folderName",
                            fontSize = 11.sp,
                            color = SlateSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SlateSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wage Information Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Wage", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = WhatsAppHelper.formatCurrency(labourer.dailyRate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SuccessGreen
                    )
                }

                Column {
                    Text("OT Hourly Rate", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = "${WhatsAppHelper.formatCurrency(labourer.effectiveOtRate())}/h",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BlueTertiary
                    )
                }

                Column {
                    Text("Site/Folder", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = folderName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = SlateSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Add Advance, Slip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddAdvance,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("add_advance_btn_${labourer.id}")
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Advance", fontSize = 12.sp)
                }

                Button(
                    onClick = onGenerateSlip,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("view_slip_btn_${labourer.id}")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wage Slip", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourerFormDialog(
    initialLabourer: Labourer?,
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, dailyRate: Double, otRate: Double, folderId: Long?, tradeRole: String, photoUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(initialLabourer?.name ?: "") }
    var phone by remember { mutableStateOf(initialLabourer?.phone ?: "") }
    var dailyRateStr by remember { mutableStateOf(initialLabourer?.dailyRate?.toInt()?.toString() ?: "500") }
    var otRateStr by remember { mutableStateOf(initialLabourer?.overtimeHourlyRate?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var selectedFolderId by remember { mutableStateOf<Long?>(initialLabourer?.folderId ?: folders.firstOrNull()?.id) }
    var tradeRole by remember { mutableStateOf(initialLabourer?.tradeRole ?: "Labour") }
    var photoUri by remember { mutableStateOf<String?>(initialLabourer?.photoUri) }
    var isFolderDropdownExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Android Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialLabourer == null) "Add New Labourer" else "Edit Labourer Details")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Profile Photo Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Select Photo",
                                    tint = SlateSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text("Add Photo", fontSize = 9.sp, color = SlateSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text("Profile Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (photoUri != null) "Photo selected" else "Tap circle to choose photo",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (photoUri != null) {
                            TextButton(
                                onClick = { photoUri = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Remove Photo", fontSize = 11.sp, color = DangerRed)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_labour_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (WhatsApp) *") },
                    placeholder = { Text("e.g. 9876543210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_labour_phone")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dailyRateStr,
                        onValueChange = { dailyRateStr = it },
                        label = { Text("Daily Rate (₹) *") },
                        placeholder = { Text("600") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_daily_rate")
                    )

                    OutlinedTextField(
                        value = otRateStr,
                        onValueChange = { otRateStr = it },
                        label = { Text("OT Rate (₹/h)") },
                        placeholder = { Text("Auto (Rate/8)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_ot_rate")
                    )
                }

                OutlinedTextField(
                    value = tradeRole,
                    onValueChange = { tradeRole = it },
                    label = { Text("Trade / Role") },
                    placeholder = { Text("Mason, Helper, Carpenter...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Folder Selection Dropdown
                if (folders.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = isFolderDropdownExpanded,
                        onExpandedChange = { isFolderDropdownExpanded = !isFolderDropdownExpanded }
                    ) {
                        val currentFolderName = folders.find { it.id == selectedFolderId }?.name ?: "None"
                        OutlinedTextField(
                            value = currentFolderName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assign Site / Folder") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFolderDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = isFolderDropdownExpanded,
                            onDismissRequest = { isFolderDropdownExpanded = false }
                        ) {
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

                errorMessage?.let {
                    Text(it, color = DangerRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter labourer name"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        errorMessage = "Please enter mobile number"
                        return@Button
                    }
                    val daily = dailyRateStr.toDoubleOrNull()
                    if (daily == null || daily <= 0) {
                        errorMessage = "Please enter valid daily rate"
                        return@Button
                    }
                    val ot = otRateStr.toDoubleOrNull() ?: 0.0

                    onSave(name, phone, daily, ot, selectedFolderId, tradeRole, photoUri)
                },
                modifier = Modifier.testTag("save_labourer_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdvancePaymentDialog(
    labourer: Labourer,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, notes: String, dateString: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val today = remember { DateUtils.todayDateString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Cash Advance")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Advance for ${labourer.name} (${labourer.phone})",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "This will be automatically deducted from their monthly wage slip.",
                    fontSize = 11.sp,
                    color = BlueTertiary
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Advance Amount (₹) *") },
                    placeholder = { Text("1000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("advance_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reason / Note (optional)") },
                    placeholder = { Text("e.g. Festival advance, Medical") },
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
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Please enter valid advance amount"
                        return@Button
                    }
                    onConfirm(amount, notes, today)
                },
                modifier = Modifier.testTag("confirm_advance_btn")
            ) {
                Text("Record Advance")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
