package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Pet
import com.example.model.Quest
import com.example.model.QuestCompletion
import com.example.model.QuestDifficulty
import com.example.model.UserProfile
import com.example.ui.components.ParchmentBox
import com.example.ui.components.PixelCharacterSprite
import com.example.ui.components.PixelCoin
import com.example.ui.components.PixelFire
import com.example.ui.components.PixelPetIcon
import com.example.ui.components.PixelTavernFireplace
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
import com.example.viewmodel.CalendarDay
import com.example.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onNavigateToQuests: () -> Unit,
    onNavigateToAdventure: () -> Unit,
    onOpenAddQuest: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    val activePet by viewModel.activePet.collectAsState()
    val maxHp by viewModel.maxHp.collectAsState()
    val completionDates by viewModel.allCompletionDates.collectAsState()

    val inventory by viewModel.inventory.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val musicEnabled by viewModel.musicEnabled.collectAsState()

    val hasBoots = remember(inventory) { inventory.any { it.itemId == "equip_iron_boots" && it.isEquipped } }
    val hasCloak = remember(inventory) { inventory.any { it.itemId == "equip_traveler_cloak" && it.isEquipped } }
    val hasPendant = remember(inventory) { inventory.any { it.itemId == "equip_moonlit_pendant" && it.isEquipped } }

    val infiniteTransition = rememberInfiniteTransition(label = "TavernIdle")
    val characterBob: Float by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CharBob"
    )

    // Query completions for current selected date
    val completionsFlow = remember(selectedDate) {
        viewModel.getCompletionsForDate(selectedDate)
    }
    val completions by completionsFlow.collectAsState(initial = emptyList<QuestCompletion>())
    val completedQuestIds = remember(completions) { completions.map { it.questId }.toSet() }

    val calendarDays = remember { viewModel.getWeekDays() }

    Box(modifier = modifier.fillMaxSize().background(PixelBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tavern Central Dashboard (Responsive Hero & Status Widgets)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TavernDashboardCard(
                    profile = profile,
                    characterBob = characterBob,
                    hasBoots = hasBoots,
                    hasCloak = hasCloak,
                    hasPendant = hasPendant,
                    activePet = activePet,
                    isPlayingMusic = isPlayingMusic,
                    maxHp = maxHp,
                    onToggleMusic = { viewModel.toggleMusicPlay() },
                    onAdventureClick = onNavigateToAdventure
                )
            }

            // Calendar Day Selector
            item {
                CalendarStrip(
                    days = calendarDays,
                    selectedDate = selectedDate,
                    completionDates = completionDates.toSet(),
                    onSelectDate = { viewModel.selectDate(it) }
                )
            }

            // First Launch Onboarding Card
            if (profile.isFirstLaunch && activeQuests.isEmpty()) {
                item {
                    OnboardingWelcomeCard(onAddFirstQuest = onOpenAddQuest)
                }
            }

            // Section Header: Today's Quests
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerTitle = if (selectedDate == viewModel.todayDateStr) {
                        "TODAY'S QUESTS"
                    } else {
                        "QUESTS FOR $selectedDate"
                    }
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = PixelTextParchment
                    )

                    Text(
                        text = "${completions.size}/${activeQuests.size} Done",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (completions.size == activeQuests.size && activeQuests.isNotEmpty()) PixelEmerald else PixelTextMuted
                    )
                }
            }

            // Quests list for selected date
            if (activeQuests.isEmpty()) {
                item {
                    EmptyQuestsPlaceholder(onAddQuest = onOpenAddQuest)
                }
            } else {
                items(activeQuests, key = { it.id }) { quest ->
                    val isDone = completedQuestIds.contains(quest.id)
                    QuestItemCard(
                        quest = quest,
                        isCompleted = isDone,
                        onToggle = {
                            viewModel.toggleQuestCompletion(quest, isDone, selectedDate)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to create quest
        FloatingActionButton(
            onClick = onOpenAddQuest,
            containerColor = PixelGold,
            contentColor = Color(0xFF1B1400),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_quest_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Quest")
        }
    }
}

@Composable
fun CalendarStrip(
    days: List<CalendarDay>,
    selectedDate: String,
    completionDates: Set<String>,
    onSelectDate: (String) -> Unit
) {
    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                val isSelected = day.dateStr == selectedDate
                val hasActivity = completionDates.contains(day.dateStr)

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) PixelSurfaceElevated else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PixelGold else if (day.isToday) PixelMana else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectDate(day.dateStr) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeek,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) PixelGold else if (day.isToday) PixelMana else PixelTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = day.dayNumber,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isSelected) PixelGoldGlow else PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Activity Dot
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (hasActivity) PixelEmerald else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun TavernDashboardCard(
    profile: UserProfile,
    characterBob: Float,
    hasBoots: Boolean,
    hasCloak: Boolean,
    hasPendant: Boolean,
    activePet: Pet?,
    isPlayingMusic: Boolean,
    maxHp: Int,
    onToggleMusic: () -> Unit,
    onAdventureClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 600.dp

        ParchmentBox(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = PixelSurfaceElevated,
            borderColor = PixelBorderHighlight,
            contentPadding = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tavern Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(PixelSurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍺 THE HEARTHSTONE TAVERN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        ),
                        color = PixelGoldGlow
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelSurfaceElevated)
                            .border(
                                1.dp,
                                if (isPlayingMusic) PixelEmerald else PixelBorder,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onToggleMusic() }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPlayingMusic) "🎵 BGM: ON" else "🔇 BGM: OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isPlayingMusic) PixelEmerald else PixelTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isWide) {
                    // Wide / Desktop View: Side-by-Side Tavern Scene & Status Widgets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, PixelBorderHighlight, RoundedCornerShape(8.dp))
                        ) {
                            TavernHearthScene(
                                profile = profile,
                                characterBob = characterBob,
                                hasBoots = hasBoots,
                                hasCloak = hasCloak,
                                hasPendant = hasPendant,
                                activePet = activePet,
                                characterSpriteSize = 76.dp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PixelSurface)
                                .border(1.dp, PixelBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            TavernStatusWidgets(
                                characterName = profile.characterName,
                                characterGender = profile.characterGender,
                                level = profile.level,
                                exp = profile.exp,
                                expNeeded = profile.expForNextLevel(),
                                gold = profile.gold,
                                streak = profile.currentStreak,
                                maxHp = maxHp,
                                activePetName = activePet?.name,
                                activePetId = activePet?.id,
                                onAdventureClick = onAdventureClick
                            )
                        }
                    }
                } else {
                    // Mobile / Compact View: Stacked Central Character Stage with Integrated Status Widgets
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, PixelBorderHighlight, RoundedCornerShape(8.dp))
                        ) {
                            TavernHearthScene(
                                profile = profile,
                                characterBob = characterBob,
                                hasBoots = hasBoots,
                                hasCloak = hasCloak,
                                hasPendant = hasPendant,
                                activePet = activePet,
                                characterSpriteSize = 68.dp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PixelSurface)
                                .border(1.dp, PixelBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            TavernStatusWidgets(
                                characterName = profile.characterName,
                                characterGender = profile.characterGender,
                                level = profile.level,
                                exp = profile.exp,
                                expNeeded = profile.expForNextLevel(),
                                gold = profile.gold,
                                streak = profile.currentStreak,
                                maxHp = maxHp,
                                activePetName = activePet?.name,
                                activePetId = activePet?.id,
                                onAdventureClick = onAdventureClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TavernHearthScene(
    profile: UserProfile,
    characterBob: Float,
    hasBoots: Boolean,
    hasCloak: Boolean,
    hasPendant: Boolean,
    activePet: Pet?,
    characterSpriteSize: androidx.compose.ui.unit.Dp = 68.dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fantasy_tavern_banner),
            contentDescription = "The Cozy Tavern Hearth",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Ambient warm hearth tint overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x44150B08))
        )

        // Interactive Stage inside Tavern: Fireplace on left, Character & Pet on right
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Animated Pixel Fireplace in hearth
            PixelTavernFireplace(size = 56.dp)

            // Adventurer Sprite & companion pet resting at tavern
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active companion pet resting beside player
                if (activePet != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        PixelPetIcon(petId = activePet.id, size = 30.dp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activePet.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PixelEmerald
                        )
                    }
                }

                // Custom Adventurer Sprite with equipped gear
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PixelCharacterSprite(
                        skinTone = profile.skinTone,
                        hairStyle = profile.hairStyle,
                        hairColor = profile.hairColor,
                        outfitColor = profile.outfitColor,
                        accessory = profile.accessory,
                        hasBoots = hasBoots,
                        hasCloak = hasCloak,
                        hasPendant = hasPendant,
                        size = characterSpriteSize,
                        idleBob = characterBob
                    )
                    Text(
                        text = profile.characterName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = PixelGoldGlow
                    )
                }
            }
        }
    }
}

