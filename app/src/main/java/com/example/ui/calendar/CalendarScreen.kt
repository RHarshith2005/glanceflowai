package com.example.ui.calendar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularDataWidget
import com.example.ui.components.GlanceArtifact
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.PosterTurquoise
import com.example.ui.viewmodel.GlanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SCREEN 3 — UPCOMING (DIGITAL POSTER COMPOSITION)
 * Full-screen Turquoise / Mint field directly translating the reference poster concept.
 * Top circular widgets (14:30 TODAY, 03 UPCOMING, LOCAL AI),
 * Huge typography: UPCOMING / GLANCES sitting partially behind the floating task artifact.
 */
@Composable
fun CalendarScreen(
    viewModel: GlanceViewModel,
    modifier: Modifier = Modifier
) {
    val activeGlances by viewModel.activeGlances.collectAsState()
    val upcomingItems = activeGlances.filter { !it.deadline.isNullOrBlank() }
    val featuredUpcoming = upcomingItems.firstOrNull() ?: activeGlances.firstOrNull()

    val currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PosterTurquoise)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp)
        ) {
            // TOP STATUS BAR & CIRCULAR DATA INSTRUMENTS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularDataWidget(
                        count = currentTimeString,
                        label = "TODAY",
                        accentColor = PaperWhite,
                        size = 86.dp
                    )

                    CircularDataWidget(
                        count = String.format("%02d", upcomingItems.size),
                        label = "UPCOMING",
                        accentColor = PaperWhite,
                        size = 86.dp
                    )

                    CircularDataWidget(
                        count = "●",
                        label = "LOCAL AI",
                        accentColor = PaperWhite,
                        size = 86.dp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // HUGE BACKGROUND POSTER TYPOGRAPHY (UPCOMING GLANCES)
            // Sits partially behind the floating physical artifact
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = "UPCOMING\nGLANCES",
                            fontSize = 72.sp,
                            lineHeight = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0x33FFFFFF),
                            letterSpacing = (-3.5).sp
                        )
                    }

                    // PRIMARY FLOATING ARTIFACT (OVERLAPPING THE GIANT TEXT)
                    if (featuredUpcoming != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp)
                        ) {
                            GlanceArtifact(
                                glance = featuredUpcoming,
                                rotationAngle = 2.1f,
                                isDarkTheme = true,
                                onTap = { viewModel.selectGlance(featuredUpcoming) },
                                onTaskToggle = { taskId -> viewModel.toggleTask(featuredUpcoming, taskId) },
                                onComplete = { viewModel.setCompleted(featuredUpcoming, true) },
                                onArchive = { viewModel.setArchived(featuredUpcoming, true) },
                                onRemind = { viewModel.scheduleReminder(featuredUpcoming) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // CHRONOLOGICAL STREAM OF REMAINING ARTIFACTS
            val remaining = upcomingItems.filter { it.id != featuredUpcoming?.id }
            if (remaining.isNotEmpty()) {
                item {
                    Text(
                        text = "CHRONOLOGICAL QUEUE // MILESTONES",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = PaperWhite,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                items(remaining, key = { "upcoming_${it.id}" }) { item ->
                    GlanceArtifact(
                        glance = item,
                        rotationAngle = if (item.id % 2L == 0L) -1.8f else 1.2f,
                        isDarkTheme = true,
                        onTap = { viewModel.selectGlance(item) },
                        onTaskToggle = { taskId -> viewModel.toggleTask(item, taskId) },
                        onComplete = { viewModel.setCompleted(item, true) },
                        onArchive = { viewModel.setArchived(item, true) },
                        onRemind = { viewModel.scheduleReminder(item) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // BOTTOM TECHNICAL INDEX
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "/${String.format("%03d", upcomingItems.size)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xCCFFFFFF),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "SYNCHRONIZED // ON-DEVICE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0x99FFFFFF),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
