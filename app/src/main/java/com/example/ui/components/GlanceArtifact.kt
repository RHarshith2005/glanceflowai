package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.ArtifactBorder
import com.example.ui.theme.ArtifactDarkTicket
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.WarmOrange
import kotlin.math.roundToInt

/**
 * Physical Information Artifact
 * Designed as a tangible ticket / document fragment floating over typography.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlanceArtifact(
    glance: GlanceItem,
    onTap: () -> Unit,
    onTaskToggle: (String) -> Unit = {},
    onComplete: () -> Unit = {},
    onArchive: () -> Unit = {},
    onRemind: () -> Unit = {},
    rotationAngle: Float = -2.2f,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dragOffset"
    )

    val surfaceColor = if (isDarkTheme) ArtifactDarkTicket else Color(0xFFFBFBF8)
    val textPrimary = if (isDarkTheme) PaperWhite else Color(0xFF101318)
    val textMuted = if (isDarkTheme) DarkMuted else Color(0xFF6B7280)
    val borderColor = if (isDarkTheme) ArtifactBorder else Color(0xFFD6D1C4)

    val accentColor = when {
        glance.priority == GlancePriority.HIGH -> UrgentRed
        glance.category == GlanceCategory.ASSIGNMENT -> ElectricLime
        glance.category == GlanceCategory.EVENT || glance.category == GlanceCategory.MEETING -> ElectricCyan
        glance.category == GlanceCategory.TASK || glance.category == GlanceCategory.REMINDER -> AcidYellow
        else -> WarmOrange
    }

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .graphicsLayer {
                this.rotationZ = rotationAngle
                this.cameraDistance = 12f
            }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = Color(0x66000000),
                ambientColor = Color(0x33000000)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(surfaceColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 150f) {
                            onComplete()
                        } else if (offsetX < -150f) {
                            onArchive()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
            .combinedClickable(onClick = onTap, onLongClick = onTap)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Artifact Top Notch & Meta Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip / Tag
                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "${glance.category.label} // ${glance.sourceType.name}".uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // High Priority Badge or Serial Index
                Text(
                    text = if (glance.priority == GlancePriority.HIGH) "HIGH PRIORITY" else "ID #${glance.id.toString().takeLast(3)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (glance.priority == GlancePriority.HIGH) UrgentRed else textMuted,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Condensed Headline
            Text(
                text = glance.title.uppercase(),
                fontSize = 24.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Black,
                color = textPrimary,
                letterSpacing = (-0.5).sp,
                textDecoration = if (glance.completed) TextDecoration.LineThrough else TextDecoration.None
            )

            // Large Deadline Callout
            if (!glance.deadline.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DUE // ${glance.deadline.uppercase()}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Perforated Dashed Ticket Divider Line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Checklist Lines
            if (glance.tasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    glance.tasks.take(3).forEachIndexed { index, task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTaskToggle(task.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d", index + 1),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (task.isCompleted) accentColor else Color.Transparent)
                                    .border(1.dp, if (task.isCompleted) accentColor else borderColor, RoundedCornerShape(3.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(11.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = task.title.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) textMuted else textPrimary,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Action Strip: [REMIND] + [→ OPEN GLANCE]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onRemind,
                    color = if (isDarkTheme) Color(0x33000000) else Color(0x1A000000),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = textMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REMIND", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = textMuted)
                    }
                }

                Surface(
                    onClick = onTap,
                    color = accentColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "→ OPEN GLANCE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = VoidBlack,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
