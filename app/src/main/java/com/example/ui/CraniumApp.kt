package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class CraniumNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    FIELD("Field", Icons.Filled.Hub, Icons.Outlined.Hub, "nav_tab_field"),
    QUARANTINE("Quarantine", Icons.Filled.Shield, Icons.Outlined.Shield, "nav_tab_quarantine"),
    CONSTITUTION("Constitution", Icons.Filled.Gavel, Icons.Outlined.Gavel, "nav_tab_constitution"),
    BENCHMARK("Stress Test", Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_tab_benchmark"),
    WORKSPACES("Workspaces", Icons.Filled.Folder, Icons.Outlined.Folder, "nav_tab_workspaces")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraniumApp(
    viewModel: CraniumViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(CraniumNavTab.FIELD) }

    val pendingQuarantineCount = state.quarantineRecords.count { it.status == "PENDING" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = NeonCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan))
                        ) {
                            Text(
                                "CRANIUM",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                state.currentProject?.name ?: "Cognitive Substrate",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Directive: [${state.metrics.activeDirective.label}]",
                                fontSize = 10.sp,
                                color = when (state.metrics.activeDirective) {
                                    com.example.data.model.CognitiveDirective.PROTECT -> RubyQuarantine
                                    com.example.data.model.CognitiveDirective.ESCALATE -> AmberAlert
                                    com.example.data.model.CognitiveDirective.DEEPEN -> VibrantPurple
                                    com.example.data.model.CognitiveDirective.LISTEN -> NeonCyan
                                    com.example.data.model.CognitiveDirective.STABILIZE -> EmeraldCanon
                                },
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = ObsidianSurfaceElevated,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (state.isSimulating) NeonCyan else AmberAlert, shape = RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (state.isSimulating) "LIVE DYNAMICS" else "PAUSED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                CraniumNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    val showBadge = tab == CraniumNavTab.QUARANTINE && pendingQuarantineCount > 0

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (showBadge) {
                                        Badge(
                                            containerColor = RubyQuarantine,
                                            contentColor = Color.White
                                        ) {
                                            Text("$pendingQuarantineCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = {
                            Text(
                                tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                CraniumNavTab.FIELD -> FieldTopologyScreen(state = state, viewModel = viewModel)
                CraniumNavTab.QUARANTINE -> QuarantineScreen(state = state, viewModel = viewModel)
                CraniumNavTab.CONSTITUTION -> ConstitutionScreen(state = state, viewModel = viewModel)
                CraniumNavTab.BENCHMARK -> StressBenchmarkScreen(state = state, viewModel = viewModel)
                CraniumNavTab.WORKSPACES -> WorkspacesScreen(state = state, viewModel = viewModel)
            }
        }
    }
}
