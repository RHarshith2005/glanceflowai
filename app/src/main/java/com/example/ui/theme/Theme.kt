package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GlanceEditorialColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = VoidBlack,
    primaryContainer = SlateElevated,
    onPrimaryContainer = ElectricLime,
    secondary = ElectricCyan,
    onSecondary = VoidBlack,
    secondaryContainer = SlateElevated,
    onSecondaryContainer = ElectricCyan,
    tertiary = WarmOrange,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = PaperWhite,
    surface = CharcoalDark,
    onSurface = PaperWhite,
    surfaceVariant = SlateDark,
    onSurfaceVariant = SubtitleGray,
    outline = SlateBorder,
    outlineVariant = SlateBorderBright,
    error = UrgentRed,
    onError = VoidBlack
)

@Composable
fun GlanceFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlanceEditorialColorScheme,
        typography = Typography,
        content = content
    )
}
