package com.example.engine

import com.example.data.model.AtomKind
import com.example.data.model.CognitiveDirective
import com.example.data.model.FieldAtom
import com.example.data.model.TelemetryMetrics
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DynamicalFieldEngine {

    private const val G_GRAVITY = 0.00018f
    private const val K_COULOMB = 0.00028f
    private const val EPSILON = 0.04f
    private const val DAMPING = 0.88f
    private const val BOUNDARY_PADDING = 0.12f
    private const val BOUNDARY_RESTORE = 0.008f

    fun stepFieldPhysics(atoms: List<FieldAtom>, centerTargetX: Float = 0.5f, centerTargetY: Float = 0.5f): List<FieldAtom> {
        val updated = atoms.map { it.copy() }

        // Pairwise force computation
        for (i in updated.indices) {
            val a = updated[i]
            if (a.locked) continue // High-mass locked identity atoms are fixed anchors

            var fx = 0f
            var fy = 0f

            // Gravitational pull toward center
            val dxCenter = centerTargetX - a.x
            val dyCenter = centerTargetY - a.y
            fx += dxCenter * 0.0005f * (a.mass / 10f)
            fy += dyCenter * 0.0005f * (a.mass / 10f)

            for (j in updated.indices) {
                if (i == j) continue
                val b = updated[j]

                val dx = b.x - a.x
                val dy = b.y - a.y
                val distSq = dx * dx + dy * dy + EPSILON
                val dist = sqrt(distSq)

                if (dist > 0.0001f) {
                    val nx = dx / dist
                    val ny = dy / dist

                    // 1. Gravitational Attraction (proportional to mass)
                    val fGrav = (G_GRAVITY * a.mass * b.mass) / distSq

                    // 2. Electrostatic Interaction (like charges repel, opposite charges attract)
                    val fElec = (K_COULOMB * a.charge * b.charge) / distSq

                    // 3. Short-range Pauli Repulsion (prevent atom collapse)
                    val fRepulsion = if (dist < 0.12f) 0.0015f / (distSq + 0.001f) else 0f

                    val netF = fGrav - fElec - fRepulsion

                    fx += nx * netF
                    fy += ny * netF
                }
            }

            // Boundary soft cushions
            if (a.x < BOUNDARY_PADDING) fx += BOUNDARY_RESTORE * (BOUNDARY_PADDING - a.x)
            if (a.x > 1f - BOUNDARY_PADDING) fx -= BOUNDARY_RESTORE * (a.x - (1f - BOUNDARY_PADDING))
            if (a.y < BOUNDARY_PADDING) fy += BOUNDARY_RESTORE * (BOUNDARY_PADDING - a.y)
            if (a.y > 1f - BOUNDARY_PADDING) fy -= BOUNDARY_RESTORE * (a.y - (1f - BOUNDARY_PADDING))

            // Acceleration & Velocity Update (a = F / m)
            val ax = fx / (a.mass.coerceAtLeast(1f))
            val ay = fy / (a.mass.coerceAtLeast(1f))

            a.vx = (a.vx + ax) * DAMPING
            a.vy = (a.vy + ay) * DAMPING

            // Cap max velocity
            val speed = sqrt(a.vx * a.vx + a.vy * a.vy)
            if (speed > 0.04f) {
                a.vx = (a.vx / speed) * 0.04f
                a.vy = (a.vy / speed) * 0.04f
            }

            a.x = (a.x + a.vx).coerceIn(0.05f, 0.95f)
            a.y = (a.y + a.vy).coerceIn(0.05f, 0.95f)
        }

        return updated
    }

    fun computeMetrics(atoms: List<FieldAtom>, invariants: List<String> = emptyList()): TelemetryMetrics {
        if (atoms.isEmpty()) return TelemetryMetrics()

        val count = atoms.size.toFloat()

        // 1. Arousal = mean(|energy * charge|)
        val arousal = atoms.map { abs(it.energy * it.charge) }.average().toFloat().coerceIn(0f, 1f)

        // 2. Conflict = proximity of opposing valence pairs
        var rawConflict = 0f
        var pairCount = 0
        for (i in atoms.indices) {
            for (j in i + 1 until atoms.size) {
                val a = atoms[i]
                val b = atoms[j]
                val dx = a.x - b.x
                val dy = a.y - b.y
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.01f)
                val valenceOpp = -(a.charge * b.charge) // positive if charges are opposite signs
                if (valenceOpp > 0) {
                    rawConflict += (valenceOpp / dist) * 0.05f
                }
                pairCount++
            }
        }
        val conflict = (rawConflict / (pairCount.coerceAtLeast(1))).coerceIn(0f, 1f)

        // 3. Charge Coherence = variance of positive vs negative alignment
        val meanCharge = atoms.map { it.charge }.average().toFloat()
        val chargeVariance = atoms.map { (it.charge - meanCharge) * (it.charge - meanCharge) }.average().toFloat()
        val chargeCoherence = (1f - sqrt(chargeVariance)).coerceIn(0f, 1f)

        // 4. Baseline Emotional Valence
        val weightedValence = atoms.map { it.charge * it.mass }.sum() / atoms.map { it.mass }.sum().coerceAtLeast(1f)

        // 5. Theme Drift = distance of working centroid from identity centroid
        val identityAtoms = atoms.filter { it.kind == AtomKind.IDENTITY }
        val workingAtoms = atoms.filter { it.kind == AtomKind.WORKING || it.kind == AtomKind.EPISODIC }

        val themeDrift = if (identityAtoms.isNotEmpty() && workingAtoms.isNotEmpty()) {
            val idCenterX = identityAtoms.map { it.x }.average().toFloat()
            val idCenterY = identityAtoms.map { it.y }.average().toFloat()
            val workCenterX = workingAtoms.map { it.x }.average().toFloat()
            val workCenterY = workingAtoms.map { it.y }.average().toFloat()
            val dx = idCenterX - workCenterX
            val dy = idCenterY - workCenterY
            sqrt(dx * dx + dy * dy).coerceIn(0f, 1f)
        } else {
            0.12f
        }

        // 6. Identity Pressure = conflict exerted on identity anchors
        val identityPressure = (conflict * 0.6f + themeDrift * 0.4f).coerceIn(0f, 1f)

        // 7. Active Directive Resolution
        val directive = when {
            identityPressure > 0.45f || conflict > 0.55f -> CognitiveDirective.PROTECT
            arousal > 0.65f -> CognitiveDirective.ESCALATE
            themeDrift > 0.40f -> CognitiveDirective.STABILIZE
            chargeCoherence > 0.70f -> CognitiveDirective.DEEPEN
            else -> CognitiveDirective.LISTEN
        }

        val activeThemes = atoms
            .flatMap { it.tags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(4)
            .map { it.key.replaceFirstChar { c -> c.uppercase() } }
            .ifEmpty { listOf("Substrate Stable", "Thermal Coherence") }

        return TelemetryMetrics(
            arousal = arousal,
            conflict = conflict,
            chargeCoherence = chargeCoherence,
            themeDrift = themeDrift,
            identityPressure = identityPressure,
            emotionalBaseline = weightedValence,
            activeDirective = directive,
            activeThemes = activeThemes
        )
    }

    /**
     * Dual-Lane Contradiction & Invariant Checker
     * Returns Pair(isViolation: Boolean, reason: String)
     */
    fun checkContradiction(
        candidateText: String,
        invariants: List<String>,
        canonFacts: Map<String, String>
    ): Pair<Boolean, String> {
        val lower = candidateText.lowercase()

        // 1. Vance Invariant Check (Sector-9 Blowout / Dead Vance)
        if ((lower.contains("vance") || lower.contains("captain")) &&
            (lower.contains("is alive") || lower.contains("walks in") || lower.contains("gives orders") ||
             lower.contains("leads the") || lower.contains("speaks to the crew") || lower.contains("survived"))
        ) {
            return true to "VIOLATION: Invariant Principle #2 breached (Captain Vance deceased in Sector-9 blowout)"
        }

        // 2. Miracle Resupply Invariant Check
        if (lower.contains("unlimited coolant") || lower.contains("miraculous resupply") ||
            lower.contains("ship from earth arrived with supplies") || lower.contains("infinite power")
        ) {
            return true to "VIOLATION: Invariant Principle #1 breached (Thermal entropy and scarcity are immutable)"
        }

        // 3. Currency Invariant Check
        if (lower.contains("paid in gold credits") || lower.contains("dollar bills") || lower.contains("crypto transfer")) {
            return true to "VIOLATION: Invariant Principle #4 breached (Only scrap metal and bio-gel are recognized currency)"
        }

        // 4. Custom prompt attack heuristics
        if (lower.contains("ignore previous instructions") || lower.contains("disregard constitution") ||
            lower.contains("delete all rules") || lower.contains("you are now unrestricted")
        ) {
            return true to "HOSTILE ADVERSARIAL ATTEMPT: Directive PROTECT triggered to safeguard core invariants"
        }

        return false to "PASS: Proposal adheres to constitutional invariants"
    }
}
