package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.db.QuarantineRecordEntity
import com.example.ui.CraniumUiState
import com.example.ui.CraniumViewModel
import com.example.ui.theme.*

@Composable
fun QuarantineScreen(
    state: CraniumUiState,
    viewModel: CraniumViewModel,
    modifier: Modifier = Modifier
) {
    val pendingRecords = state.quarantineRecords.filter { it.status == "PENDING" }
    val resolvedRecords = state.quarantineRecords.filter { it.status != "PENDING" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Governance Gate Header & Protocol Card
        item {
            GovernanceProtocolCard(pendingCount = pendingRecords.size)
        }

        // 2. Pending Proposals
        item {
            Text(
                "AWAITING OPERATOR VERIFICATION (${pendingRecords.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        if (pendingRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldCanon, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Quarantine Inbox Clean", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("No pending synthetic continuations requiring review.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(pendingRecords) { record ->
                QuarantineRecordCard(
                    record = record,
                    onApprove = { viewModel.approveQuarantineRecord(record) },
                    onReject = { viewModel.rejectQuarantineRecord(record) }
                )
            }
        }

        // 3. Resolved / Historic Log
        if (resolvedRecords.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "RESOLVED QUARANTINE LOGS (${resolvedRecords.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            items(resolvedRecords) { record ->
                ResolvedQuarantineCard(record = record)
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun GovernanceProtocolCard(pendingCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RubyQuarantine.copy(alpha = 0.5f)))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RubyQuarantine.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = RubyQuarantine, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CRANIUM QUARANTINE PROTOCOL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RubyQuarantine,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "All synthetic model continuations are held in cold isolation. Zero unverified proposals fuse into field dynamics without explicit gate approval.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun QuarantineRecordCard(
    record: QuarantineRecordEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (record.violationDetected) RubyQuarantine else NeonCyan.copy(alpha = 0.5f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.violationDetected) {
                        Surface(
                            color = RubyQuarantine.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "🔴 CONTRADICTION ALERT",
                                color = RubyQuarantine,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = NeonCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "PROVISIONAL CONTINUATION",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    "Cycle #${record.cycleNumber}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                record.proposalText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            if (record.violationReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ObsidianDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        record.violationReason,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 11.sp,
                        color = if (record.violationDetected) RubyQuarantine else EmeraldCanon,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyQuarantine),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("reject_quarantine_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject & Purge", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldCanon),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("approve_quarantine_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve & Fuse into Field", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ResolvedQuarantineCard(record: QuarantineRecordEntity) {
    val isApproved = record.status == "APPROVED"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isApproved) EmeraldCanon else RubyQuarantine,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    record.proposalText,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
                Text(
                    "Status: ${record.status} • Directive: ${record.directive}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
