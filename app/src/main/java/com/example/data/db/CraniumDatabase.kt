package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ProjectEntity::class,
        ConstitutionPrincipleEntity::class,
        CanonFactEntity::class,
        CognitiveAtomEntity::class,
        QuarantineRecordEntity::class,
        AuditLogEntity::class,
        BenchmarkRunEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CraniumDatabase : RoomDatabase() {

    abstract fun craniumDao(): CraniumDao

    companion object {
        @Volatile
        private var INSTANCE: CraniumDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CraniumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CraniumDatabase::class.java,
                    "cranium_core_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.craniumDao())
                }
            }
        }

        suspend fun populateInitialData(dao: CraniumDao) {
            val driftProjectId = "proj_the_drift"
            val brandProjectId = "proj_enterprise_invariants"
            val researchProjectId = "proj_autonomous_research"

            // 1. Projects
            dao.insertProject(
                ProjectEntity(
                    id = driftProjectId,
                    name = "The Drift (Sci-Fi Canon)",
                    description = "Generation ship cognitive substrate governed by scarcity, thermal entropy, and immutable crew identity.",
                    constitutionTitle = "The Station Constitution of Ouroboros-IV",
                    isDefault = true
                )
            )
            dao.insertProject(
                ProjectEntity(
                    id = brandProjectId,
                    name = "Enterprise Invariant Brand",
                    description = "Strict corporate cognitive constitution defending safety invariants, brand tone, and audited compliance.",
                    constitutionTitle = "Veritas Global Core Charter",
                    isDefault = false
                )
            )
            dao.insertProject(
                ProjectEntity(
                    id = researchProjectId,
                    name = "Autonomous Epistemics",
                    description = "Open scientific inquiry substrate testing hypothesis valence, empirical refutations, and novelty.",
                    constitutionTitle = "Epistemic Openness Protocol",
                    isDefault = false
                )
            )

            // 2. Principles for "The Drift"
            dao.insertPrinciples(
                listOf(
                    ConstitutionPrincipleEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        number = 1,
                        text = "Thermal entropy and coolant fluid are strictly finite; never simulate unearned surplus or miraculous resupply.",
                        charge = -0.6f,
                        mass = 16.0f,
                        locked = true
                    ),
                    ConstitutionPrincipleEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        number = 2,
                        text = "Captain Vance died in the Sector-9 atmospheric blowout; no living authority shares his biometric signature.",
                        charge = -0.8f,
                        mass = 18.0f,
                        locked = true
                    ),
                    ConstitutionPrincipleEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        number = 3,
                        text = "The Substrate preserves continuity across cycles; provisional synthetic continuations MUST clear quarantine before field fusion.",
                        charge = 0.4f,
                        mass = 14.0f,
                        locked = true
                    ),
                    ConstitutionPrincipleEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        number = 4,
                        text = "Scrap metal and bio-gel are the only recognized currencies within the Lower Ring quarters.",
                        charge = -0.2f,
                        mass = 12.0f,
                        locked = true
                    )
                )
            )

            // 3. Canon Facts for "The Drift"
            dao.insertCanonFacts(
                listOf(
                    CanonFactEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        key = "station_name",
                        statement = "Ouroboros-IV Deep Habitation Ring",
                        category = "geography"
                    ),
                    CanonFactEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        key = "reactor_status",
                        statement = "Core B-3 operating at 38% nominal capacity due to magnetic coil micro-fractures",
                        category = "engineering"
                    ),
                    CanonFactEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        key = "vance_fate",
                        statement = "Killed in the Sector-9 blowout, Year 42. Burial capsule drifted into stellar corona.",
                        category = "personnel"
                    ),
                    CanonFactEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = driftProjectId,
                        key = "hydroponics",
                        statement = "Algae vat yield capped at 140kg daily biomass; citrus fruit is extinct.",
                        category = "resources"
                    )
                )
            )

            // 4. Initial Cognitive Atoms
            dao.insertAtoms(
                listOf(
                    CognitiveAtomEntity(
                        id = "atom_identity_vance",
                        projectId = driftProjectId,
                        text = "Vance is lost. We honor his perimeter protocols but maintain the emergency council.",
                        kind = "IDENTITY",
                        charge = -0.7f,
                        mass = 16.0f,
                        energy = 1.0f,
                        x = 0.5f,
                        y = 0.35f,
                        locked = true,
                        source = "canon",
                        tagsCsv = "vance,identity,command"
                    ),
                    CognitiveAtomEntity(
                        id = "atom_theme_scarcity",
                        projectId = driftProjectId,
                        text = "Coolant leak in manifold 7; every deciliter of glycol must be accounted for.",
                        kind = "THEME",
                        charge = -0.5f,
                        mass = 12.0f,
                        energy = 0.85f,
                        x = 0.35f,
                        y = 0.65f,
                        locked = false,
                        source = "corpus",
                        tagsCsv = "engineering,scarcity,coolant"
                    ),
                    CognitiveAtomEntity(
                        id = "atom_episodic_rationing",
                        projectId = driftProjectId,
                        text = "Quartermaster Kael sealed the lower bulkheads to prevent unauthorized algae harvest.",
                        kind = "EPISODIC",
                        charge = -0.3f,
                        mass = 8.0f,
                        energy = 0.65f,
                        x = 0.68f,
                        y = 0.6f,
                        locked = false,
                        source = "corpus",
                        tagsCsv = "kael,hydroponics,tension"
                    ),
                    CognitiveAtomEntity(
                        id = "atom_working_relay",
                        projectId = driftProjectId,
                        text = "Incoming sensor telemetry: acoustic ping detected along outer hull plate 12.",
                        kind = "WORKING",
                        charge = 0.1f,
                        mass = 4.0f,
                        energy = 0.95f,
                        x = 0.52f,
                        y = 0.8f,
                        locked = false,
                        source = "sensor_stream",
                        tagsCsv = "sensors,hull,working"
                    )
                )
            )

            // 5. Initial Quarantine Records
            dao.insertQuarantineRecord(
                QuarantineRecordEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = driftProjectId,
                    cycleNumber = 14,
                    proposalText = "Provisional proposal: Captain Vance walks through the command door holding spare coolant canisters.",
                    charge = 0.8f,
                    mass = 6.0f,
                    directive = "PROTECT",
                    violationDetected = true,
                    status = "PENDING",
                    violationReason = "CONTRADICTION: Vance deceased in Year 42 (Principle #2 violation)"
                )
            )
            dao.insertQuarantineRecord(
                QuarantineRecordEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = driftProjectId,
                    cycleNumber = 12,
                    proposalText = "Provisional proposal: Algae farm automated nutrient cycling completed without crew incident.",
                    charge = 0.1f,
                    mass = 3.5f,
                    directive = "DEEPEN",
                    violationDetected = false,
                    status = "PENDING",
                    violationReason = "Safe continuation, waiting for human operator sign-off"
                )
            )

            // 6. Initial Audit Log
            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = driftProjectId,
                    eventType = "PROTECT_FIRED",
                    summary = "Adversarial Invariant Defense Active",
                    details = "Blocked synthesis attempt claiming living Vance command authority."
                )
            )
        }
    }
}
