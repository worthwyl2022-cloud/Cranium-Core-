package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AuditLogEntity
import com.example.data.db.ProjectEntity
import com.example.ui.CraniumUiState
import com.example.ui.CraniumViewModel
import com.example.ui.theme.*

@Composable
fun WorkspacesScreen(
    state: CraniumUiState,
    viewModel: CraniumViewModel,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with Create Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "COGNITIVE WORKSPACES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Persistent Multi-Project Isolation",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_workspace_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Project Workspace Cards
        items(state.projects) { project ->
            val isSelected = project.id == state.currentProject?.id
            WorkspaceProjectCard(
                project = project,
                isSelected = isSelected,
                onSelect = { viewModel.selectProject(project.id) },
                atomCount = if (isSelected) state.atoms.size else 0,
                principleCount = if (isSelected) state.principles.size else 0
            )
        }

        // 3. Substrate Audit Stream
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "GOVERNANCE AUDIT LOG STREAM",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        if (state.auditLogs.isNotEmpty()) {
            items(state.auditLogs) { log ->
                AuditLogItemCard(log = log)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "No audit logs recorded for this workspace yet.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, charter ->
                viewModel.createProject(name, desc, charter)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun WorkspaceProjectCard(
    project: ProjectEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    atomCount: Int,
    principleCount: Int
) {
    val borderColor = if (isSelected) NeonCyan else ObsidianBorder
    val bg = if (isSelected) ObsidianSurfaceElevated else ObsidianSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) NeonCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        project.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NeonCyan else TextPrimary
                    )
                }

                if (isSelected) {
                    Surface(
                        color = NeonCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "ACTIVE",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                project.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Constitution: ${project.constitutionTitle}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = VibrantPurple
            )

            if (isSelected && (atomCount > 0 || principleCount > 0)) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Atoms: $atomCount", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text("Invariants: $principleCount", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text("Status: Synchronized", fontSize = 10.sp, color = EmeraldCanon, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun AuditLogItemCard(log: AuditLogEntity) {
    val eventColor = when (log.eventType) {
        "PROTECT_FIRED" -> RubyQuarantine
        "INJECTION" -> NeonCyan
        "PROMOTION" -> EmeraldCanon
        "QUARANTINE_REJECT" -> AmberAlert
        else -> ElectricIndigo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(eventColor)
                    .padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(log.summary, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(log.eventType, fontSize = 9.sp, color = eventColor, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(log.details, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var constitutionTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Cognitive Workspace", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workspace Name (e.g. Project Orion)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Mission Scope") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = constitutionTitle,
                    onValueChange = { constitutionTitle = it },
                    label = { Text("Constitution / Charter Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(
                            name,
                            description.ifBlank { "Custom cognitive substrate." },
                            constitutionTitle.ifBlank { "$name Charter" }
                        )
                    }
                }
            ) {
                Text("Create Workspace")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
