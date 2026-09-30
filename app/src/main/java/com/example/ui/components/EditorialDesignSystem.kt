package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlancePriority
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

/**
 * Editorial Circular Data Widget / Physical Information Module
 * Example:
 *      14
 *    GLANCES
 */
@Composable
fun CircularDataWidget(
    count: String,
    label: String,
    accentColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    size: Dp = 90.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(CharcoalDark)
            .border(1.5.dp, accentColor.copy(alpha = 0.6f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = PaperWhite,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Technical Monospace Status Indicator
 * e.g. "● LOCAL AI ACTIVE" / "OFFLINE MODE"
 */
@Composable
fun TechnicalStatusIndicator(
    text: String,
    isActive: Boolean = true,
    accentColor: Color = ElectricLime,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "indicatorPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = modifier,
        color = SlateDark.copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) accentColor.copy(alpha = dotAlpha) else DarkMuted)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text.uppercase(),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isActive) PaperWhite else LightMuted,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Editorial Category Badge
 */
@Composable
fun CategoryColorPill(
    category: GlanceCategory,
    modifier: Modifier = Modifier
) {
    val (accent, border) = when (category) {
        GlanceCategory.ASSIGNMENT -> ElectricLime to ElectricLime.copy(alpha = 0.5f)
        GlanceCategory.EVENT, GlanceCategory.MEETING -> ElectricCyan to ElectricCyan.copy(alpha = 0.5f)
        GlanceCategory.EXAM, GlanceCategory.IMPORTANT -> UrgentRed to UrgentRed.copy(alpha = 0.5f)
        GlanceCategory.REMINDER, GlanceCategory.TASK -> AcidYellow to AcidYellow.copy(alpha = 0.5f)
        GlanceCategory.SHOPPING, GlanceCategory.NOTE -> WarmOrange to WarmOrange.copy(alpha = 0.5f)
        GlanceCategory.GENERAL -> LightMuted to SlateBorder
    }

    Surface(
        modifier = modifier,
        color = SlateElevated,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, border)
    ) {
        Text(
            text = category.label.uppercase(),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = accent,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

/**
 * Editorial Priority Badge
 */
@Composable
fun EditorialPriorityBadge(
    priority: GlancePriority,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (priority) {
        GlancePriority.HIGH -> "HIGH PRIORITY" to UrgentRed
        GlancePriority.MEDIUM -> "MED PRIORITY" to AcidYellow
        GlancePriority.LOW -> "LOW PRIORITY" to ElectricCyan
    }

    Text(
        text = text,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        letterSpacing = 1.sp,
        modifier = modifier
    )
}

/**
 * Numbered Editorial Task Row
 * e.g. "01 BUILD CNN CLASSIFIER"
 */
@Composable
fun NumberedTaskRow(
    indexNumber: String,
    title: String,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Monospace index number (e.g. 01, 02)
        Text(
            text = indexNumber,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isCompleted) DarkMuted else LightMuted
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Checkbox box
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isCompleted) ElectricLime else SlateDark)
                .border(1.dp, if (isCompleted) ElectricLime else SlateBorder, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = VoidBlack,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCompleted) DarkMuted else PaperWhite,
            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            letterSpacing = 0.2.sp
        )
    }
}
