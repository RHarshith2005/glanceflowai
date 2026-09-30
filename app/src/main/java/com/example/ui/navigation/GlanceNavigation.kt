package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.calendar.CalendarScreen
import com.example.ui.capture.CaptureScreen
import com.example.ui.capture.ProcessingAnimationOverlay
import com.example.ui.components.GlanceDetailBottomSheet
import com.example.ui.glances.GlancesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.sync.SyncScreen
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.GlanceViewModel
import com.example.ui.viewmodel.ProcessingStep

object GlanceRoutes {
    const val HOME = "home"
    const val CAPTURE = "capture"
    const val UPCOMING = "upcoming"
    const val ARCHIVE = "archive"
    const val SYNC = "sync"
    const val SETTINGS = "settings"
}

@Composable
fun GlanceMainApp(
    viewModel: GlanceViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: GlanceRoutes.HOME

    val processingStep by viewModel.processingStep.collectAsState()
    val selectedGlance by viewModel.selectedGlance.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Jetpack Navigation NavHost Structure
        NavHost(
            navController = navController,
            startDestination = GlanceRoutes.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(GlanceRoutes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCapture = {
                        navController.navigate(GlanceRoutes.CAPTURE) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGlances = {
                        navController.navigate(GlanceRoutes.ARCHIVE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(GlanceRoutes.CAPTURE) {
                CaptureScreen(
                    viewModel = viewModel,
                    onClose = { navController.popBackStack() }
                )
            }

            composable(GlanceRoutes.UPCOMING) {
                CalendarScreen(viewModel = viewModel)
            }

            composable(GlanceRoutes.ARCHIVE) {
                GlancesScreen(viewModel = viewModel)
            }

            composable(GlanceRoutes.SYNC) {
                SyncScreen(viewModel = viewModel)
            }

            composable(GlanceRoutes.SETTINGS) {
                SettingsScreen(viewModel = viewModel)
            }
        }

        // Floating Integrated Minimal Navigation Capsule (Hidden on full-screen Capture)
        val showBottomNav = currentRoute != GlanceRoutes.CAPTURE
        AnimatedVisibility(
            visible = showBottomNav,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp, start = 16.dp, end = 16.dp)
        ) {
            FloatingNavCapsule(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == GlanceRoutes.CAPTURE) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }

        // Processing Animation Overlay
        if (processingStep !is ProcessingStep.Idle) {
            ProcessingAnimationOverlay(
                step = processingStep,
                onDismiss = {
                    viewModel.dismissProcessing()
                    if (currentRoute == GlanceRoutes.CAPTURE) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // Glance Detail Modal
        if (selectedGlance != null) {
            GlanceDetailBottomSheet(
                glance = selectedGlance,
                onDismiss = { viewModel.selectGlance(null) },
                onUpdate = { updated -> viewModel.updateGlance(updated) },
                onDelete = { id -> viewModel.deleteGlance(id) },
                onRemind = { item -> viewModel.scheduleReminder(item) },
                onToggleTask = { taskId ->
                    selectedGlance?.let { viewModel.toggleTask(it, taskId) }
                }
            )
        }
    }
}

@Composable
private fun FloatingNavCapsule(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CharcoalDark.copy(alpha = 0.95f),
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GLANCE (HOME)
            NavCapsuleItem(
                label = "GLANCE",
                selected = currentRoute == GlanceRoutes.HOME,
                onClick = { onNavigate(GlanceRoutes.HOME) }
            )

            // TIMELINE (UPCOMING)
            NavCapsuleItem(
                label = "TIMELINE",
                selected = currentRoute == GlanceRoutes.UPCOMING,
                onClick = { onNavigate(GlanceRoutes.UPCOMING) }
            )

            // CAPTURE (Primary Circular Action Module)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(VoidBlack)
                    .border(2.dp, ElectricLime, CircleShape)
                    .clickable { onNavigate(GlanceRoutes.CAPTURE) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElectricLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        tint = VoidBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ARCHIVE (GLANCES)
            NavCapsuleItem(
                label = "ARCHIVE",
                selected = currentRoute == GlanceRoutes.ARCHIVE,
                onClick = { onNavigate(GlanceRoutes.ARCHIVE) }
            )

            // SYNC
            NavCapsuleItem(
                label = "SYNC",
                selected = currentRoute == GlanceRoutes.SYNC,
                onClick = { onNavigate(GlanceRoutes.SYNC) }
            )

            // SETTINGS
            NavCapsuleItem(
                label = "SYS",
                selected = currentRoute == GlanceRoutes.SETTINGS,
                onClick = { onNavigate(GlanceRoutes.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavCapsuleItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            color = if (selected) ElectricLime else SubtitleGray,
            letterSpacing = 0.5.sp
        )
    }
}
