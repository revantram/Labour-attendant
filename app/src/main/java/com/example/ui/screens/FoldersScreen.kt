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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.Folder
import com.example.data.model.Labourer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SuccessGreen
import com.example.util.WhatsAppHelper

@Composable
fun FoldersScreen(
    folders: List<Folder>,
    labourers: List<Labourer>,
    onSaveFolder: (id: Long, name: String, description: String, colorHex: String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onSelectFolderToView: (Long) -> Unit
) {
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var editingFolder by remember { mutableStateOf<Folder?>(null) }
    var deleteFolderConfirm by remember { mutableStateOf<Folder?>(null) }

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
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Site & Department Folders",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Group labourers by site, project, or trade contractor",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (folders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No folders created yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { isAddDialogOpen = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create First Folder")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(folders, key = { it.id }) { folder ->
                        val folderWorkers = labourers.filter { it.folderId == folder.id }
                        val dailyTotal = folderWorkers.sumOf { it.dailyRate }

                        FolderCard(
                            folder = folder,
                            workerCount = folderWorkers.size,
                            totalDailyWages = dailyTotal,
                            onEdit = { editingFolder = folder },
                            onDelete = { deleteFolderConfirm = folder },
                            onViewAttendance = { onSelectFolderToView(folder.id) }
                        )
                    }
                }
            }
        }

        // Floating action button
        FloatingActionButton(
            onClick = { isAddDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_folder_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Folder")
        }
    }

    // Add or Edit Dialog
    if (isAddDialogOpen || editingFolder != null) {
        val target = editingFolder
        FolderFormDialog(
            initialFolder = target,
            onDismiss = {
                isAddDialogOpen = false
                editingFolder = null
            },
            onSave = { name, desc, colorHex ->
                onSaveFolder(target?.id ?: 0L, name, desc, colorHex)
                isAddDialogOpen = false
                editingFolder = null
            }
        )
    }

    // Delete confirmation
    deleteFolderConfirm?.let { folder ->
        AlertDialog(
            onDismissRequest = { deleteFolderConfirm = null },
            title = { Text("Delete Folder?") },
            text = { Text("Are you sure you want to delete '${folder.name}'? Labourers will remain unassigned.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFolder(folder)
                        deleteFolderConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteFolderConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FolderCard(
    folder: Folder,
    workerCount: Int,
    totalDailyWages: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewAttendance: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("folder_card_${folder.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (folder.description.isNotBlank()) {
                            Text(
                                text = folder.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$workerCount Labourers",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateSecondary
                    )
                }

                Text(
                    text = "Daily Cost: ${WhatsAppHelper.formatCurrency(totalDailyWages)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onViewAttendance,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth().testTag("view_folder_attendance_${folder.id}")
            ) {
                Text(
                    text = "View Attendance for this Folder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun FolderFormDialog(
    initialFolder: Folder?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(initialFolder?.name ?: "") }
    var description by remember { mutableStateOf(initialFolder?.description ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialFolder == null) "Create New Folder / Site" else "Edit Folder")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder / Site Name *") },
                    placeholder = { Text("e.g. Site A - City Mall") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("folder_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Location / Description (optional)") },
                    placeholder = { Text("e.g. Sector 62 commercial project") },
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
                    if (name.isBlank()) {
                        error = "Please enter folder name"
                        return@Button
                    }
                    onSave(name, description, "#3B82F6")
                },
                modifier = Modifier.testTag("save_folder_btn")
            ) {
                Text("Save Folder")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
