package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quest
import com.example.model.QuestCompletion
import com.example.model.QuestDifficulty
import com.example.model.QuestRecurrence
import com.example.ui.components.ParchmentBox
import com.example.ui.theme.PixelBackground
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelBorderHighlight
import com.example.ui.theme.PixelEmerald
import com.example.ui.theme.PixelGold
import com.example.ui.theme.PixelGoldGlow
import com.example.ui.theme.PixelMana
import com.example.ui.theme.PixelRuby
import com.example.ui.theme.PixelSurface
import com.example.ui.theme.PixelSurfaceElevated
import com.example.ui.theme.PixelTextDim
import com.example.ui.theme.PixelTextMuted
import com.example.ui.theme.PixelTextParchment
import com.example.viewmodel.GameViewModel

@Composable
fun QuestsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Active, 1: Archived
    var selectedCategory by remember { mutableStateOf("All") }
    var questToEdit by remember { mutableStateOf<Quest?>(null) }
    var questToDelete by remember { mutableStateOf<Quest?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val activeQuests by viewModel.activeQuests.collectAsState()
    val archivedQuests by viewModel.archivedQuests.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val completionsFlow = remember(selectedDate) {
        viewModel.getCompletionsForDate(selectedDate)
    }
    val completions by completionsFlow.collectAsState(initial = emptyList<QuestCompletion>())
    val completedQuestIds = remember(completions) { completions.map { it.questId }.toSet() }

    val categories = listOf("All", "Study", "Fitness", "Work", "Wellness", "Creativity", "Chores")

    val currentList = if (selectedTabIndex == 0) activeQuests else archivedQuests
    val filteredList = currentList.filter {
        selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    Box(modifier = modifier.fillMaxSize().background(PixelBackground)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Title
            Text(
                text = "QUEST LOG",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = PixelGoldGlow
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Row: Active vs Archived
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PixelSurface,
                contentColor = PixelGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PixelGold
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, PixelBorder, RoundedCornerShape(6.dp))
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "ACTIVE (${activeQuests.size})",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "ARCHIVED (${archivedQuests.size})",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) PixelGold else PixelSurface)
                            .border(
                                1.dp,
                                if (isSelected) PixelGold else PixelBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color(0xFF1B1400) else PixelTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quests List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛡️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTabIndex == 0) "No active quests found." else "No archived quests.",
                            color = PixelTextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList, key = { it.id }) { quest ->
                        val isDone = completedQuestIds.contains(quest.id)
                        QuestDetailCard(
                            quest = quest,
                            isCompleted = isDone,
                            isArchivedTab = selectedTabIndex == 1,
                            onToggleCompletion = {
                                viewModel.toggleQuestCompletion(quest, isDone, selectedDate)
                            },
                            onEdit = { questToEdit = quest },
                            onDelete = { questToDelete = quest },
                            onDuplicate = { viewModel.duplicateQuest(quest) },
                            onToggleArchive = {
                                viewModel.archiveQuest(quest, selectedTabIndex == 0)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Quest FAB
        if (selectedTabIndex == 0) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PixelGold,
                contentColor = Color(0xFF1B1400),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("quests_screen_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Quest")
            }
        }
    }

    // Dialog: Create Quest
    if (showCreateDialog) {
        QuestFormDialog(
            title = "NEW QUEST",
            initialQuest = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, desc, cat, diff, rec, reminder ->
                viewModel.addQuest(name, desc, cat, diff, rec, reminder)
                showCreateDialog = false
            }
        )
    }

    // Dialog: Edit Quest
    if (questToEdit != null) {
        QuestFormDialog(
            title = "EDIT QUEST",
            initialQuest = questToEdit,
            onDismiss = { questToEdit = null },
            onSave = { name, desc, cat, diff, rec, reminder ->
                questToEdit?.let {
                    viewModel.updateQuest(
                        it.copy(
                            title = name,
                            description = desc,
                            category = cat,
                            difficulty = diff,
                            recurrence = rec,
                            reminderTime = reminder
                        )
                    )
                }
                questToEdit = null
            }
        )
    }

    // Dialog: Confirm Delete
    if (questToDelete != null) {
        AlertDialog(
            onDismissRequest = { questToDelete = null },
            containerColor = PixelSurfaceElevated,
            title = {
                Text(
                    "ABANDON QUEST?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PixelRuby
                )
            },
            text = {
                Text(
                    "Are you sure you wish to delete '${questToDelete?.title}'? All completion history for this quest will be removed.",
                    color = PixelTextParchment
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        questToDelete?.let { viewModel.deleteQuest(it) }
                        questToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelRuby)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { questToDelete = null }) {
                    Text("CANCEL", color = PixelTextMuted)
                }
            }
        )
    }
}

