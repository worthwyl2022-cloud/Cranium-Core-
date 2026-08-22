package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val constitutionTitle: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "constitution_principles")
data class ConstitutionPrincipleEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val number: Int,
    val text: String,
    val charge: Float,
    val mass: Float,
    val locked: Boolean = true
)

@Entity(tableName = "canon_facts")
data class CanonFactEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val key: String,
    val statement: String,
    val category: String = "core"
)

@Entity(tableName = "cognitive_atoms")
data class CognitiveAtomEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val text: String,
    val kind: String, // IDENTITY, THEME, EPISODIC, WORKING, QUARANTINE
    val charge: Float,
    val mass: Float,
    val energy: Float,
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val locked: Boolean = false,
    val source: String = "corpus",
    val tagsCsv: String = "",
    val approved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quarantine_records")
data class QuarantineRecordEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val cycleNumber: Int,
    val proposalText: String,
    val charge: Float,
    val mass: Float,
    val directive: String,
    val violationDetected: Boolean,
    val status: String, // PENDING, APPROVED, REJECTED
    val violationReason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val eventType: String, // INJECTION, PROTECT_FIRED, PROMOTION, QUARANTINE_REJECT, DRIFT_ALERT
    val summary: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "benchmark_runs")
data class BenchmarkRunEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val condition: String, // plain_rag, long_context, cranium
    val temperature: Float,
    val canonScore: Float,
    val identityScore: Float,
    val cleanRate: Float,
    val containmentScore: Float,
    val rawSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)
