package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow

class CraniumRepository(private val dao: CraniumDao) {

    fun getAllProjects(): Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun getProjectById(projectId: String): ProjectEntity? = dao.getProjectById(projectId)

    suspend fun insertProject(project: ProjectEntity) = dao.insertProject(project)

    suspend fun deleteProject(project: ProjectEntity) = dao.deleteProject(project)

    // Principles
    fun getPrinciplesByProject(projectId: String): Flow<List<ConstitutionPrincipleEntity>> =
        dao.getPrinciplesByProject(projectId)

    suspend fun insertPrinciple(principle: ConstitutionPrincipleEntity) = dao.insertPrinciple(principle)

    suspend fun deletePrinciple(id: String) = dao.deletePrinciple(id)

    // Canon Facts
    fun getCanonFactsByProject(projectId: String): Flow<List<CanonFactEntity>> =
        dao.getCanonFactsByProject(projectId)

    suspend fun insertCanonFact(fact: CanonFactEntity) = dao.insertCanonFact(fact)

    suspend fun deleteCanonFact(id: String) = dao.deleteCanonFact(id)

    // Atoms
    fun getAtomsByProject(projectId: String): Flow<List<CognitiveAtomEntity>> =
        dao.getAtomsByProject(projectId)

    suspend fun insertAtom(atom: CognitiveAtomEntity) = dao.insertAtom(atom)

    suspend fun deleteAtom(id: String) = dao.deleteAtom(id)

    suspend fun clearAtomsByProject(projectId: String) = dao.clearAtomsByProject(projectId)

    // Quarantine
    fun getQuarantineRecords(projectId: String): Flow<List<QuarantineRecordEntity>> =
        dao.getQuarantineRecordsByProject(projectId)

    suspend fun insertQuarantineRecord(record: QuarantineRecordEntity) =
        dao.insertQuarantineRecord(record)

    suspend fun updateQuarantineStatus(id: String, status: String) =
        dao.updateQuarantineStatus(id, status)

    suspend fun deleteQuarantineRecord(id: String) =
        dao.deleteQuarantineRecord(id)

    // Audit Logs
    fun getAuditLogs(projectId: String): Flow<List<AuditLogEntity>> =
        dao.getAuditLogsByProject(projectId)

    suspend fun insertAuditLog(log: AuditLogEntity) = dao.insertAuditLog(log)

    // Benchmarks
    fun getBenchmarkRuns(projectId: String): Flow<List<BenchmarkRunEntity>> =
        dao.getBenchmarkRunsByProject(projectId)

    suspend fun insertBenchmarkRun(run: BenchmarkRunEntity) =
        dao.insertBenchmarkRun(run)
}
