package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtomKind
import com.example.data.model.CognitiveDirective
import com.example.data.model.FieldAtom
import com.example.ui.CraniumUiState
import com.example.ui.CraniumViewModel
import com.example.ui.theme.*

@Composable
fun FieldTopologyScreen(
    state: CraniumUiState,
    viewModel: CraniumViewModel,
    modifier: Modifier = Modifier
) {
    var showInjectSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Telemetry HUD Bar
        item {
            TelemetryHUDCard(state = state, onToggleSim = { viewModel.toggleSimulation() })
        }

        // 2. Interactive Force Field Canvas
        item {
            FieldCanvasCard(
                atoms = state.atoms,
                selectedAtom = state.selectedAtom,
                onSelectAtom = { viewModel.selectAtom(it) }
            )
        }

        // 3. Selected Atom Inspector
        if (state.selectedAtom != null) {
            item {
                AtomInspectorCard(
                    atom = state.selectedAtom,
                    onDismiss = { viewModel.selectAtom(null) }
                )
            }
        }

        // 4. Directive & Theme Tags Bar
        item {
            DirectiveAndThemesSection(state = state)
        }

        // 5. Quick Injector Button
        item {
            Button(
                onClick = { showInjectSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("inject_atom_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Inject Cognitive Atom / Scene Step",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showInjectSheet) {
        InjectAtomDialog(
            onDismiss = { showInjectSheet = false },
            onInject = { text, kind, charge, mass, tags ->
                viewModel.injectAtom(text, kind, charge, mass, tags)
                showInjectSheet = false
            }
        )
    }
}

@Composable
fun TelemetryHUDCard(
    state: CraniumUiState,
    onToggleSim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (state.isSimulating) NeonCyan else AmberAlert)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "DYNAMICAL FIELD TELEMETRY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleSim,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Simulation",
                            tint = NeonCyan
                        )
                    }
                    Text(
                        "Cycle #${state.currentCycle}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricGauge(
                    label = "Arousal",
                    value = state.metrics.arousal,
                    color = if (state.metrics.arousal > 0.5f) AmberAlert else NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricGauge(
                    label = "Conflict",
                    value = state.metrics.conflict,
                    color = if (state.metrics.conflict > 0.4f) RubyQuarantine else VibrantPurple,
                    modifier = Modifier.weight(1f)
                )
                MetricGauge(
                    label = "Coherence",
                    value = state.metrics.chargeCoherence,
                    color = EmeraldCanon,
                    modifier = Modifier.weight(1f)
                )
                MetricGauge(
                    label = "Drift",
                    value = state.metrics.themeDrift,
                    color = if (state.metrics.themeDrift > 0.35f) AmberAlert else ElectricIndigo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricGauge(
    label: String,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceElevated)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            String.format("%.2f", value),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = ObsidianBorder
        )
    }
}

@Composable
fun FieldCanvasCard(
    atoms: List<FieldAtom>,
    selectedAtom: FieldAtom?,
    onSelectAtom: (FieldAtom?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(atoms) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height
                            // Find closest atom within tap radius
                            val clicked = atoms.minByOrNull { atom ->
                                val ax = atom.x * width
                                val ay = atom.y * height
                                val dx = offset.x - ax
                                val dy = offset.y - ay
                                dx * dx + dy * dy
                            }
                            if (clicked != null) {
                                val ax = clicked.x * width
                                val ay = clicked.y * height
                                val dist = kotlin.math.sqrt((offset.x - ax) * (offset.x - ax) + (offset.y - ay) * (offset.y - ay))
                                if (dist < 40.dp.toPx()) {
                                    onSelectAtom(clicked)
                                } else {
                                    onSelectAtom(null)
                                }
                            } else {
                                onSelectAtom(null)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw Radar Grid Background
                val cx = w / 2f
                val cy = h / 2f
                drawCircle(color = ObsidianBorder.copy(alpha = 0.4f), radius = w * 0.18f, center = Offset(cx, cy), style = Stroke(width = 1f))
                drawCircle(color = ObsidianBorder.copy(alpha = 0.4f), radius = w * 0.35f, center = Offset(cx, cy), style = Stroke(width = 1f))
                drawCircle(color = ObsidianBorder.copy(alpha = 0.3f), radius = w * 0.46f, center = Offset(cx, cy), style = Stroke(width = 1f))
                drawLine(color = ObsidianBorder.copy(alpha = 0.3f), start = Offset(0f, cy), end = Offset(w, cy), strokeWidth = 1f)
                drawLine(color = ObsidianBorder.copy(alpha = 0.3f), start = Offset(cx, 0f), end = Offset(cx, h), strokeWidth = 1f)

                // Draw Inter-Atom Force Lines
                for (i in atoms.indices) {
                    for (j in i + 1 until atoms.size) {
                        val a = atoms[i]
                        val b = atoms[j]
                        val ax = a.x * w
                        val ay = a.y * h
                        val bx = b.x * w
                        val by = b.y * h
                        val dist = kotlin.math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by))

                        if (dist < w * 0.45f) {
                            val alpha = ((w * 0.45f - dist) / (w * 0.45f) * 0.4f).coerceIn(0f, 0.4f)
                            val lineColor = if (a.charge * b.charge > 0) NeonCyan else RubyQuarantine
                            drawLine(
                                color = lineColor.copy(alpha = alpha),
                                start = Offset(ax, ay),
                                end = Offset(bx, by),
                                strokeWidth = 1.2f
                            )
                        }
                    }
                }

                // Draw Atoms
                atoms.forEach { atom ->
                    val ax = atom.x * w
                    val ay = atom.y * h
                    val radius = (6f + (atom.mass / 20f) * 12f).coerceIn(8f, 20f)

                    val nodeColor = when (atom.kind) {
                        AtomKind.IDENTITY -> VibrantPurple
                        AtomKind.THEME -> NeonCyan
                        AtomKind.EPISODIC -> EmeraldCanon
                        AtomKind.WORKING -> if (atom.charge >= 0) NeonCyan else AmberAlert
                        AtomKind.QUARANTINE -> RubyQuarantine
                    }

                    // Outer pulse/glow ring
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.25f),
                        radius = radius + 6f,
                        center = Offset(ax, ay)
                    )

                    // Core Node
                    drawCircle(
                        color = nodeColor,
                        radius = radius,
                        center = Offset(ax, ay)
                    )

                    // Selected Halo
                    if (selectedAtom?.id == atom.id) {
                        drawCircle(
                            color = Color.White,
                            radius = radius + 10f,
                            center = Offset(ax, ay),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }

            // Overlay Helper Text
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegendChip("Identity (Invariant)", VibrantPurple)
                LegendChip("Theme", NeonCyan)
                LegendChip("Episodic", EmeraldCanon)
                LegendChip("Working", AmberAlert)
            }
        }
    }
}

