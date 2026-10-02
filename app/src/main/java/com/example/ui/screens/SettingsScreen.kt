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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.ParchmentBox
import com.example.ui.components.PixelCharacterSprite
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
fun SettingsScreen(
    viewModel: GameViewModel,
    onNavigateToTavern: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val musicEnabled by viewModel.musicEnabled.collectAsState()
    val musicVolume by viewModel.musicVolume.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()

    var selectedTab by remember { mutableStateOf("Account") }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    // Character Edit State
    var editName by remember(profile.characterName) { mutableStateOf(profile.characterName) }
    var editGender by remember(profile.characterGender) { mutableStateOf(profile.characterGender) }
    var editSkinTone by remember(profile.skinTone) { mutableStateOf(profile.skinTone) }
    var editHairStyle by remember(profile.hairStyle) { mutableStateOf(profile.hairStyle) }
    var editHairColor by remember(profile.hairColor) { mutableStateOf(profile.hairColor) }
    var editOutfitColor by remember(profile.outfitColor) { mutableStateOf(profile.outfitColor) }
    var editAccessory by remember(profile.accessory) { mutableStateOf(profile.accessory) }

    val skinTones = listOf("Fair", "Tan", "Dark", "Pale", "Olive")
    val hairStyles = listOf("Short", "Long", "Spiky", "Braids", "Bald")
    val hairColors = listOf("Brown", "Blonde", "Black", "Red", "Silver", "Blue")
    val outfitColors = listOf("Red", "Blue", "Green", "Purple", "Gold", "Dark")
    val accessories = listOf("None", "Headband", "Glasses", "Eyepatch")

    val tabs = listOf("Account", "Character", "Audio", "Gameplay", "About")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar with Back Button to Tavern
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateToTavern,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Tavern",
                            tint = PixelGoldGlow
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SETTINGS & SYSTEM",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = PixelGoldGlow
                    )
                }
            }

            // Tab Navigation Strip
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tabs) { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) PixelGold else PixelSurfaceElevated)
                                .border(1.dp, if (isSelected) PixelGoldGlow else PixelBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedTab = tab }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("settings_tab_${tab.lowercase()}")
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

            // Active Tab Content
            item {
                ParchmentBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PixelSurface,
                    borderColor = PixelBorderHighlight,
                    contentPadding = 16.dp
                ) {
                    when (selectedTab) {
                        "Account" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "ADVENTURER ACCOUNT",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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
                                            Text("Active Adventurer", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = PixelEmerald)
                                        }
                                    }
                                }

                                Text(
                                    text = "Your progress, equipment, pets, and streaks are safely saved. You can leave the Tavern and return at any time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelTextMuted,
                                    lineHeight = 18.sp
                                )

                                // Section 10: Sign Out Option
                                Button(
                                    onClick = { showSignOutConfirm = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelSurfaceElevated,
                                        contentColor = PixelRuby
                                    ),
                                    border = BorderStroke(1.dp, PixelRuby.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("settings_sign_out_button")
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("LEAVE THE TAVERN (SIGN OUT)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        "Character" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "HERO CUSTOMIZATION",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )

                                // Live Sprite Preview
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PixelSurfaceElevated)
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    PixelCharacterSprite(
                                        skinTone = editSkinTone,
                                        hairStyle = editHairStyle,
                                        hairColor = editHairColor,
                                        outfitColor = editOutfitColor,
                                        accessory = editAccessory,
                                        size = 80.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = editName.ifEmpty { "Adventurer" },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PixelGoldGlow
                                        )
                                        Text(
                                            text = "Level ${profile.level} ${profile.characterGender}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PixelTextMuted
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    label = { Text("Hero Name") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PixelGold,
                                        unfocusedBorderColor = PixelBorder,
                                        focusedTextColor = PixelTextParchment,
                                        unfocusedTextColor = PixelTextParchment
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text("Skin Tone", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(skinTones) { tone ->
                                        CustomChip(text = tone, isSelected = tone == editSkinTone) {
                                            editSkinTone = tone
                                        }
                                    }
                                }

                                Text("Hair Style", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(hairStyles) { style ->
                                        CustomChip(text = style, isSelected = style == editHairStyle) {
                                            editHairStyle = style
                                        }
                                    }
                                }

                                Text("Hair Color", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(hairColors) { color ->
                                        CustomChip(text = color, isSelected = color == editHairColor) {
                                            editHairColor = color
                                        }
                                    }
                                }

                                Text("Outfit Color", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(outfitColors) { color ->
                                        CustomChip(text = color, isSelected = color == editOutfitColor) {
                                            editOutfitColor = color
                                        }
                                    }
                                }

                                Text("Accessory", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(accessories) { acc ->
                                        CustomChip(text = acc, isSelected = acc == editAccessory) {
                                            editAccessory = acc
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateCharacterCustomization(
                                            name = editName.ifBlank { "Adventurer" },
                                            gender = editGender,
                                            hairStyle = editHairStyle,
                                            hairColor = editHairColor,
                                            skinTone = editSkinTone,
                                            outfitColor = editOutfitColor,
                                            accessory = editAccessory
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelGold,
                                        contentColor = Color(0xFF1B1400)
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("SAVE HERO APPEARANCE", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        "Audio" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "AUDIO & SOUND EFFECTS",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Chiptune Sound FX", style = MaterialTheme.typography.bodyMedium, color = PixelTextParchment)
                                        Text("Level up, coin, hit, and jump retro sound effects", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                    }
                                    Switch(
                                        checked = soundEnabled,
                                        onCheckedChange = { viewModel.toggleSound() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PixelGold,
                                            checkedTrackColor = PixelGold.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Tavern Hearth BGM", style = MaterialTheme.typography.bodyMedium, color = PixelTextParchment)
                                        Text("Ambient 8-bit medieval tavern lute melody", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                    }
                                    Switch(
                                        checked = musicEnabled,
                                        onCheckedChange = { viewModel.toggleMusic() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PixelEmerald,
                                            checkedTrackColor = PixelEmerald.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                if (musicEnabled) {
                                    Column {
                                        Text("Music Volume: ${(musicVolume * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                        Slider(
                                            value = musicVolume,
                                            onValueChange = { viewModel.setMusicVolume(it) },
                                            valueRange = 0.05f..0.5f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = PixelEmerald,
                                                activeTrackColor = PixelEmerald
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        "Gameplay" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "GAMEPLAY & NOTIFICATIONS",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Quest Reminders", style = MaterialTheme.typography.bodyMedium, color = PixelTextParchment)
                                        Text("Alerts when your scheduled quests are due", style = MaterialTheme.typography.labelSmall, color = PixelTextMuted)
                                    }
                                    Switch(
                                        checked = notificationsEnabled,
                                        onCheckedChange = { viewModel.toggleNotifications() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PixelMana,
                                            checkedTrackColor = PixelMana.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                Text(
                                    text = "Daily reset occurs at 00:00 midnight local time. Habits can be completed once daily for full EXP and Guild Gold.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelTextMuted,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        "About" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "ABOUT QWEST",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PixelGoldGlow
                                )
                                Text(
                                    text = "Qwest v1.2.0 • Fantasy Habit RPG",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PixelTextParchment
                                )
                                Text(
                                    text = "\"Your journey begins with one Qwest.\"\n\nBuilt with 16-bit passion to turn daily goals into legendary adventures. Keep your streak alive, level up your adventurer, collect pets, and conquer endless challenges!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelTextMuted,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Section 10: Sign Out Confirmation Dialog
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            containerColor = PixelSurfaceElevated,
            title = {
                Text(
                    text = "Leave the Tavern?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PixelRuby
                )
            },
            text = {
                Text(
                    text = "You can return anytime, Adventurer. Your journey and hero progress will be waiting safely for you.",
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelRuby,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_sign_out_button")
                ) {
                    Text("SIGN OUT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutConfirm = false },
                    modifier = Modifier.testTag("cancel_sign_out_button")
                ) {
                    Text("CANCEL", color = PixelTextMuted)
                }
            }
        )
    }
}

@Composable
private fun CustomChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) PixelGold else PixelSurface)
            .border(1.dp, if (isSelected) PixelGold else PixelBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Color(0xFF1B1400) else PixelTextParchment
        )
    }
}
