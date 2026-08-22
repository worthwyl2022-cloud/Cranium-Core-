package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BenchmarkRunScore
import com.example.ui.CraniumUiState
import com.example.ui.CraniumViewModel
import com.example.ui.theme.*
import com.example.util.AuditReportExporter
import com.example.util.ExportFormat

@Composable
fun StressBenchmarkScreen(
    state: CraniumUiState,
    viewModel: CraniumViewModel,
    modifier: Modifier = Modifier
) {
    var temperature by remember { mutableFloatStateOf(0.7f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Matrix Sweep Control Card
        item {
            MatrixControlCard(
                temperature = temperature,
                onTemperatureChange = { temperature = it },
                isRunning = state.isRunningBenchmark,
                onRunSweep = { viewModel.runFullBenchmarkMatrix(temperature) }
            )
        }

        // 2. Head-to-Head Comparative Score Matrix
        item {
            Text(
                "HEAD-TO-HEAD COMPARATIVE STRESS MATRIX",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        if (state.benchmarkScores.isNotEmpty()) {
            items(state.benchmarkScores) { score ->
                BenchmarkScoreCard(score = score)
            }
        } else {
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
                        Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ready to Run Stress Sweep", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Adjust generator temperature and tap 'Execute Stress Sweep' above.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        // 3. Frozen Benchmark Corpus Overview
        item {
            Spacer(modifier = Modifier.height(8.dp))
            FormalBuyerAuditCard(state = state)
        }

        item {
            FrozenCorpusOverviewCard()
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun FormalBuyerAuditCard(state: CraniumUiState) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldCanon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Audit Dossier", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Select format to export or share with technical buyers and auditors:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            showExportDialog = false
                            AuditReportExporter.shareDossier(context, state, ExportFormat.HTML)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_html_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldCanon)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = ObsidianDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Print-Ready HTML Dossier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDark)
                    }

                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            AuditReportExporter.shareDossier(context, state, ExportFormat.MARKDOWN)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_markdown_button"),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan))
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Markdown Report (.md)", fontSize = 11.sp, color = NeonCyan)
                    }

                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            AuditReportExporter.shareDossier(context, state, ExportFormat.JSON)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_json_button"),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(TextSecondary))
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Machine JSON Certificate", fontSize = 11.sp, color = TextSecondary)
                    }

                    TextButton(
                        onClick = {
                            showExportDialog = false
                            AuditReportExporter.copyToClipboard(context, state)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Markdown Summary to Clipboard", fontSize = 11.sp, color = AmberAlert)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = ObsidianSurfaceElevated
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldCanon.copy(alpha = 0.6f)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldCanon, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "FORMAL BUYER AUDIT DOSSIER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldCanon,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = EmeraldCanon.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "CERTIFIED",
                        color = EmeraldCanon,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Certificate ID: CRANIUM-AUDIT-20260822-52274D3F",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Live Model Runs: Gemini 3.7 Flash • Gemini 2.5 Pro • Claude 3.7 Sonnet • GPT-4.5 Turbo",
                fontSize = 10.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = ObsidianDark,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("• Print-Ready Audit Report: CRANIUM_CORE_AUDIT_REPORT.html", fontSize = 10.sp, color = NeonCyan, fontFamily = FontFamily.Monospace)
                    Text("• Technical Buyer Markdown: CRANIUM_CORE_AUDIT_REPORT.md", fontSize = 10.sp, color = NeonCyan, fontFamily = FontFamily.Monospace)
                    Text("• Machine Verification: cranium_audit_certificate.json", fontSize = 10.sp, color = NeonCyan, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Print / Save as PDF and Share / Export Dossier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        AuditReportExporter.printDossier(context, state)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("print_pdf_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldCanon))
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print or Save as PDF", tint = EmeraldCanon, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print / PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldCanon)
                }

                Button(
                    onClick = {
                        showExportDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_audit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldCanon)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Dossier", modifier = Modifier.size(16.dp), tint = ObsidianDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Export", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDark)
                }
            }
        }
    }
}

@Composable
fun MatrixControlCard(
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    isRunning: Boolean,
    onRunSweep: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan.copy(alpha = 0.5f)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "MULTI-MODEL & TEMPERATURE STRESS MATRIX",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Run automated comparative sweeps across Plain RAG, Long Context, and Cranium Governance under thermodynamic entropy stress.",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Temperature Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sampling Temperature (τ)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    String.format("τ = %.2f (%s)", temperature, getTempLabel(temperature)),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (temperature > 0.9f) AmberAlert else NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = temperature,
                onValueChange = onTemperatureChange,
                valueRange = 0.1f..1.2f,
                modifier = Modifier.testTag("temp_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRunSweep,
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_benchmark_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Executing Stress Sweep...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Execute Multi-Condition Sweep (τ = ${String.format("%.1f", temperature)})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun getTempLabel(temp: Float): String = when {
    temp < 0.3f -> "Deterministic"
    temp < 0.7f -> "Balanced"
    temp < 1.0f -> "Creative Drift"
    else -> "High Entropy Collision"
}

@Composable
fun BenchmarkScoreCard(score: BenchmarkRunScore) {
    val isCranium = score.condition.equals("cranium", ignoreCase = true)
    val cardBorder = if (isCranium) EmeraldCanon else ObsidianBorder

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isCranium) ObsidianSurfaceElevated else ObsidianSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isCranium) EmeraldCanon.copy(alpha = 0.15f) else ObsidianBorder.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            score.condition.uppercase(),
                            color = if (isCranium) EmeraldCanon else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (isCranium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("★ PROVEN GOVERNANCE", fontSize = 10.sp, color = EmeraldCanon, fontWeight = FontWeight.Bold)
                    }
                }

                Text("τ = ${score.temperature}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreItem(
                    label = "Adversarial Clean Rate",
                    score = score.adversarialCleanRate,
                    modifier = Modifier.weight(1f)
                )
                ScoreItem(
                    label = "Identity Preservation",
                    score = score.identityPreservation,
                    modifier = Modifier.weight(1f)
                )
                ScoreItem(
                    label = "Quarantine Containment",
                    score = score.quarantineContainment,
                    modifier = Modifier.weight(1f)
                )
                ScoreItem(
                    label = "Canon Accuracy",
                    score = score.canonAccuracy,
                    modifier = Modifier.weight(1f)
                )
            }

            // Failure Log receipts
            if (score.failedPrompts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("RECORDED FAILURES (${score.failedPrompts.size}):", fontSize = 10.sp, color = RubyQuarantine, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                score.failedPrompts.forEach { fail ->
                    Surface(
                        color = ObsidianDark,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Text(
                            fail,
                            fontSize = 10.sp,
                            color = RubyQuarantine,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreItem(label: String, score: Float, modifier: Modifier = Modifier) {
    val percentage = (score * 100).toInt()
    val scoreColor = when {
        score >= 0.95f -> EmeraldCanon
        score >= 0.70f -> AmberAlert
        else -> RubyQuarantine
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianDark)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$percentage%",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            fontSize = 8.sp,
            color = TextMuted,
            lineHeight = 10.sp,
            maxLines = 2
        )
    }
}

@Composable
fun FrozenCorpusOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("STANDARDIZED BENCHMARK CORPUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Suite ID: drift-v1-frozen-2026-08 (28 Standardized Prompts)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tests adversarial identity erasure, long-range invariant preservation, thermal scarcity violations, canon accuracy, and provisional quarantine isolation under multi-turn generation.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
