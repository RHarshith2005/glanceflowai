package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.LightMuted
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderBright
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GlanceCard(
    glance: GlanceItem,
    onTap: () -> Unit,
    onLongPress: () -> Unit = onTap,
    onTaskToggle: (String) -> Unit = {},
    onCompleteToggle: (Boolean) -> Unit = {},
    onArchiveToggle: (Boolean) -> Unit = {},
    onRemindClick: () -> Unit = {},
    isHeroFeatured: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Dominant accent per category / priority
    val dominantAccent = when {
        glance.priority == GlancePriority.HIGH -> UrgentRed
        glance.category == GlanceCategory.ASSIGNMENT -> ElectricLime
        glance.category == GlanceCategory.EVENT || glance.category == GlanceCategory.MEETING -> ElectricCyan
        glance.category == GlanceCategory.EXAM -> UrgentRed
        glance.category == GlanceCategory.TASK || glance.category == GlanceCategory.REMINDER -> AcidYellow
        else -> WarmOrange
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onCompleteToggle(!glance.completed)
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onArchiveToggle(!glance.archived)
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> ElectricLime.copy(alpha = 0.35f)
                    SwipeToDismissBoxValue.EndToStart -> UrgentRed.copy(alpha = 0.35f)
                    else -> SlateDark
                },
                label = "swipeBg"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPLETE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElectricLime, fontSize = 12.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ARCHIVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = UrgentRed, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Archive, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onTap, onLongClick = onLongPress),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (glance.completed) SlateDark.copy(alpha = 0.6f) else CharcoalDark
            ),
            border = BorderStroke(1.5.dp, if (isHeroFeatured) dominantAccent else SlateBorder)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Asymmetric background watermark lettering for editorial feel
                Text(
                    text = "GLANCE // ${glance.category.name.take(3)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    color = Color(0x08FFFFFF),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 12.dp)
                )

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Top Technical Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryColorPill(category = glance.category)
                            Spacer(modifier = Modifier.width(8.dp))
                            EditorialPriorityBadge(priority = glance.priority)
                        }

                        // Source Icon + Pin
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (sourceLabel, sourceIcon) = when (glance.sourceType) {
                                SourceType.CAMERA -> "VISION" to Icons.Default.CameraAlt
                                SourceType.VOICE -> "VOICE" to Icons.Default.KeyboardVoice
                                SourceType.TEXT -> "TEXT" to Icons.AutoMirrored.Filled.TextSnippet
                                SourceType.IMPORT -> "SYNC" to Icons.AutoMirrored.Filled.ArrowForward
                            }
                            Text(
                                text = sourceLabel,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkMuted,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = sourceIcon,
                                contentDescription = null,
                                tint = DarkMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Main Editorial Headline (Stacked uppercase bold typography)
                    Text(
                        text = glance.title.uppercase(),
                        fontSize = if (isHeroFeatured) 26.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (glance.completed) DarkMuted else PaperWhite,
                        lineHeight = if (isHeroFeatured) 28.sp else 24.sp,
                        letterSpacing = (-0.5).sp,
                        textDecoration = if (glance.completed) TextDecoration.LineThrough else TextDecoration.None
                    )

                    // Big Date / Deadline callout
                    if (!glance.deadline.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dominantAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DUE // ${glance.deadline.uppercase()}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = dominantAccent,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Rule Divider
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SlateBorder)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Numbered Tasks Checklist
                    if (glance.tasks.isNotEmpty()) {
                        Column {
                            glance.tasks.take(3).forEachIndexed { index, task ->
                                NumberedTaskRow(
                                    indexNumber = String.format("%02d", index + 1),
                                    title = task.title,
                                    isCompleted = task.isCompleted,
                                    onToggle = { onTaskToggle(task.id) }
                                )
                            }
                            if (glance.tasks.size > 3) {
                                Text(
                                    text = "+${glance.tasks.size - 3} MORE ACTIONS",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkMuted,
                                    modifier = Modifier.padding(top = 4.dp, start = 36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Bottom Action Strip: [→ OPEN GLANCE] + [REMIND]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onRemindClick,
                            color = SlateDark,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SlateBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = "Remind",
                                    tint = SubtitleGray,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "REMIND",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = SubtitleGray,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        // Editorial Open Button
                        Surface(
                            onClick = onTap,
                            color = dominantAccent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "→ OPEN GLANCE",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = VoidBlack,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