@Composable
fun TavernStatusWidgets(
    characterName: String,
    characterGender: String,
    level: Int,
    exp: Int,
    expNeeded: Int,
    gold: Int,
    streak: Int,
    maxHp: Int,
    activePetName: String?,
    activePetId: String?,
    onAdventureClick: () -> Unit
) {
    val progress = (exp.toFloat() / expNeeded.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Level, Class & Adventure Shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelGold)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "LVL $level",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF1B1400)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = characterName.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        ),
                        color = PixelTextParchment
                    )
                    Text(
                        text = characterGender.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = PixelGold
                    )
                }
            }

            // Health & Adventure Quick Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceElevated)
                    .border(1.dp, PixelRuby, RoundedCornerShape(6.dp))
                    .clickable { onAdventureClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❤️ $maxHp HP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PixelRuby
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Adventure",
                    tint = PixelRuby,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // EXP Bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "EXP PROGRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    ),
                    color = PixelMana
                )
                Text(
                    text = "$exp / $expNeeded EXP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    ),
                    color = PixelTextMuted
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, PixelBorder, RoundedCornerShape(4.dp)),
                color = PixelMana,
                trackColor = PixelBackground
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Streak, Gold & Companion Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                PixelFire(size = 16.dp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "$streak STREAK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = PixelTextParchment
                )
            }

            // Gold
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                PixelCoin(size = 16.dp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "$gold GOLD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = PixelGoldGlow
                )
            }

            // Companion Indicator if any
            if (activePetId != null && activePetName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PixelSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    PixelPetIcon(petId = activePetId, size = 16.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activePetName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = PixelEmerald
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerStatusCard(
    characterName: String = "Adventurer",
    characterGender: String = "Adventurer",
    level: Int,
    exp: Int,
    expNeeded: Int,
    gold: Int,
    streak: Int,
    maxHp: Int,
    activePetName: String?,
    activePetId: String?,
    onAdventureClick: () -> Unit
) {
    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PixelSurfaceElevated,
        borderColor = PixelBorderHighlight,
        contentPadding = 14.dp
    ) {
        TavernStatusWidgets(
            characterName = characterName,
            characterGender = characterGender,
            level = level,
            exp = exp,
            expNeeded = expNeeded,
            gold = gold,
            streak = streak,
            maxHp = maxHp,
            activePetName = activePetName,
            activePetId = activePetId,
            onAdventureClick = onAdventureClick
        )
    }
}

@Composable
fun QuestItemCard(
    quest: Quest,
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    val diff = quest.getDifficultyEnum()
    val diffColor = when (diff) {
        QuestDifficulty.EASY -> PixelEmerald
        QuestDifficulty.NORMAL -> PixelMana
        QuestDifficulty.HARD -> PixelGold
        QuestDifficulty.EPIC -> PixelRuby
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) PixelSurface.copy(alpha = 0.6f) else PixelSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isCompleted) PixelBorder else PixelBorderHighlight
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("quest_card_${quest.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Pixel Box
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCompleted) PixelEmerald else PixelSurfaceElevated)
                    .border(
                        1.5.dp,
                        if (isCompleted) PixelEmerald else PixelBorderHighlight,
                        RoundedCornerShape(4.dp)
                    ),
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

            Spacer(modifier = Modifier.width(14.dp))

            // Quest details
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
                        color = PixelTextMuted,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelSurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = quest.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Difficulty Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(diffColor.copy(alpha = 0.15f))
                            .border(1.dp, diffColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${diff.displayName} (+${diff.expReward} EXP)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = diffColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingWelcomeCard(
    onAddFirstQuest: () -> Unit
) {
    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PixelSurfaceElevated,
        borderColor = PixelGold,
        highlightBorder = true,
        contentPadding = 16.dp
    ) {
        Column {
            Text(
                text = "📜 WELCOME, NOBLE ADVENTURER!",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = PixelGoldGlow
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Turn your real-world habits into epic quests. Completing tasks earns you EXP to level up and Gold to buy potions, pet eggs, and equipment for Adventure Mode!",
                style = MaterialTheme.typography.bodySmall,
                color = PixelTextParchment
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddFirstQuest,
                colors = ButtonDefaults.buttonColors(containerColor = PixelGold, contentColor = Color(0xFF1B1400)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CREATE FIRST QUEST", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyQuestsPlaceholder(
    onAddQuest: () -> Unit
) {
    ParchmentBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚔️",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your adventure begins here.",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = PixelTextParchment
            )
            Text(
                text = "Create your first quest to earn EXP and Gold.",
                style = MaterialTheme.typography.bodySmall,
                color = PixelTextMuted
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddQuest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelGold,
                    contentColor = Color(0xFF1B1400)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("+ CREATE QUEST", fontWeight = FontWeight.Bold)
            }
        }
    }
}
