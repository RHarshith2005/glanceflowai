package com.example.ui.glances

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlancePriority
import com.example.ui.components.GlanceArtifact
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.PosterCharcoal
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.GlanceViewModel

/**
 * SCREEN 8 — ALL GLANCES (DIGITAL POSTER REPOSITORY)
 * Large header: ALL GLANCES
 * Vertically stacked / overlapping physical Glance artifacts with depth and alternating tilt angles.
 */
@Composable
fun GlancesScreen(
    viewModel: GlanceViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val completedGlances by viewModel.completedGlances.collectAsState()
    val archivedGlances by viewModel.archivedGlances.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "ACTIVE (${searchResults.size})",
        "DONE. (${completedGlances.size})",
        "ARCHIVED (${archivedGlances.size})"
    )

    var selectedCategoryFilter by remember { mutableStateOf<GlanceCategory?>(null) }
    var selectedPriorityFilter by remember { mutableStateOf<GlancePriority?>(null) }

    val currentList = when (selectedTabIndex) {
        0 -> searchResults
        1 -> completedGlances
        else -> archivedGlances
    }.filter { item ->
        val matchesCat = selectedCategoryFilter == null || item.category == selectedCategoryFilter
        val matchesPrio = selectedPriorityFilter == null || item.priority == selectedPriorityFilter
        matchesCat && matchesPrio
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PosterCharcoal)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp)
        ) {
            // TOP POSTER HEADER: ALL / GLANCES
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Text(
                        text = "PHYSICAL REPOSITORY // ARCHIVE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricLime,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedTabIndex == 1) "DONE." else "ALL\nGLANCES",
                        fontSize = 58.sp,
                        lineHeight = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = PaperWhite,
                        letterSpacing = (-2.5).sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "SEARCH CNN, CIFAR-10, VIVA...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DarkMuted
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricLime) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = DarkMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = SlateBorder,
                        focusedContainerColor = CharcoalDark,
                        unfocusedContainerColor = CharcoalDark,
                        focusedTextColor = PaperWhite,
                        unfocusedTextColor = PaperWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category filters row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        ArchiveFilterPill(
                            label = "ALL",
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null }
                        )
                    }
                    items(GlanceCategory.values().size) { idx ->
                        val cat = GlanceCategory.values()[idx]
                        ArchiveFilterPill(
                            label = cat.label.uppercase(),
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: ACTIVE | DONE. | ARCHIVED
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = PosterCharcoal,
                    contentColor = ElectricLime,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = ElectricLime
                        )
                    },
                    divider = {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SlateBorder))
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Bold,
                                    color = if (selectedTabIndex == index) ElectricLime else SubtitleGray,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // STACKED PHYSICAL ARTIFACTS
            if (currentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "NO MATCHING ARTIFACTS" else "COLLECTION EMPTY",
                                fontFamily = FontFamily.Monospace,
                                color = SubtitleGray,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Point your camera at real-world text to build your collection.",
                                color = DarkMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(currentList, key = { _, it -> "art_${it.id}" }) { index, glance ->
                    // Alternate slight rotation angles to create realistic physical document stack
                    val tiltAngle = when (index % 4) {
                        0 -> -2.2f
                        1 -> 1.8f
                        2 -> -1.4f
                        else -> 2.4f
                    }

                    GlanceArtifact(
                        glance = glance,
                        rotationAngle = tiltAngle,
                        isDarkTheme = true,
                        onTap = { viewModel.selectGlance(glance) },
                        onTaskToggle = { taskId -> viewModel.toggleTask(glance, taskId) },
                        onComplete = { viewModel.setCompleted(glance, !glance.completed) },
                        onArchive = { viewModel.setArchived(glance, !glance.archived) },
                        onRemind = { viewModel.scheduleReminder(glance) },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) ElectricLime.copy(alpha = 0.2f) else SlateDark,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) ElectricLime else SlateBorder
        )
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            color = if (selected) ElectricLime else SubtitleGray,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
