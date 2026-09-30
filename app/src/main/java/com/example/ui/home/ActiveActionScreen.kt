package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority
import com.example.ui.components.GlanceArtifact
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.PosterRed
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.GlanceViewModel

/**
 * SCREEN 2 — ACTIVE GLANCE / NEXT ACTION
 * Full-screen dominant deep red color field inspired by the reference poster composition.
 * Huge typography:
 * DO
 * IT
 * NOW
 * Central floating physical artifact overlapping typography.
 */
@Composable
fun ActiveActionScreen(
    viewModel: GlanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeGlances by viewModel.activeGlances.collectAsState()
    val highPriorityGlance = activeGlances.firstOrNull { it.priority == GlancePriority.HIGH }
        ?: activeGlances.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PosterRed)
    ) {
        // Top Technical Navigation & Metas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PaperWhite
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PaperWhite)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CRITICAL ACTION // NOW",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = PaperWhite,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // HUGE BACKGROUND POSTER TYPOGRAPHY (50-70% SCREEN OCCUPANCY)
        // DO
        // IT
        // NOW
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 80.dp)
        ) {
            Text(
                text = "DO\nIT\nNOW",
                fontSize = 96.sp,
                lineHeight = 84.sp,
                fontWeight = FontWeight.Black,
                color = Color(0x33FFFFFF),
                letterSpacing = (-4.0).sp
            )
        }

        // FLOATING CENTRAL PHYSICAL ARTIFACT (OVERLAPPING THE GIANT TYPOGRAPHY)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (highPriorityGlance != null) {
                GlanceArtifact(
                    glance = highPriorityGlance,
                    rotationAngle = 2.4f,
                    isDarkTheme = true,
                    onTap = { viewModel.selectGlance(highPriorityGlance) },
                    onTaskToggle = { taskId -> viewModel.toggleTask(highPriorityGlance, taskId) },
                    onComplete = { viewModel.setCompleted(highPriorityGlance, true) },
                    onArchive = { viewModel.setArchived(highPriorityGlance, true) },
                    onRemind = { viewModel.scheduleReminder(highPriorityGlance) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Metadata & Done Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${highPriorityGlance.category.label} // ${highPriorityGlance.priority.label}".uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = PaperWhite,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${highPriorityGlance.tasks.size} ACTIONS REMAINING",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color(0xCCFFFFFF)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.setCompleted(highPriorityGlance, true)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PaperWhite,
                            contentColor = VoidBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESOLVE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Surface(
                    color = Color(0x22FFFFFF),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ALL CLEAR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = PaperWhite, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No critical deadlines pending.", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
