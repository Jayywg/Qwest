package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.components.EggHatchDialog
import com.example.ui.components.PixelCoin
import com.example.ui.components.QuestCompleteDialog
import com.example.ui.components.StreakMilestoneDialog
import com.example.ui.screens.AdventureScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.QuestFormDialog
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.auth.AuthRootScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PixelBackground
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelBorderHighlight
import com.example.ui.theme.PixelGold
import com.example.ui.theme.PixelGoldGlow
import com.example.ui.theme.PixelMana
import com.example.ui.theme.PixelRuby
import com.example.ui.theme.PixelSurface
import com.example.ui.theme.PixelSurfaceElevated
import com.example.ui.theme.PixelTextDim
import com.example.ui.theme.PixelTextMuted
import com.example.ui.theme.PixelTextParchment
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.GameViewModel

enum class ScreenDestination {
    TAVERN,
    QUESTS,
    ADVENTURE,
    SHOP,
    INVENTORY,
    STATS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val authUiState by viewModel.authUiState.collectAsState()
                when (authUiState) {
                    is AuthUiState.Loading -> {
                        AuthLoadingScreen()
                    }
                    is AuthUiState.SignedOut -> {
                        AuthRootScreen(viewModel = viewModel)
                    }
                    is AuthUiState.SignedIn -> {
                        MainAppScaffold(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBackground),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚔️ QWEST",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                color = PixelGoldGlow
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                color = PixelGold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Entering the Tavern...",
                style = MaterialTheme.typography.bodyMedium,
                color = PixelTextParchment
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf(ScreenDestination.TAVERN) }
    var showAddQuestDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val profile by viewModel.userProfile.collectAsState()
    val rewardEvent by viewModel.rewardEvent.collectAsState()
    val eggHatchResult by viewModel.eggHatchResult.collectAsState()
    val streakMilestone by viewModel.streakMilestone.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()

    // Handle transient snackbar messages
    LaunchedEffect(infoMessage) {
        infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInfoMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PixelBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚔️ QWEST",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = PixelGoldGlow
                        )
                    }
                },
                navigationIcon = {
                    if (currentScreen != ScreenDestination.TAVERN) {
                        IconButton(onClick = { currentScreen = ScreenDestination.TAVERN }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Tavern",
                                tint = PixelTextParchment
                            )
                        }
                    }
                },
                actions = {
                    // Gold Pill in TopBar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelSurfaceElevated)
                            .border(1.dp, PixelGold, RoundedCornerShape(4.dp))
                            .clickable { currentScreen = ScreenDestination.SHOP }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        PixelCoin(size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile.gold}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PixelGoldGlow
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Sound Toggle Icon
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Icon(
                            if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            tint = if (soundEnabled) PixelMana else PixelTextDim
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Settings Icon
                    IconButton(
                        onClick = { currentScreen = ScreenDestination.SETTINGS },
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentScreen == ScreenDestination.SETTINGS) PixelGold else PixelGoldGlow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PixelSurface,
                    titleContentColor = PixelGoldGlow
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PixelSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, PixelBorder)
            ) {
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.TAVERN,
                    onClick = { currentScreen = ScreenDestination.TAVERN },
                    icon = { Icon(Icons.Default.Nightlife, contentDescription = "Tavern") },
                    label = { Text("Tavern", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                    colors = navigationItemColors()
                )
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.QUESTS,
                    onClick = { currentScreen = ScreenDestination.QUESTS },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Quests") },
                    label = { Text("Quests", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                    colors = navigationItemColors()
                )
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.ADVENTURE,
                    onClick = { currentScreen = ScreenDestination.ADVENTURE },
                    icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Adventure") },
                    label = { Text("Adventure", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                    colors = navigationItemColors()
                )
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.SHOP,
                    onClick = { currentScreen = ScreenDestination.SHOP },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Shop") },
                    label = { Text("Shop", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                    colors = navigationItemColors()
                )
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.INVENTORY,
                    onClick = { currentScreen = ScreenDestination.INVENTORY },
                    icon = { Icon(Icons.Default.Backpack, contentDescription = "Inventory") },
                    label = { Text("Inventory", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                    colors = navigationItemColors()
                )
                NavigationBarItem(
                    selected = currentScreen == ScreenDestination.STATS,
                    onClick = { currentScreen = ScreenDestination.STATS },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                    label = { Text("Stats", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                    colors = navigationItemColors()
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                ScreenDestination.TAVERN -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToQuests = { currentScreen = ScreenDestination.QUESTS },
                    onNavigateToAdventure = { currentScreen = ScreenDestination.ADVENTURE },
                    onOpenAddQuest = { showAddQuestDialog = true },
                    onNavigateToSettings = { currentScreen = ScreenDestination.SETTINGS }
                )
                ScreenDestination.QUESTS -> QuestsScreen(
                    viewModel = viewModel
                )
                ScreenDestination.ADVENTURE -> AdventureScreen(
                    viewModel = viewModel,
                    onReturnToCamp = { currentScreen = ScreenDestination.TAVERN }
                )
                ScreenDestination.SHOP -> ShopScreen(
                    viewModel = viewModel,
                    onNavigateToInventory = { currentScreen = ScreenDestination.INVENTORY }
                )
                ScreenDestination.INVENTORY -> InventoryScreen(
                    viewModel = viewModel,
                    onNavigateToShop = { currentScreen = ScreenDestination.SHOP }
                )
                ScreenDestination.STATS -> StatsScreen(
                    viewModel = viewModel
                )
                ScreenDestination.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToTavern = { currentScreen = ScreenDestination.TAVERN }
                )
            }
        }
    }

    // Celebration Dialogs
    rewardEvent?.let { result ->
        QuestCompleteDialog(
            result = result,
            onDismiss = { viewModel.dismissRewardEvent() }
        )
    }

    eggHatchResult?.let { result ->
        EggHatchDialog(
            result = result,
            onDismiss = { viewModel.dismissEggHatch() }
        )
    }

    streakMilestone?.let { streakDays ->
        StreakMilestoneDialog(
            streakDays = streakDays,
            onDismiss = { viewModel.dismissStreakMilestone() }
        )
    }

    // Quick Add Quest Dialog (from Home FAB)
    if (showAddQuestDialog) {
        QuestFormDialog(
            title = "NEW QUEST",
            initialQuest = null,
            onDismiss = { showAddQuestDialog = false },
            onSave = { name, desc, cat, diff, rec, reminder ->
                viewModel.addQuest(name, desc, cat, diff, rec, reminder)
                showAddQuestDialog = false
            }
        )
    }
}

@Composable
fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PixelGoldGlow,
    selectedTextColor = PixelGoldGlow,
    indicatorColor = PixelSurfaceElevated,
    unselectedIconColor = PixelTextDim,
    unselectedTextColor = PixelTextDim
)