@Composable
fun QuestDetailCard(
    quest: Quest,
    isCompleted: Boolean,
    isArchivedTab: Boolean,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleArchive: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val diff = quest.getDifficultyEnum()
    val diffColor = when (diff) {
        QuestDifficulty.EASY -> PixelEmerald
        QuestDifficulty.NORMAL -> PixelMana
        QuestDifficulty.HARD -> PixelGold
        QuestDifficulty.EPIC -> PixelRuby
    }

    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isCompleted) PixelSurface.copy(alpha = 0.5f) else PixelSurface,
        borderColor = if (isCompleted) PixelBorder else PixelBorderHighlight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            if (!isArchivedTab) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCompleted) PixelEmerald else PixelSurfaceElevated)
                        .border(
                            1.5.dp,
                            if (isCompleted) PixelEmerald else PixelBorderHighlight,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onToggleCompletion() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) PixelTextDim else PixelTextParchment
                    )
                )

                if (quest.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = quest.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PixelTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelSurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            quest.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelTextMuted
                        )
                    }

                    // Difficulty
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(diffColor.copy(alpha = 0.15f))
                            .border(1.dp, diffColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "+${diff.expReward} EXP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = diffColor
                        )
                    }

                    // Recurrence
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelSurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            quest.recurrence,
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelMana
                        )
                    }

                    if (quest.reminderTime != null) {
                        Text(
                            "⏰ ${quest.reminderTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelGold
                        )
                    }
                }
            }

            // Menu button
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = PixelTextMuted
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(PixelSurfaceElevated)
                        .border(1.dp, PixelBorderHighlight)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = PixelTextParchment) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PixelMana) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = PixelTextParchment) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PixelGold) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isArchivedTab) "Unarchive" else "Archive",
                                color = PixelTextParchment
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (isArchivedTab) Icons.Default.Unarchive else Icons.Default.Archive,
                                contentDescription = null,
                                tint = PixelTextMuted
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleArchive()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = PixelRuby) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = PixelRuby) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestFormDialog(
    title: String,
    initialQuest: Quest?,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, cat: String, diff: String, rec: String, reminder: String?) -> Unit
) {
    var name by remember { mutableStateOf(initialQuest?.title ?: "") }
    var description by remember { mutableStateOf(initialQuest?.description ?: "") }
    var category by remember { mutableStateOf(initialQuest?.category ?: "Study") }
    var difficulty by remember { mutableStateOf(initialQuest?.difficulty ?: "NORMAL") }
    var recurrence by remember { mutableStateOf(initialQuest?.recurrence ?: "DAILY") }
    var reminderTime by remember { mutableStateOf(initialQuest?.reminderTime ?: "") }

    val categories = listOf("Study", "Fitness", "Work", "Wellness", "Creativity", "Chores", "General")
    val difficulties = listOf(
        QuestDifficulty.EASY,
        QuestDifficulty.NORMAL,
        QuestDifficulty.HARD,
        QuestDifficulty.EPIC
    )
    val recurrences = listOf("DAILY", "WEEKLY", "ONE_TIME")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PixelSurfaceElevated,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PixelGoldGlow
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Quest Title") },
                    placeholder = { Text("e.g. Study for 30 minutes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelGold,
                        unfocusedBorderColor = PixelBorder,
                        focusedTextColor = PixelTextParchment,
                        unfocusedTextColor = PixelTextParchment
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelGold,
                        unfocusedBorderColor = PixelBorder,
                        focusedTextColor = PixelTextParchment,
                        unfocusedTextColor = PixelTextParchment
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection
                Text("Category", style = MaterialTheme.typography.labelMedium, color = PixelTextMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val sel = cat == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (sel) PixelMana else PixelSurface)
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                cat,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (sel) Color(0xFF002233) else PixelTextParchment
                            )
                        }
                    }
                }

                // Difficulty selection
                Text("Difficulty & Rewards", style = MaterialTheme.typography.labelMedium, color = PixelTextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    difficulties.forEach { diffEnum ->
                        val sel = diffEnum.name == difficulty
                        val col = when (diffEnum) {
                            QuestDifficulty.EASY -> PixelEmerald
                            QuestDifficulty.NORMAL -> PixelMana
                            QuestDifficulty.HARD -> PixelGold
                            QuestDifficulty.EPIC -> PixelRuby
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (sel) col else PixelSurface)
                                .border(1.dp, col, RoundedCornerShape(4.dp))
                                .clickable { difficulty = diffEnum.name }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    diffEnum.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (sel) Color(0xFF1B1400) else col
                                )
                                Text(
                                    "+${diffEnum.expReward} EXP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) Color(0xFF1B1400) else PixelTextMuted
                                )
                            }
                        }
                    }
                }

                // Recurrence
                Text("Schedule", style = MaterialTheme.typography.labelMedium, color = PixelTextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recurrences.forEach { rec ->
                        val sel = rec == recurrence
                        val label = when (rec) {
                            "DAILY" -> "Daily"
                            "WEEKLY" -> "Weekly"
                            else -> "One-Time"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (sel) PixelGold else PixelSurface)
                                .clickable { recurrence = rec }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (sel) Color(0xFF1B1400) else PixelTextParchment
                            )
                        }
                    }
                }

                // Reminder time
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Reminder (e.g. 19:00)") },
                    placeholder = { Text("Optional time") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelGold,
                        unfocusedBorderColor = PixelBorder,
                        focusedTextColor = PixelTextParchment,
                        unfocusedTextColor = PixelTextParchment
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            description,
                            category,
                            difficulty,
                            recurrence,
                            reminderTime.ifBlank { null }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelGold,
                    contentColor = Color(0xFF1B1400)
                ),
                enabled = name.isNotBlank()
            ) {
                Text("SAVE QUEST", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = PixelTextMuted)
            }
        }
    )
}
