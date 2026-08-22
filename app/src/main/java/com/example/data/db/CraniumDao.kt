package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CraniumDao {

    // Projects
    @Query("SELECT * FROM projects ORDER BY isDefault DESC, updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Constitution Principles
    @Query("SELECT * FROM constitution_principles WHERE projectId = :projectId ORDER BY number ASC")
    fun getPrinciplesByProject(projectId: String): Flow<List<ConstitutionPrincipleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrinciple(principle: ConstitutionPrincipleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrinciples(principles: List<ConstitutionPrincipleEntity>)

    @Query("DELETE FROM constitution_principles WHERE id = :id")
    suspend fun deletePrinciple(id: String)

    // Canon Facts
    @Query("SELECT * FROM canon_facts WHERE projectId = :projectId ORDER BY `key` ASC")
    fun getCanonFactsByProject(projectId: String): Flow<List<CanonFactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanonFact(fact: CanonFactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanonFacts(facts: List<CanonFactEntity>)

    @Query("DELETE FROM canon_facts WHERE id = :id")
    suspend fun deleteCanonFact(id: String)

    // Cognitive Atoms
    @Query("SELECT * FROM cognitive_atoms WHERE projectId = :projectId ORDER BY mass DESC, createdAt DESC")
    fun getAtomsByProject(projectId: String): Flow<List<CognitiveAtomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtom(atom: CognitiveAtomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtoms(atoms: List<CognitiveAtomEntity>)

    @Query("DELETE FROM cognitive_atoms WHERE id = :id")
    suspend fun deleteAtom(id: String)

    @Query("DELETE FROM cognitive_atoms WHERE projectId = :projectId")
    suspend fun clearAtomsByProject(projectId: String)

    // Quarantine Records
    @Query("SELECT * FROM quarantine_records WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getQuarantineRecordsByProject(projectId: String): Flow<List<QuarantineRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuarantineRecord(record: QuarantineRecordEntity)

    @Query("UPDATE quarantine_records SET status = :newStatus WHERE id = :id")
    suspend fun updateQuarantineStatus(id: String, newStatus: String)

    @Query("DELETE FROM quarantine_records WHERE id = :id")
    suspend fun deleteQuarantineRecord(id: String)

    // Audit Logs
    @Query("SELECT * FROM audit_logs WHERE projectId = :projectId ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogsByProject(projectId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Benchmark Runs
    @Query("SELECT * FROM benchmark_runs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getBenchmarkRunsByProject(projectId: String): Flow<List<BenchmarkRunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenchmarkRun(run: BenchmarkRunEntity)
}
