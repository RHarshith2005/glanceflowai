package com.example.ui.capture

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import com.example.domain.model.TaskItem
import com.example.ui.components.GlanceArtifact
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.PosterYellow
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.ProcessingStep

/**
 * SCREEN 5 & 6 — AI PROCESSING & RESULT (DIGITAL POSTER)
 * Dominant Acid Yellow color field with kinetic typography:
 * CAPTURED → READING YOUR WORLD → UNDERSTANDING → STRUCTURING → I FOUND WHAT MATTERS
 * Physical information artifact emerges directly from the transformation.
 */
@Composable
fun ProcessingAnimationOverlay(
    step: ProcessingStep,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (step is ProcessingStep.Idle) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulseLaser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    // Synthesized sample artifact for visual result emergence
    val previewArtifact = GlanceItem(
        id = 999L,
        title = "Machine Learning Assignment",
        category = GlanceCategory.ASSIGNMENT,
        priority = GlancePriority.HIGH,
        deadline = "Friday 23:59",
        tasks = listOf(
            TaskItem(title = "Implement CNN classifier"),
            TaskItem(title = "Train with CIFAR-10"),
            TaskItem(title = "Print lab report")
        ),
        rawText = "Machine Learning Assignment. Build CNN classifier using CIFAR-10. Submit Friday.",
        sourceType = SourceType.CAMERA
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PosterYellow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP TECHNICAL BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI KINETIC SYNTHESIS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = VoidBlack,
                    letterSpacing = 1.2.sp
                )

                Surface(
                    color = VoidBlack,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "STAGE 0${step.index.coerceAtLeast(1)} / 05",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PosterYellow,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // CENTER KINETIC TYPOGRAPHY OR RESULT ARTIFACT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (step is ProcessingStep.Ready) {
                    // SCREEN 6: RESULT - "I FOUND WHAT MATTERS" WITH EMERGING ARTIFACT
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "I FOUND\nWHAT\nMATTERS",
                            fontSize = 58.sp,
                            lineHeight = 52.sp,
                            fontWeight = FontWeight.Black,
                            color = VoidBlack,
                            letterSpacing = (-2.5).sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        GlanceArtifact(
                            glance = previewArtifact,
                            rotationAngle = -2.0f,
                            isDarkTheme = true,
                            onTap = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // SCREEN 5: STAGE-BY-STAGE KINETIC TYPOGRAPHY
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(180)) },
                        label = "kineticTypo"
                    ) { targetStep ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (targetStep) {
                                is ProcessingStep.Scanning -> {
                                    Text(
                                        text = "READING\nYOUR\nWORLD",
                                        fontSize = 64.sp,
                                        lineHeight = 58.sp,
                                        fontWeight = FontWeight.Black,
                                        color = VoidBlack,
                                        letterSpacing = (-2.5).sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                is ProcessingStep.Understanding -> {
                                    Text(
                                        text = "UNDER\nSTANDING",
                                        fontSize = 68.sp,
                                        lineHeight = 60.sp,
                                        fontWeight = FontWeight.Black,
                                        color = VoidBlack,
                                        letterSpacing = (-2.5).sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                is ProcessingStep.Structuring -> {
                                    Text(
                                        text = "STRUC\nTURING",
                                        fontSize = 68.sp,
                                        lineHeight = 60.sp,
                                        fontWeight = FontWeight.Black,
                                        color = VoidBlack,
                                        letterSpacing = (-2.5).sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                is ProcessingStep.Failure -> {
                                    Text(
                                        text = "PARSE\nERROR",
                                        fontSize = 64.sp,
                                        lineHeight = 58.sp,
                                        fontWeight = FontWeight.Black,
                                        color = UrgentRed,
                                        letterSpacing = (-2.5).sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "CAPTURED",
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Black,
                                        color = VoidBlack,
                                        letterSpacing = (-2.0).sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Scanner Reticle on Acid Yellow
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VoidBlack)
                                    .border(2.dp, VoidBlack, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val y = size.height * laserY
                                    drawLine(
                                        color = PosterYellow,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }
                                Text("NLP // OCR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = PosterYellow, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // BOTTOM CONTROL / PROGRESS INDICATOR
            if (step is ProcessingStep.Ready || step is ProcessingStep.Failure) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoidBlack,
                        contentColor = PosterYellow
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        text = if (step is ProcessingStep.Ready) "OPEN NEW GLANCE ARTIFACT →" else "DISMISS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● POWERED BY GEMMA 2B (ON-DEVICE)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = VoidBlack,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "NO CLOUD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoidBlack
                    )
                }
            }
        }
    }
}
