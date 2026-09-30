package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GlanceItem
import com.example.domain.model.TaskItem
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.LightMuted
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlanceDetailBottomSheet(
    glance: GlanceItem?,
    onDismiss: () -> Unit,
    onUpdate: (GlanceItem) -> Unit,
    onDelete: (Long) -> Unit,
    onRemind: (GlanceItem) -> Unit,
    onToggleTask: (String) -> Unit
) {
    if (glance == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember(glance) { mutableStateOf(glance.title) }
    var editedDeadline by remember(glance) { mutableStateOf(glance.deadline ?: "") }
    var newTaskText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalDark,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            // Technical Top Bar
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

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = PaperWhite)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // LARGE TITLE (STACKED EDITORIAL)
            if (isEditing) {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("TITLE", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = PaperWhite,
                        unfocusedTextColor = PaperWhite
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedDeadline,
                    onValueChange = { editedDeadline = it },
                    label = { Text("DEADLINE", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = PaperWhite,
                        unfocusedTextColor = PaperWhite
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onUpdate(glance.copy(title = editedTitle, deadline = editedDeadline.ifBlank { null }))
                        isEditing = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = VoidBlack),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = glance.title.uppercase(),
                        fontSize = 32.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = PaperWhite,
                        letterSpacing = (-1.0).sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { isEditing = true }) {
                        Text("EDIT", color = ElectricLime, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // METADATA GRID: DEADLINE | PRIORITY | SOURCE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EditorialMetaBlock(
                    label = "DEADLINE",
                    value = glance.deadline?.uppercase() ?: "NONE",
                    accentColor = ElectricLime
                )
                EditorialMetaBlock(
                    label = "PRIORITY",
                    value = glance.priority.label,
                    accentColor = if (glance.priority.name == "HIGH") UrgentRed else AcidYellow
                )
                EditorialMetaBlock(
                    label = "SOURCE",
                    value = glance.sourceType.name,
                    accentColor = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SlateBorder))
            Spacer(modifier = Modifier.height(20.dp))

            // TASKS SECTION
            Text(
                text = "TASKS",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = ElectricLime,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateDark)
                    .padding(14.dp)
            ) {
                if (glance.tasks.isEmpty()) {
                    Text("NO TASKS EXTRACTED", color = DarkMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                } else {
                    glance.tasks.forEachIndexed { index, task ->
                        NumberedTaskRow(
                            indexNumber = String.format("%02d", index + 1),
                            title = task.title,
                            isCompleted = task.isCompleted,
                            onToggle = { onToggleTask(task.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Inline add task
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("ADD ACTION...", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = PaperWhite,
                            unfocusedTextColor = PaperWhite
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                val updated = glance.tasks + TaskItem(title = newTaskText.trim())
                                onUpdate(glance.copy(tasks = updated))
                                newTaskText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = ElectricLime)
                    }
                }
            }

            // RAW CAPTURE PREVIEW
            if (glance.rawText.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "RAW CAPTURE // OPTICAL SOURCE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = LightMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = VoidBlack,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = glance.rawText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DarkMuted,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onRemind(glance) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmOrange),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REMIND", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = PaperWhite, fontSize = 11.sp)
                }

                Button(
                    onClick = { onUpdate(glance.copy(completed = !glance.completed)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (glance.completed) ElectricLime.copy(alpha = 0.2f) else SlateDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricLime),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (glance.completed) "DONE ✓" else "COMPLETE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = ElectricLime,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = {
                        onDelete(glance.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UrgentRed),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EditorialMetaBlock(
    label: String,
    value: String,
    accentColor: Color
) {
    Column {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = DarkMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = accentColor
        )
    }
}
