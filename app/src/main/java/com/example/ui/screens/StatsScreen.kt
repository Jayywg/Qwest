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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.model.Achievement
import com.example.ui.components.ParchmentBox
import com.example.ui.components.PixelCharacterSprite
import com.example.ui.components.PixelCoin
import com.example.ui.components.PixelFire
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
fun StatsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val adventureRecord by viewModel.adventureRecord.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    val musicEnabled by viewModel.musicEnabled.collectAsState()
    val musicVolume by viewModel.musicVolume.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()

    var showExportDialog by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val currentSession by viewModel.currentSession.collectAsState()

    // Character customization state
    var editName by remember(profile.characterName) { mutableStateOf(profile.characterName) }
    var editGender by remember(profile.characterGender) { mutableStateOf(profile.characterGender) }
    var editHairStyle by remember(profile.hairStyle) { mutableStateOf(profile.hairStyle) }
    var editHairColor by remember(profile.hairColor) { mutableStateOf(profile.hairColor) }
    var editSkinTone by remember(profile.skinTone) { mutableStateOf(profile.skinTone) }
    var editOutfitColor by remember(profile.outfitColor) { mutableStateOf(profile.outfitColor) }
    var editAccessory by remember(profile.accessory) { mutableStateOf(profile.accessory) }

    var selectedSettingsTab by remember { mutableStateOf("Character") }

    Box(modifier = modifier.fillMaxSize().background(PixelBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "CHRONICLES & ACHIEVEMENTS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = PixelGoldGlow
                )
            }

            // Stat Summary Overview
            item {
                ParchmentBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PixelSurfaceElevated,
                    borderColor = PixelBorderHighlight,
                    contentPadding = 16.dp
                ) {
                    Column {
                        Text(
                            text = "ADVENTURER PROFILE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = PixelGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniBox(title = "CURRENT LEVEL", value = "LVL ${profile.level}", color = PixelGoldGlow)
                            StatMiniBox(title = "COMPLETED", value = "${profile.totalQuestsCompleted}", color = PixelEmerald)
                            StatMiniBox(title = "STREAK", value = "${profile.currentStreak}d", color = PixelRuby)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniBox(title = "LONGEST STREAK", value = "${profile.longestStreak}d", color = PixelRuby)
                            StatMiniBox(title = "TOTAL EXP", value = "${profile.totalExpEarned}", color = PixelMana)
                            StatMiniBox(title = "TOTAL GOLD", value = "${profile.totalGoldEarned}", color = PixelGold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniBox(title = "GOLD SPENT", value = "${profile.totalGoldSpent}", color = PixelTextMuted)
                            StatMiniBox(title = "POTIONS USED", value = "${profile.potionsUsed}", color = PixelEmerald)
                            StatMiniBox(title = "BEST DISTANCE", value = "${adventureRecord.bestDistance}m", color = PixelMana)
                        }
                    }
                }
            }

            // Section: Guild Achievements
            item {
                Text(
                    text = "GUILD ACHIEVEMENTS (${achievements.count { it.isUnlocked }}/${achievements.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = PixelTextParchment
                )
            }

            items(achievements, key = { it.id }) { ach ->
                AchievementRow(
                    achievement = ach,
                    onClaim = { viewModel.claimAchievement(ach.id) }
                )
            }

            // Section: Settings & Preferences (6 Categories)
            item {
                Text(
                    text = "SETTINGS & SYSTEM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = PixelTextParchment
                )
            }

            // Category Tab Strip
            item {
                val tabs = listOf("Character", "Account", "Audio", "Gameplay", "Notifications", "Data", "About")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tabs) { tab ->
                        val isSelected = selectedSettingsTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) PixelGold else PixelSurfaceElevated)
                                .border(1.dp, if (isSelected) PixelGoldGlow else PixelBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedSettingsTab = tab }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tab.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isSelected) Color(0xFF1B1400) else PixelTextParchment
                            )
                        }
                    }
                }
            }

            // Category Content
            item {
                ParchmentBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PixelSurface,
                    borderColor = PixelBorderHighlight
                ) {
                    when (selectedSettingsTab) {
                        "Account" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "ADVENTURER ACCOUNT",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PixelSurfaceElevated)
                                        .border(1.dp, PixelBorder, RoundedCornerShape(6.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Hero Name", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                            Text(profile.characterName, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = PixelGoldGlow)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Account Email", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                            Text(currentSession?.email?.ifEmpty { "adventurer@qwest.com" } ?: "adventurer@qwest.com", style = MaterialTheme.typography.labelMedium, color = PixelTextParchment)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Tavern Status", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                            Text("Active Adventurer", style = MaterialTheme.typography.labelMedium, color = PixelEmerald)
                                        }
                                    }
                                }

                                Text(
                                    text = "Your journey, items, and quests are safely recorded in your hero's journal. You can return to the Tavern at any time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelTextMuted,
                                    lineHeight = 18.sp
                                )

                                Button(
                                    onClick = { showSignOutConfirm = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelSurfaceElevated,
                                        contentColor = PixelRuby
                                    ),
                                    border = BorderStroke(1.dp, PixelRuby.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("sign_out_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("LEAVE THE TAVERN (SIGN OUT)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        "Character" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "HERO CUSTOMIZATION",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )

                                // Live Character Sprite Preview
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PixelSurfaceElevated)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    PixelCharacterSprite(
                                        skinTone = editSkinTone,
                                        hairStyle = editHairStyle,
                                        hairColor = editHairColor,
                                        outfitColor = editOutfitColor,
                                        accessory = editAccessory,
                                        size = 76.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = editName.ifEmpty { "Adventurer" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PixelGoldGlow
                                        )
                                        Text(
                                            text = "$editGender • $editHairStyle $editHairColor",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PixelTextMuted
                                        )
                                        Text(
                                            text = "Outfit: $editOutfitColor • Acc: $editAccessory",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PixelTextMuted
                                        )
                                    }
                                }

                                // Name Input
                                Column {
                                    Text("Adventurer Name", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PixelTextParchment,
                                            unfocusedTextColor = PixelTextParchment,
                                            focusedBorderColor = PixelGold,
                                            unfocusedBorderColor = PixelBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Gender / Class Presentation
                                Column {
                                    Text("Identity / Presentation", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val genders = listOf("Adventurer", "Knight", "Mage", "Rogue")
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        genders.forEach { g ->
                                            val sel = editGender == g
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (sel) PixelGold else PixelSurfaceElevated)
                                                    .clickable { editGender = g }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(g, style = MaterialTheme.typography.labelSmall, color = if (sel) Color.Black else PixelTextParchment)
                                            }
                                        }
                                    }
                                }

                                // Hair Style
                                Column {
                                    Text("Hair Style", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val styles = listOf("Messy", "Short", "Long", "Ponytail")
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        styles.forEach { s ->
                                            val sel = editHairStyle == s
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (sel) PixelGold else PixelSurfaceElevated)
                                                    .clickable { editHairStyle = s }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(s, style = MaterialTheme.typography.labelSmall, color = if (sel) Color.Black else PixelTextParchment)
                                            }
                                        }
                                    }
                                }

                                // Hair Color
                                Column {
                                    Text("Hair Color", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val colors = listOf("Chestnut", "Golden", "Obsidian", "Silver", "Crimson")
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        colors.forEach { c ->
                                            val sel = editHairColor == c
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (sel) PixelGold else PixelSurfaceElevated)
                                                    .clickable { editHairColor = c }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(c, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = if (sel) Color.Black else PixelTextParchment)
                                            }
                                        }
                                    }
                                }

                                // Skin Tone
                                Column {
                                    Text("Skin Tone", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val skins = listOf("Fair", "Warm", "Tan", "Olive", "Deep")
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        skins.forEach { st ->
                                            val sel = editSkinTone == st
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (sel) PixelGold else PixelSurfaceElevated)
                                                    .clickable { editSkinTone = st }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(st, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = if (sel) Color.Black else PixelTextParchment)
                                            }
                                        }
                                    }
                                }

                                // Outfit Color
                                Column {
                                    Text("Outfit Tunic", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val outfits = listOf("Navy", "Crimson", "Forest", "Royal", "Charcoal")
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        outfits.forEach { o ->
                                            val sel = editOutfitColor == o
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (sel) PixelGold else PixelSurfaceElevated)
                                                    .clickable { editOutfitColor = o }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(o, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = if (sel) Color.Black else PixelTextParchment)
                                            }
                                        }
                                    }
                                }

                                // Accessory
                                Column {
                                    Text("Accessory", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val accs = listOf("None", "Headband", "Glasses", "Eyepatch", "Feather")
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        accs.forEach { a ->
                                            val sel = editAccessory == a
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (sel) PixelGold else PixelSurfaceElevated)
                                                    .clickable { editAccessory = a }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(a, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = if (sel) Color.Black else PixelTextParchment)
                                            }
                                        }
                                    }
                                }

                                // Save Button
                                Button(
                                    onClick = {
                                        viewModel.updateCharacterCustomization(
                                            name = editName,
                                            gender = editGender,
                                            hairStyle = editHairStyle,
                                            hairColor = editHairColor,
                                            skinTone = editSkinTone,
                                            outfitColor = editOutfitColor,
                                            accessory = editAccessory
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PixelGold, contentColor = Color(0xFF1B1400)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("SAVE CHARACTER", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        "Audio" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "CHIPTUNE SOUND & MUSIC",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )

                                // Sound FX Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Chiptune Sound FX", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PixelTextParchment)
                                        Text("Synthetic retro 8-bit cues", style = MaterialTheme.typography.bodySmall, color = PixelTextMuted)
                                    }
                                    Switch(
                                        checked = soundEnabled,
                                        onCheckedChange = { viewModel.toggleSound() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PixelGold, checkedTrackColor = PixelSurfaceElevated)
                                    )
                                }

                                // Background Music Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Tavern Background Music", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PixelTextParchment)
                                        Text(if (isPlayingMusic) "Playing cozy tavern melody" else "Music paused", style = MaterialTheme.typography.bodySmall, color = if (isPlayingMusic) PixelEmerald else PixelTextMuted)
                                    }
                                    Switch(
                                        checked = musicEnabled,
                                        onCheckedChange = { viewModel.toggleMusic() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PixelGold, checkedTrackColor = PixelSurfaceElevated)
                                    )
                                }

                                // Play / Pause Button
                                Button(
                                    onClick = { viewModel.toggleMusicPlay() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PixelSurfaceElevated, contentColor = PixelGold),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (isPlayingMusic) "⏸️ PAUSE TAVERN MELODY" else "▶️ PLAY TAVERN MELODY", fontWeight = FontWeight.Bold)
                                }

                                // Music Volume Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Music Volume", style = MaterialTheme.typography.labelSmall, color = PixelTextParchment)
                                        Text("${(musicVolume * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = PixelGold)
                                    }
                                    Slider(
                                        value = musicVolume,
                                        onValueChange = { viewModel.setMusicVolume(it) },
                                        colors = SliderDefaults.colors(thumbColor = PixelGold, activeTrackColor = PixelGold, inactiveTrackColor = PixelSurfaceElevated)
                                    )
                                }
                            }
                        }

                        "Gameplay" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "GAMEPLAY MECHANICS",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )
                                Text(
                                    text = "• Habits and tasks yield Gold and EXP upon completion.\n• Leveling up restores full HP and unlocks milestones.\n• Equipping armor in Inventory increases your Max HP in Adventure Mode.\n• Active pets run by your side in the endless-runner and rest at the Tavern.\n• Quests can be filtered by daily routine or one-time quest objectives.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelTextParchment,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        "Notifications" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "DAILY REMINDERS",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Daily Quest Alerts", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PixelTextParchment)
                                        Text("Morning reminders to check quests", style = MaterialTheme.typography.bodySmall, color = PixelTextMuted)
                                    }
                                    Switch(
                                        checked = notificationsEnabled,
                                        onCheckedChange = { viewModel.toggleNotifications() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PixelGold, checkedTrackColor = PixelSurfaceElevated)
                                    )
                                }
                            }
                        }

                        "Data" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "DATA MANAGEMENT & BACKUP",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.exportData { json ->
                                                showExportDialog = json
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PixelSurfaceElevated, contentColor = PixelGold),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("BACKUP", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showImportDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = PixelSurfaceElevated, contentColor = PixelMana),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("RESTORE", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { showResetConfirm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PixelSurfaceElevated, contentColor = PixelRuby),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("RESET SAVE DATA", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showSignOutConfirm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PixelSurfaceElevated, contentColor = PixelRuby),
                                    border = BorderStroke(1.dp, PixelRuby.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("LEAVE THE TAVERN (SIGN OUT)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        "About" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "ABOUT QWEST",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )
                                Text(
                                    text = "Qwest v2.0 • Indie Pixel Habit RPG\n\nA quiet, nostalgic fantasy journey turning daily habits and real-life quests into heroic adventures.\n\nAll music and pixel art assets are originally crafted for Qwest.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelTextParchment,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Export Dialog
    if (showExportDialog != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = null },
            containerColor = PixelSurfaceElevated,
            title = {
                Text("SAVE DATA BACKUP", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PixelGoldGlow)
            },
            text = {
                Column {
                    Text("Copy this JSON snippet to safely back up your journey:", style = MaterialTheme.typography.bodySmall, color = PixelTextParchment)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = showExportDialog!!,
                        onValueChange = {},
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment,
                            focusedBorderColor = PixelGold,
                            unfocusedBorderColor = PixelBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGold, contentColor = Color(0xFF1B1400))
                ) {
                    Text("CLOSE", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = PixelSurfaceElevated,
            title = {
                Text("RESTORE BACKUP", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PixelMana)
            },
            text = {
                Column {
                    Text("Paste your backup JSON snippet below:", style = MaterialTheme.typography.bodySmall, color = PixelTextParchment)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PixelTextParchment,
                            unfocusedTextColor = PixelTextParchment,
                            focusedBorderColor = PixelMana,
                            unfocusedBorderColor = PixelBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importData(importText) {
                            showImportDialog = false
                            importText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelMana, contentColor = Color(0xFF002233)),
                    enabled = importText.isNotBlank()
                ) {
                    Text("RESTORE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("CANCEL", color = PixelTextMuted)
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = PixelSurfaceElevated,
            title = {
                Text("RESET ALL PROGRESS?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PixelRuby)
            },
            text = {
                Text("This will reset your quests, inventory, and character back to level 1. This action cannot be undone.", color = PixelTextParchment)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelRuby)
                ) {
                    Text("RESET DATA", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("CANCEL", color = PixelTextMuted)
                }
            }
        )
    }

    // 10. SIGN OUT: Leave the Tavern Confirmation Dialog
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            containerColor = PixelSurfaceElevated,
            title = {
                Text(
                    text = "Leave the Tavern?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PixelGoldGlow
                )
            },
            text = {
                Text(
                    text = "You can return anytime, Adventurer.\nYour quest progress, inventory, and gold will be safely preserved.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelTextParchment
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelRuby, contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("confirm_sign_out_button")
                ) {
                    Text("SIGN OUT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutConfirm = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PixelTextParchment),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("cancel_sign_out_button")
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun AchievementRow(
    achievement: Achievement,
    onClaim: () -> Unit
) {
    val progressRatio = (achievement.progress.toFloat() / achievement.maxProgress.toFloat()).coerceIn(0f, 1f)

    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (achievement.isUnlocked) PixelSurfaceElevated else PixelSurface,
        borderColor = if (achievement.isUnlocked && !achievement.isRewardClaimed) PixelGold else PixelBorderHighlight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceElevated)
                    .border(
                        1.dp,
                        if (achievement.isUnlocked) PixelGold else PixelBorder,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (achievement.isUnlocked) PixelTextParchment else PixelTextMuted
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (achievement.isUnlocked) PixelGold else PixelMana,
                        trackColor = PixelSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${achievement.progress}/${achievement.maxProgress}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = PixelTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Claim / Unlocked state
            if (achievement.isUnlocked && !achievement.isRewardClaimed) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color(0xFF1B1400)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("CLAIM", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else if (achievement.isRewardClaimed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("CLAIMED", style = MaterialTheme.typography.labelSmall, color = PixelEmerald)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelCoin(size = 12.dp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+${achievement.rewardGold}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = PixelTextDim
                    )
                }
            }
        }
    }
}

@Composable
fun StatMiniBox(
    title: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(PixelSurface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = color
        )
    }
}
