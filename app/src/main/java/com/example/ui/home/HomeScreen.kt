package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import com.example.ui.components.CircularDataWidget
import com.example.ui.components.GlanceArtifact
import com.example.ui.components.TechnicalStatusIndicator
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.PosterCream
import com.example.ui.theme.PosterInk
import com.example.ui.theme.PosterRed
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.WarmOrange
import com.example.ui.viewmodel.GlanceViewModel
import java.util.Calendar

/**
 * SCREEN 1 — GLANCE HOME (DIGITAL POSTER COMPOSITION)
 * Full-screen visual canvas, enormous typography hero (50-70% occupancy),
 * circular data widgets, and a floating physical Glance artifact overlapping the text.
 */
@Composable
fun HomeScreen(
    viewModel: GlanceViewModel,
    onNavigateToCapture: () -> Unit,
    onNavigateToGlances: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeGlances by viewModel.activeGlances.collectAsState()
    val isAirplaneMode by viewModel.airplaneModeSimulation.collectAsState()
    val aiEngineMode by viewModel.aiEngineMode.collectAsState()

    var showTextInputDialog by remember { mutableStateOf(false) }
    var showVoiceInputDialog by remember { mutableStateOf(false) }
    var showDemoPresetsSheet by remember { mutableStateOf(false) }
    var showActiveActionPoster by remember { mutableStateOf(false) }

    val urgentCount = activeGlances.count { it.priority == GlancePriority.HIGH }
    val dueTodayCount = activeGlances.count {
        it.deadline?.contains("Today", ignoreCase = true) == true ||
        it.deadline?.contains("Friday", ignoreCase = true) == true
    }

    val featuredGlance = activeGlances.firstOrNull { it.priority == GlancePriority.HIGH }
        ?: activeGlances.firstOrNull()

    val greetingMeta = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "GOOD MORNING // HARSHITH"
            in 12..16 -> "GOOD AFTERNOON // HARSHITH"
            else -> "GOOD EVENING // HARSHITH"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp)
        ) {
            // TOP METADATA & TECHNICAL INDICATOR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = greetingMeta,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SubtitleGray,
                        letterSpacing = 1.2.sp
                    )

                    TechnicalStatusIndicator(
                        text = when {
                            isAirplaneMode -> "AIRPLANE MODE (GEMMA)"
                            aiEngineMode == com.example.ai.understanding.AIEngineMode.GEMMA_LOCAL -> "GEMMA 2B ON-DEVICE"
                            aiEngineMode == com.example.ai.understanding.AIEngineMode.OFFLINE_NLP -> "LOCAL NLP ACTIVE"
                            else -> "GEMINI CLOUD ACTIVE"
                        },
                        isActive = true,
                        accentColor = ElectricLime
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // CIRCULAR DATA INSTRUMENTS ROW
            // [14 GLANCES]  [02 DUE]  [01 URGENT]
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularDataWidget(
                        count = String.format("%02d", activeGlances.size),
                        label = "GLANCES",
                        accentColor = ElectricLime,
                        onClick = onNavigateToGlances,
                        size = 88.dp
                    )

                    CircularDataWidget(
                        count = String.format("%02d", dueTodayCount),
                        label = "DUE",
                        accentColor = ElectricCyan,
                        onClick = onNavigateToGlances,
                        size = 88.dp
                    )

                    CircularDataWidget(
                        count = String.format("%02d", urgentCount),
                        label = "URGENT",
                        accentColor = UrgentRed,
                        onClick = { showActiveActionPoster = true },
                        size = 88.dp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // POSTER HERO AREA:
            // ENORMOUS CONDENSED TYPOGRAPHY WITH OVERLAPPING FLOATING ARTIFACT
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // GIANT BACKGROUND TYPOGRAPHY (HERO)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = "YOUR\nDAY\nAT A\nGLANCE",
                            fontSize = 68.sp,
                            lineHeight = 62.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0x1AFFFFFF),
                            letterSpacing = (-3.0).sp
                        )
                    }

                    // CENTRAL FLOATING PHYSICAL ARTIFACT (SLIGHT TILT & DEPTH OVERLAPPING THE TEXT)
                    if (featuredGlance != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp)
                        ) {
                            GlanceArtifact(
                                glance = featuredGlance,
                                rotationAngle = -2.2f,
                                isDarkTheme = true,
                                onTap = { viewModel.selectGlance(featuredGlance) },
                                onTaskToggle = { taskId -> viewModel.toggleTask(featuredGlance, taskId) },
                                onComplete = { viewModel.setCompleted(featuredGlance, true) },
                                onArchive = { viewModel.setArchived(featuredGlance, true) },
                                onRemind = { viewModel.scheduleReminder(featuredGlance) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // CRITICAL ACTION POSTER TRIGGER ("DO IT NOW" SCREEN 2 SHORTCUT)
            if (urgentCount > 0) {
                item {
                    Surface(
                        onClick = { showActiveActionPoster = true },
                        color = PosterRed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = PaperWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("DO IT NOW", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = PaperWhite, fontSize = 14.sp)
                                    Text("Urgent task requiring immediate action", color = Color(0xCCFFFFFF), fontSize = 11.sp)
                                }
                            }

                            Text("OPEN →", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = PaperWhite, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            // QUICK CAPTURE STRIP
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CAPTURE THE WORLD",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = SubtitleGray,
                        letterSpacing = 1.2.sp
                    )

                    Text(
                        text = "${activeGlances.size} ACTIVE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DarkMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PosterActionButton(
                        label = "SCAN",
                        icon = Icons.Default.CameraAlt,
                        accentColor = ElectricLime,
                        onClick = onNavigateToCapture,
                        modifier = Modifier.weight(1f)
                    )
                    PosterActionButton(
                        label = "VOICE",
                        icon = Icons.Default.KeyboardVoice,
                        accentColor = WarmOrange,
                        onClick = { showVoiceInputDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    PosterActionButton(
                        label = "TEXT",
                        icon = Icons.AutoMirrored.Filled.TextSnippet,
                        accentColor = ElectricCyan,
                        onClick = { showTextInputDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    PosterActionButton(
                        label = "DEMO",
                        icon = Icons.Default.AutoAwesome,
                        accentColor = AcidYellow,
                        onClick = { showDemoPresetsSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }

            // RECENT PHYSICAL ARTIFACTS STREAM
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PHYSICAL ARTIFACTS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = PaperWhite,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "VIEW REPOSITORY →",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ElectricLime,
                        modifier = Modifier.clickable { onNavigateToGlances() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            val remainingGlances = activeGlances.filter { it.id != featuredGlance?.id }
            if (remainingGlances.isEmpty()) {
                item {
                    Surface(
                        color = CharcoalDark,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("NO SECONDARY ARTIFACTS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = PaperWhite, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Point your camera at a whiteboard or note to generate more.", color = DarkMuted, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(remainingGlances.take(3), key = { "artifact_${it.id}" }) { glance ->
                    GlanceArtifact(
                        glance = glance,
                        rotationAngle = if (glance.id % 2L == 0L) 1.5f else -1.2f,
                        isDarkTheme = true,
                        onTap = { viewModel.selectGlance(glance) },
                        onTaskToggle = { taskId -> viewModel.toggleTask(glance, taskId) },
                        onComplete = { viewModel.setCompleted(glance, true) },
                        onArchive = { viewModel.setArchived(glance, true) },
                        onRemind = { viewModel.scheduleReminder(glance) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // FULL SCREEN ACTIVE ACTION POSTER (SCREEN 2 OVERLAY)
        AnimatedVisibility(
            visible = showActiveActionPoster,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ActiveActionScreen(
                viewModel = viewModel,
                onBack = { showActiveActionPoster = false }
            )
        }
    }

    // Quick Text Input Dialog
    if (showTextInputDialog) {
        var textToParse by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            containerColor = CharcoalDark,
            title = {
                Text("TEXT // RAW NOTE", color = PaperWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text("Type or paste unstructured text to synthesize into an artifact:", color = SubtitleGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = textToParse,
                        onValueChange = { textToParse = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = { Text("e.g. AI Assignment: Build CNN classifier using CIFAR-10. Submit Friday.", color = DarkMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = PaperWhite,
                            unfocusedTextColor = PaperWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textToParse.isNotBlank()) {
                            viewModel.processRawText(textToParse, SourceType.TEXT)
                            showTextInputDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = VoidBlack),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("SYNTHESIZE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("CANCEL", color = DarkMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        )
    }

    // Voice Capture Dialog
    if (showVoiceInputDialog) {
        VoiceCapturePosterDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceInputDialog = false }
        )
    }

    // Demo Presets Dialog
    if (showDemoPresetsSheet) {
        DemoPresetsPosterDialog(
            onSelect = { presetText ->
                viewModel.processRawText(presetText, SourceType.CAMERA)
                showDemoPresetsSheet = false
            },
            onDismiss = { showDemoPresetsSheet = false }
        )
    }
}

@Composable
private fun PosterActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = CharcoalDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = PaperWhite,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun VoiceCapturePosterDialog(
    viewModel: GlanceViewModel,
    onDismiss: () -> Unit
) {
    var voiceText by remember { mutableStateOf("Remind me to submit the AI assignment Friday.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = {
            Text("TELL ME WHAT TO DO", color = PaperWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 16.sp)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Minimal animated waveform bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(14.dp, 28.dp, 20.dp, 34.dp, 16.dp, 30.dp, 22.dp, 12.dp)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(h)
                                .clip(RoundedCornerShape(2.dp))
                                .background(WarmOrange)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Spoken acoustic command:", color = SubtitleGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = voiceText,
                    onValueChange = { voiceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmOrange,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = PaperWhite,
                        unfocusedTextColor = PaperWhite
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.processRawText(voiceText, SourceType.VOICE)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange, contentColor = VoidBlack),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("MORPH INTO ARTIFACT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = DarkMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    )
}

@Composable
private fun DemoPresetsPosterDialog(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(
        "CLASSROOM BLACKBOARD (SPEC EXAMPLE)" to """
            AI Assignment
            Submit: Friday
            Implement CNN classifier
            Dataset: CIFAR-10
            Bring printed report
        """.trimIndent(),
        "MACHINE LEARNING ASSIGNMENT" to """
            Machine Learning Assignment
            Build a CNN classifier using CIFAR-10.
            Submit Friday.
            Bring printed report.
        """.trimIndent(),
        "LAB VIVA & TIMETABLE" to """
            Deep Learning Lab Viva
            Tomorrow at 10 AM in Lab 3
            Review ResNet architecture
            Bring lab observation record
        """.trimIndent()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = {
            Text("CLASSROOM DEMO PRESETS", color = PaperWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { (name, text) ->
                    Surface(
                        onClick = { onSelect(text) },
                        color = SlateDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(name, fontWeight = FontWeight.Black, color = ElectricLime, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text.replace("\n", " • "), color = DarkMuted, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = DarkMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp) }
        }
    )
}
