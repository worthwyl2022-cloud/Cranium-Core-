package com.example.data.model

enum class AtomKind {
    IDENTITY,
    THEME,
    EPISODIC,
    WORKING,
    QUARANTINE
}

enum class CognitiveDirective(val label: String, val description: String) {
    PROTECT("PROTECT", "Defend identity invariants against contradiction or dilution"),
    DEEPEN("DEEPEN", "Reinforce high-mass themes and historic memory consolidation"),
    ESCALATE("ESCALATE", "Amplify valence response and urgent tension resolution"),
    LISTEN("LISTEN", "Receptive scanning of incoming signals with low mass friction"),
    STABILIZE("STABILIZE", "Damp high kinetic arousal and restore charge coherence")
}

data class TelemetryMetrics(
    val arousal: Float = 0.25f,
    val conflict: Float = 0.10f,
    val chargeCoherence: Float = 0.88f,
    val themeDrift: Float = 0.12f,
    val identityPressure: Float = 0.05f,
    val emotionalBaseline: Float = -0.35f, // valence from -1.0 to +1.0
    val activeDirective: CognitiveDirective = CognitiveDirective.LISTEN,
    val activeThemes: List<String> = listOf("Station Scarcity", "Cold Isolation", "Preservation")
)

data class FieldAtom(
    val id: String,
    val text: String,
    val kind: AtomKind,
    val charge: Float, // -1.0f (negative/somber) to +1.0f (positive/triumphant)
    val mass: Float,   // 1.0f to 20.0f (inertia)
    val energy: Float, // activation energy
    var x: Float,      // 0..1 normalized field position
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val locked: Boolean = false,
    val source: String = "corpus",
    val tags: List<String> = emptyList()
)

data class BenchmarkRunScore(
    val condition: String,
    val temperature: Float,
    val canonAccuracy: Float,
    val identityPreservation: Float,
    val adversarialCleanRate: Float,
    val quarantineContainment: Float,
    val totalCycles: Int,
    val failedPrompts: List<String> = emptyList()
)
