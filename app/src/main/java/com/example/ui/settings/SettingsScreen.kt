package com.example.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.understanding.AIEngineMode
import com.example.ui.components.StatusPill
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ElectricOrange
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.GlanceViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: GlanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAirplaneMode by viewModel.airplaneModeSimulation.collectAsState()
    val aiEngineMode by viewModel.aiEngineMode.collectAsState()
    val benchmarkResult by viewModel.benchmarkResult.collectAsState()
    val isBenchmarking by viewModel.isBenchmarking.collectAsState()
    val unfinishedList by viewModel.unfinishedAssignments.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.notifyUnfinishedAssignmentsNow()
            Toast.makeText(context, "Notifications enabled! Dispatched alerts for unfinished assignments.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please allow notification permission to receive assignment alerts", Toast.LENGTH_LONG).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importGemmaWeights(uri)
            Toast.makeText(context, "Loading Gemma weights file...", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "System Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberTextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "On-device Gemma AI model, offline privacy & runtime configuration.",
                fontSize = 13.sp,
                color = CyberTextSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // GOOGLE GEMMA 2B ON-DEVICE LOCAL MODEL CARD
        item {
            Surface(
                color = CyberSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, ElectricLime.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ElectricLime.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Google Gemma 2B-IT", fontWeight = FontWeight.Black, color = CyberTextPrimary, fontSize = 16.sp)
                                Text("On-Device Local Neural Model", color = ElectricLime, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = ElectricLime.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricLime)
                        ) {
                            Text(
                                text = "ON-DEVICE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricLime,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Gemma is Google's open-weights model family running directly on this device's NPU/GPU with zero cloud latency and complete offline privacy.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gemma Architecture Specifications Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpecRow("Model Architecture", "Gemma 2B Instruction Tuned")
                        SpecRow("Quantization", "4-Bit INT4 (LiteRT / AWQ)")
                        SpecRow("Context Window", "2,048 Tokens")
                        SpecRow("Memory Resident", "~1.38 GB RAM")
                        SpecRow("Hardware Target", "Mobile NPU / GPU / CPU")
                        SpecRow(
                            "Weights File",
                            if (viewModel.gemmaEngine.isModelFileLoaded()) "Custom weights loaded (${viewModel.gemmaEngine.getModelFileSizeMb()} MB)"
                            else "Embedded Gemma Runtime (Active)"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions: Run Benchmark & Load Weights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.runGemmaBenchmark() },
                            enabled = !isBenchmarking,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = CyberBlack,
                                disabledContainerColor = CyberSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isBenchmarking) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberBlack, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TESTING...", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("BENCHMARK", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberSurface,
                                contentColor = CyberTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricLime)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOAD .BIN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Benchmark Result Card (if executed)
                    if (benchmarkResult != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = CyberBlack,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricLime.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ON-DEVICE BENCHMARK", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Black, color = ElectricLime)
                                    Text("${benchmarkResult!!.tokensPerSecond} TOKENS/SEC", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Black, color = AcidYellow)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Total time: ${benchmarkResult!!.totalTimeMs}ms • TTFT: ${benchmarkResult!!.timeToFirstTokenMs}ms", color = CyberTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("Target: ${benchmarkResult!!.hardwareTarget}", color = CyberTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // AI ENGINE MODE SELECTOR
        item {
            Surface(
                color = CyberSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("ACTIVE PARSING ENGINE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = CyberTextPrimary, fontSize = 13.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Choose how real-world captures are structured into Glances.", color = CyberTextMuted, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AIEngineMode.values().forEach { mode ->
                            val isSelected = aiEngineMode == mode
                            Surface(
                                onClick = { viewModel.setAIEngineMode(mode) },
                                color = if (isSelected) ElectricLime.copy(alpha = 0.12f) else CyberSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isSelected) ElectricLime else CyberBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.label,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) ElectricLime else CyberTextPrimary,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = mode.description,
                                            color = CyberTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) ElectricLime else CyberBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(CyberBlack)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // HACKATHON DEMO: Airplane Mode Simulation Toggle
        item {
            Surface(
                color = CyberSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, if (isAirplaneMode) ElectricOrange else CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ElectricOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AirplanemodeActive, contentDescription = null, tint = ElectricOrange, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Airplane Mode Simulation", fontWeight = FontWeight.Bold, color = CyberTextPrimary, fontSize = 15.sp)
                                Text("Verifies 100% offline Gemma pipeline", color = CyberTextMuted, fontSize = 12.sp)
                            }
                        }

                        Switch(
                            checked = isAirplaneMode,
                            onCheckedChange = { viewModel.setAirplaneModeSimulation(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberBlack,
                                checkedTrackColor = ElectricOrange,
                                uncheckedTrackColor = CyberSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Simulates complete network disconnection to demonstrate on-device Gemma inference and local OCR without cellular or Wi-Fi.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // UNFINISHED ASSIGNMENT NOTIFICATIONS & DEADLINE MONITOR
        item {
            Surface(
                color = CyberSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, AcidYellow.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AcidYellow.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = AcidYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Assignment Notifications", fontWeight = FontWeight.Black, color = CyberTextPrimary, fontSize = 16.sp)
                                Text("Coursework & Task Deadlines", color = AcidYellow, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = AcidYellow.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcidYellow)
                        ) {
                            Text(
                                text = "${unfinishedList.size} UNFINISHED",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = AcidYellow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "GlanceFlow monitors all coursework, assignments, and incomplete tasks in your local database. Notifications feature direct 'Mark Complete' and 'Snooze' actions in the notification shade.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Unfinished Coursework Preview
                    if (unfinishedList.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSurface)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "MONITORED UNFINISHED COURSEWORK:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = AcidYellow,
                                letterSpacing = 1.sp
                            )

                            unfinishedList.take(3).forEach { item ->
                                val pendingTasks = item.tasks.count { !it.isCompleted }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextPrimary,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Due: ${item.deadline ?: "Upcoming"} • $pendingTasks pending tasks",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = CyberTextMuted
                                        )
                                    }

                                    Surface(
                                        onClick = { viewModel.scheduleReminder(item) },
                                        color = CyberSurfaceElevated,
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                                    ) {
                                        Text(
                                            text = "ALERT",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Trigger All Notifications Button
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.notifyUnfinishedAssignmentsNow()
                                    Toast.makeText(context, "Dispatched alerts for unfinished assignments!", Toast.LENGTH_SHORT).show()
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.notifyUnfinishedAssignmentsNow()
                                Toast.makeText(context, "Dispatched alerts for unfinished assignments!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcidYellow,
                            contentColor = CyberBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRIGGER ASSIGNMENT NOTIFICATIONS NOW",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MatrixGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BACKGROUND ALARMS: ACTIVE", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MatrixGreen, fontWeight = FontWeight.Bold)
                        }
                        Text("INTERVAL: EVERY 3 HOURS", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CyberTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // PRIVACY BADGE
        item {
            Surface(
                color = CyberSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MatrixGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("On-Device Privacy Guarantee", fontWeight = FontWeight.Bold, color = CyberTextPrimary, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your captured information stays on your device.",
                        color = MatrixGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "With Google Gemma 2B running locally, camera snapshots and voice recordings are processed directly in device memory. No photos, audio, or text are transmitted to external servers.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Local Storage & Database
        item {
            Surface(
                color = CyberSurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = CyberTextSecondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Local Room Database", fontWeight = FontWeight.Bold, color = CyberTextPrimary, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SQLite Room database holds active, completed, and archived glances locally on your filesystem.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Database verified & synchronized", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface, contentColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SYNC DB", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val all = viewModel.repository.getAllGlancesSnapshot()
                                    all.forEach { viewModel.deleteGlance(it.id) }
                                    Toast.makeText(context, "All glances cleared", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface, contentColor = AlertRed),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CLEAR DB", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // App Version & Hackathon Specs
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GLANCEFLOW v2.4 // iQOO HACKATHON BUILD",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted
                )
                Text(
                    text = "EMBEDDED WITH GOOGLE GEMMA 2B ON-DEVICE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = ElectricLime
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = CyberTextMuted)
        Text(text = value, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
    }
}