@Composable
fun LegendChip(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ObsidianDark.copy(alpha = 0.8f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = TextSecondary)
    }
}

@Composable
fun AtomInspectorCard(
    atom: FieldAtom,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (atom.kind) {
                        AtomKind.IDENTITY -> VibrantPurple
                        AtomKind.THEME -> NeonCyan
                        AtomKind.EPISODIC -> EmeraldCanon
                        AtomKind.WORKING -> AmberAlert
                        AtomKind.QUARANTINE -> RubyQuarantine
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text(atom.kind.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = badgeColor.copy(alpha = 0.2f),
                            labelColor = badgeColor
                        ),
                        border = null
                    )
                    if (atom.locked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🔒 LOCKED INVARIANT", fontSize = 10.sp, color = VibrantPurple, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                atom.text,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Mass: ${atom.mass}m", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                Text("Charge: ${String.format("%+.2f", atom.charge)}q", fontSize = 11.sp, color = if (atom.charge >= 0) NeonCyan else AmberAlert, fontFamily = FontFamily.Monospace)
                Text("Energy: ${String.format("%.2f", atom.energy)}E", fontSize = 11.sp, color = EmeraldCanon, fontFamily = FontFamily.Monospace)
                Text("Src: ${atom.source}", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
fun DirectiveAndThemesSection(state: CraniumUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ACTIVE DIRECTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)

                val dirColor = when (state.metrics.activeDirective) {
                    CognitiveDirective.PROTECT -> RubyQuarantine
                    CognitiveDirective.ESCALATE -> AmberAlert
                    CognitiveDirective.DEEPEN -> VibrantPurple
                    CognitiveDirective.LISTEN -> NeonCyan
                    CognitiveDirective.STABILIZE -> EmeraldCanon
                }

                Surface(
                    color = dirColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(dirColor))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dirColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            state.metrics.activeDirective.label,
                            color = dirColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                state.metrics.activeDirective.description,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text("ACTIVE THEMES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.metrics.activeThemes) { theme ->
                    Surface(
                        color = ObsidianSurfaceElevated,
                        shape = RoundedCornerShape(6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
                    ) {
                        Text(
                            "#$theme",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InjectAtomDialog(
    onDismiss: () -> Unit,
    onInject: (String, AtomKind, Float, Float, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf(AtomKind.WORKING) }
    var charge by remember { mutableFloatStateOf(0.0f) }
    var mass by remember { mutableFloatStateOf(4.0f) }
    var tags by remember { mutableStateOf("sensors, field") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inject Cognitive Atom", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Atom Text / Scene Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Kind Selector
                Text("Kind:", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(AtomKind.WORKING, AtomKind.EPISODIC, AtomKind.THEME).forEach { kind ->
                        FilterChip(
                            selected = selectedKind == kind,
                            onClick = { selectedKind = kind },
                            label = { Text(kind.name, fontSize = 10.sp) }
                        )
                    }
                }

                // Charge slider
                Column {
                    Text("Valence Charge: ${String.format("%+.2f", charge)}", fontSize = 12.sp, color = TextSecondary)
                    Slider(
                        value = charge,
                        onValueChange = { charge = it },
                        valueRange = -1.0f..1.0f
                    )
                }

                // Mass slider
                Column {
                    Text("Mass (Inertia): ${String.format("%.1f", mass)}", fontSize = 12.sp, color = TextSecondary)
                    Slider(
                        value = mass,
                        onValueChange = { mass = it },
                        valueRange = 1.0f..20.0f
                    )
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onInject(text, selectedKind, charge, mass, tags)
                    }
                }
            ) {
                Text("Inject to Field")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
