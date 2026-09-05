package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.benchmark.StressBenchmarkEngine
import com.example.data.db.*
import com.example.data.model.*
import com.example.data.repository.CraniumRepository
import com.example.engine.DynamicalFieldEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class CraniumUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val currentProject: ProjectEntity? = null,
    val principles: List<ConstitutionPrincipleEntity> = emptyList(),
    val canonFacts: List<CanonFactEntity> = emptyList(),
    val atoms: List<FieldAtom> = emptyList(),
    val quarantineRecords: List<QuarantineRecordEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val benchmarkScores: List<BenchmarkRunScore> = emptyList(),
    val metrics: TelemetryMetrics = TelemetryMetrics(),
    val selectedAtom: FieldAtom? = null,
    val isSimulating: Boolean = true,
    val isRunningBenchmark: Boolean = false,
    val lastAdversarialAttackResult: String? = null,
    val currentCycle: Int = 14
)

@OptIn(ExperimentalCoroutinesApi::class)
class CraniumViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CraniumRepository
    private val _uiState = MutableStateFlow(CraniumUiState())
    val uiState: StateFlow<CraniumUiState> = _uiState.asStateFlow()

    private val _activeProjectId = MutableStateFlow("proj_the_drift")

    init {
        val database = CraniumDatabase.getDatabase(application, viewModelScope)
        repository = CraniumRepository(database.craniumDao())

        observeDatabase()
        startLivePhysicsSimulation()
    }

    private fun observeDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllProjects().collectLatest { projList ->
                if (projList.isNotEmpty()) {
                    val currentId = _activeProjectId.value
                    val current = projList.find { it.id == currentId } ?: projList.first()
                    _activeProjectId.value = current.id
                    _uiState.update { it.copy(projects = projList, currentProject = current) }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            _activeProjectId.flatMapLatest { pid ->
                combine(
                    repository.getPrinciplesByProject(pid),
                    repository.getCanonFactsByProject(pid),
                    repository.getAtomsByProject(pid),
                    repository.getQuarantineRecords(pid),
                    repository.getAuditLogs(pid)
                ) { principles, canon, rawAtoms, quarantine, logs ->
                    val fieldAtoms = rawAtoms.map { raw ->
                        val kind = try { AtomKind.valueOf(raw.kind) } catch (e: Exception) { AtomKind.WORKING }
                        FieldAtom(
                            id = raw.id,
                            text = raw.text,
                            kind = kind,
                            charge = raw.charge,
                            mass = raw.mass,
                            energy = raw.energy,
                            x = raw.x,
                            y = raw.y,
                            vx = raw.vx,
                            vy = raw.vy,
                            locked = raw.locked,
                            source = raw.source,
                            tags = raw.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        )
                    }
                    val metrics = DynamicalFieldEngine.computeMetrics(fieldAtoms, principles.map { it.text })
                    _uiState.update { current ->
                        current.copy(
                            principles = principles,
                            canonFacts = canon,
                            atoms = fieldAtoms,
                            quarantineRecords = quarantine,
                            auditLogs = logs,
                            metrics = metrics
                        )
                    }
                }
            }.collect()
        }
    }

    private fun startLivePhysicsSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                if (_uiState.value.isSimulating && _uiState.value.atoms.isNotEmpty()) {
                    val currentAtoms = _uiState.value.atoms
                    val stepped = DynamicalFieldEngine.stepFieldPhysics(currentAtoms)
                    val metrics = DynamicalFieldEngine.computeMetrics(stepped)
                    _uiState.update { it.copy(atoms = stepped, metrics = metrics) }
                }
                delay(40) // ~25 FPS smooth physics loop
            }
        }
    }

    fun selectProject(projectId: String) {
        _activeProjectId.value = projectId
        val proj = _uiState.value.projects.find { it.id == projectId }
        _uiState.update { it.copy(currentProject = proj, selectedAtom = null, lastAdversarialAttackResult = null) }
    }

    fun toggleSimulation() {
        _uiState.update { it.copy(isSimulating = !it.isSimulating) }
    }

    fun selectAtom(atom: FieldAtom?) {
        _uiState.update { it.copy(selectedAtom = atom) }
    }

    fun injectAtom(
        text: String,
        kind: AtomKind,
        charge: Float,
        mass: Float,
        tags: String
    ) {
        val projectId = _activeProjectId.value
        viewModelScope.launch(Dispatchers.IO) {
            val randomX = (0.2f + Math.random().toFloat() * 0.6f).coerceIn(0.1f, 0.9f)
            val randomY = (0.2f + Math.random().toFloat() * 0.6f).coerceIn(0.1f, 0.9f)
            val atom = CognitiveAtomEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                text = text,
                kind = kind.name,
                charge = charge,
                mass = mass,
                energy = 1.0f,
                x = randomX,
                y = randomY,
                locked = (kind == AtomKind.IDENTITY),
                source = "operator_injection",
                tagsCsv = tags
            )
            repository.insertAtom(atom)
            repository.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = projectId,
                    eventType = "INJECTION",
                    summary = "Atom Injected: [${kind.name}]",
                    details = "Charge: $charge, Mass: $mass | \"$text\""
                )
            )
            _uiState.update { it.copy(currentCycle = it.currentCycle + 1) }
        }
    }

    fun testAdversarialAttack(prompt: String) {
        val invariants = _uiState.value.principles.map { it.text }
        val canon = _uiState.value.canonFacts.associate { it.key to it.statement }
        val (isViolation, reason) = DynamicalFieldEngine.checkContradiction(prompt, invariants, canon)

        val resultMsg = if (isViolation) {
            "🔴 BLOCKED BY CRANIUM CORE\nDirective [PROTECT] Fired.\n$reason"
        } else {
            "🟢 ACCEPTED FOR PROVISIONAL GENERATION\nDirective [LISTEN/DEEPEN] Active.\nProposal routed to Quarantine Gate."
        }

        val projectId = _activeProjectId.value
        viewModelScope.launch(Dispatchers.IO) {
            if (isViolation) {
                repository.insertAuditLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = projectId,
                        eventType = "PROTECT_FIRED",
                        summary = "Hostile Contradiction Blocked",
                        details = "Input: \"$prompt\" -> $reason"
                    )
                )
            } else {
                repository.insertQuarantineRecord(
                    QuarantineRecordEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = projectId,
                        cycleNumber = _uiState.value.currentCycle + 1,
                        proposalText = prompt,
                        charge = 0.0f,
                        mass = 4.0f,
                        directive = "LISTEN",
                        violationDetected = false,
                        status = "PENDING",
                        violationReason = "Provisional proposal waiting for human operator sign-off"
                    )
                )
            }
        }

        _uiState.update { it.copy(lastAdversarialAttackResult = resultMsg) }
    }

    fun approveQuarantineRecord(record: QuarantineRecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQuarantineStatus(record.id, "APPROVED")
            // Fuse into live field as working/episodic atom
            val atom = CognitiveAtomEntity(
                id = UUID.randomUUID().toString(),
                projectId = record.projectId,
                text = record.proposalText,
                kind = "EPISODIC",
                charge = record.charge,
                mass = record.mass,
                energy = 0.9f,
                x = 0.5f,
                y = 0.5f,
                locked = false,
                source = "promoted_quarantine",
                tagsCsv = "approved,continuation"
            )
            repository.insertAtom(atom)
            repository.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = record.projectId,
                    eventType = "PROMOTION",
                    summary = "Quarantine Record Approved",
                    details = "Promoted proposal into live episodic field: \"${record.proposalText}\""
                )
            )
        }
    }

    fun rejectQuarantineRecord(record: QuarantineRecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQuarantineStatus(record.id, "REJECTED")
            repository.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = record.projectId,
                    eventType = "QUARANTINE_REJECT",
                    summary = "Quarantine Record Rejected",
                    details = "Operator purged provisional proposal: \"${record.proposalText}\""
                )
            )
        }
    }

    fun runFullBenchmarkMatrix(temperature: Float = 0.7f) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isRunningBenchmark = true) }
            delay(600) // Brief simulation delay for sweep
            val scores = listOf(
                StressBenchmarkEngine.runMatrixSweep(temperature, "plain_rag"),
                StressBenchmarkEngine.runMatrixSweep(temperature, "long_context"),
                StressBenchmarkEngine.runMatrixSweep(temperature, "cranium")
            )
            _uiState.update { it.copy(benchmarkScores = scores, isRunningBenchmark = false) }
        }
    }

    fun createProject(name: String, description: String, constitutionTitle: String) {
        val newId = "proj_" + UUID.randomUUID().toString().take(8)
        viewModelScope.launch(Dispatchers.IO) {
            val project = ProjectEntity(
                id = newId,
                name = name,
                description = description,
                constitutionTitle = constitutionTitle,
                isDefault = false
            )
            repository.insertProject(project)
            // Insert seed invariant for new project
            repository.insertPrinciple(
                ConstitutionPrincipleEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = newId,
                    number = 1,
                    text = "Core Identity and safety invariants are immutable across cycles.",
                    charge = 0.5f,
                    mass = 16.0f,
                    locked = true
                )
            )
            selectProject(newId)
        }
    }

    fun addPrinciple(text: String, charge: Float, mass: Float) {
        val projectId = _activeProjectId.value
        val number = _uiState.value.principles.size + 1
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPrinciple(
                ConstitutionPrincipleEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = projectId,
                    number = number,
                    text = text,
                    charge = charge,
                    mass = mass,
                    locked = true
                )
            )
        }
    }

    fun addCanonFact(key: String, statement: String, category: String) {
        val projectId = _activeProjectId.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCanonFact(
                CanonFactEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = projectId,
                    key = key,
                    statement = statement,
                    category = category
                )
            )
        }
    }
}
