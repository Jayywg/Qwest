package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ChiptuneAudio
import com.example.model.GameItemCatalog
import com.example.ui.components.ParchmentBox
import com.example.ui.components.PixelCoin
import com.example.ui.components.PixelHeart
import com.example.ui.components.PixelPetIcon
import com.example.ui.components.PixelPotionIcon
import com.example.ui.theme.PixelBackground
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelBorderHighlight
import com.example.ui.theme.PixelEmerald
import com.example.ui.theme.PixelEmeraldGlow
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
import kotlin.math.sin
import kotlin.random.Random

data class Obstacle(
    var x: Float,
    val width: Float = 40f,
    val height: Float = 48f,
    val type: Int = 0 // 0: Bramble, 1: Ruin Stone
)

data class AdventureCoin(
    var x: Float,
    val y: Float,
    val radius: Float = 14f,
    var collected: Boolean = false
)

@Composable
fun AdventureScreen(
    viewModel: GameViewModel,
    onReturnToCamp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxHp by viewModel.maxHp.collectAsState()
    val activePet by viewModel.activePet.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val adventureRecord by viewModel.adventureRecord.collectAsState()

    val potionItem = inventory.find { it.itemId == GameItemCatalog.HEALING_POTION.id }
    val potionCount = potionItem?.quantity ?: 0

    // Game Loop State
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    var currentHp by remember { mutableIntStateOf(maxHp) }
    var distanceTraveled by remember { mutableFloatStateOf(0f) }
    var coinsCollectedInRun by remember { mutableIntStateOf(0) }

    // Player physics
    var playerY by remember { mutableFloatStateOf(0f) } // 0 is ground
    var playerVelocityY by remember { mutableFloatStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var invulnerableFrames by remember { mutableIntStateOf(0) }
    var animFrame by remember { mutableIntStateOf(0) }
    var tickCount by remember { mutableIntStateOf(0) }

    // Obstacles and Coins
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    val coins = remember { mutableStateListOf<AdventureCoin>() }
    var bgOffset by remember { mutableFloatStateOf(0f) }

    fun resetGame() {
        currentHp = maxHp
        distanceTraveled = 0f
        coinsCollectedInRun = 0
        playerY = 0f
        playerVelocityY = 0f
        isJumping = false
        invulnerableFrames = 0
        tickCount = 0
        obstacles.clear()
        coins.clear()
        bgOffset = 0f
        isGameOver = false
        isPlaying = true
    }

    fun jump() {
        if (!isJumping && isPlaying && !isGameOver) {
            playerVelocityY = 17f
            isJumping = true
            ChiptuneAudio.playJump()
        }
    }

    fun usePotion() {
        if (isPlaying && !isGameOver && currentHp < maxHp && potionCount > 0) {
            val used = viewModel.useHealingPotion()
            if (used) {
                currentHp = (currentHp + 1).coerceAtMost(maxHp)
            }
        }
    }

    // Main Game Update Tick
    LaunchedEffect(isPlaying, isGameOver) {
        if (!isPlaying || isGameOver) return@LaunchedEffect

        var lastSpawnDistance = 0f
        var lastCoinSpawnDistance = 0f

        while (isPlaying && !isGameOver) {
            withFrameMillis { _ ->
                tickCount++
                if (tickCount % 6 == 0) {
                    animFrame = (animFrame + 1) % 4
                }

                // Game speed increases slightly with distance
                val speed = 5.2f + (distanceTraveled / 1000f).coerceAtMost(4f)
                distanceTraveled += speed * 0.15f
                bgOffset = (bgOffset + speed * 0.5f) % 800f

                // Physics update
                if (isJumping || playerY > 0f) {
                    playerY += playerVelocityY
                    playerVelocityY -= 0.85f // gravity
                    if (playerY <= 0f) {
                        playerY = 0f
                        playerVelocityY = 0f
                        isJumping = false
                    }
                }

                if (invulnerableFrames > 0) {
                    invulnerableFrames--
                }

                // Spawn obstacles
                if (distanceTraveled - lastSpawnDistance > 320f + Random.nextFloat() * 200f) {
                    obstacles.add(Obstacle(x = 850f, type = Random.nextInt(2)))
                    lastSpawnDistance = distanceTraveled
                }

                // Spawn coins
                if (distanceTraveled - lastCoinSpawnDistance > 180f + Random.nextFloat() * 150f) {
                    val coinY = if (Random.nextBoolean()) 20f else 90f
                    coins.add(AdventureCoin(x = 850f, y = coinY))
                    lastCoinSpawnDistance = distanceTraveled
                }

                // Move obstacles
                val obstIter = obstacles.iterator()
                while (obstIter.hasNext()) {
                    val obs = obstIter.next()
                    obs.x -= speed

                    // Collision check with player (player is at x = 120, y = playerY, size ~ 40x50)
                    val playerX = 120f
                    val playerWidth = 36f
                    val playerHeight = 46f

                    val obsX = obs.x
                    val obsWidth = obs.width
                    val obsHeight = obs.height

                    val overlapX = playerX + playerWidth > obsX && playerX < obsX + obsWidth
                    val overlapY = playerY < obsHeight

                    if (overlapX && overlapY && invulnerableFrames <= 0) {
                        currentHp--
                        // Moss golem gives longer invulnerability
                        val invulnTime = if (activePet?.id == "pet_moss_golem") 90 else 55
                        invulnerableFrames = invulnTime
                        ChiptuneAudio.playHit()

                        if (currentHp <= 0) {
                            isGameOver = true
                            isPlaying = false
                            viewModel.recordAdventureEnd(
                                score = (distanceTraveled * 10).toInt() + coinsCollectedInRun * 25,
                                distance = distanceTraveled.toInt(),
                                coins = coinsCollectedInRun
                            )
                        }
                    }

                    if (obs.x < -100f) {
                        obstIter.remove()
                    }
                }

                // Move coins & collection check
                val coinIter = coins.iterator()
                while (coinIter.hasNext()) {
                    val coin = coinIter.next()
                    coin.x -= speed

                    // Forest cat has coin magnet pull!
                    if (activePet?.id == "pet_forest_cat" && coin.x in 80f..250f) {
                        coin.x -= 3f
                    }

                    val playerX = 120f
                    val hitCoin = (coin.x in (playerX - 25f)..(playerX + 45f)) &&
                            (playerY + 25f >= coin.y - 15f && playerY <= coin.y + 35f)

                    if (hitCoin && !coin.collected) {
                        coin.collected = true
                        coinsCollectedInRun++
                        ChiptuneAudio.playCoin()
                    }

                    if (coin.x < -100f || coin.collected) {
                        coinIter.remove()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isPlaying && !isGameOver) {
                    jump()
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Health Hearts
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..maxHp) {
                        PixelHeart(
                            isFilled = i <= currentHp,
                            size = 20.dp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                // In-Run Stats
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Coins
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        PixelCoin(size = 16.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coinsCollectedInRun",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PixelGoldGlow
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Distance
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${distanceTraveled.toInt()}m",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PixelMana
                        )
                    }
                }
            }

            // Game Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, PixelBorderHighlight, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val groundY = canvasHeight - 80f

                    // 1. Sky Gradient Background
                    drawRect(
                        Color(0xFF0F172A),
                        topLeft = Offset(0f, 0f),
                        size = Size(canvasWidth, groundY)
                    )

                    // 2. Distant Pixel Mountains (Parallax layer 1)
                    val mountainPath = Path().apply {
                        moveTo(0f, groundY - 140f)
                        var mx = -(bgOffset * 0.2f) % 200f
                        while (mx < canvasWidth + 200f) {
                            lineTo(mx + 70f, groundY - 200f)
                            lineTo(mx + 140f, groundY - 140f)
                            mx += 140f
                        }
                        lineTo(canvasWidth, groundY)
                        lineTo(0f, groundY)
                        close()
                    }
                    drawPath(mountainPath, Color(0xFF1E293B))

                    // 3. Midground Pixel Pine Trees (Parallax layer 2)
                    var treeX = -(bgOffset * 0.5f) % 120f
                    while (treeX < canvasWidth + 120f) {
                        val treeBase = groundY - 10f
                        drawRect(Color(0xFF2C1810), Offset(treeX + 16f, treeBase - 25f), Size(8f, 25f))
                        // Tree leaves
                        drawRect(Color(0xFF1B4332), Offset(treeX + 4f, treeBase - 55f), Size(32f, 18f))
                        drawRect(Color(0xFF2D6A4F), Offset(treeX + 8f, treeBase - 75f), Size(24f, 22f))
                        drawRect(Color(0xFF40916C), Offset(treeX + 12f, treeBase - 90f), Size(16f, 18f))
                        treeX += 110f
                    }

                    // 4. Ground (Foreground)
                    drawRect(
                        Color(0xFF2D6A4F),
                        topLeft = Offset(0f, groundY),
                        size = Size(canvasWidth, 16f) // Grass top
                    )
                    drawRect(
                        Color(0xFF1F2421),
                        topLeft = Offset(0f, groundY + 16f),
                        size = Size(canvasWidth, canvasHeight - groundY - 16f) // Earth
                    )

                    // Ground decorative pixel stones
                    var stoneX = -(bgOffset) % 90f
                    while (stoneX < canvasWidth + 90f) {
                        drawRect(Color(0xFF333D29), Offset(stoneX + 20f, groundY + 22f), Size(10f, 6f))
                        stoneX += 90f
                    }

                    // 5. Draw Obstacles
                    for (obs in obstacles) {
                        val oy = groundY - obs.height
                        if (obs.type == 0) {
                            // Spiked Bramble / Thorns
                            drawRect(Color(0xFF4A1525), Offset(obs.x, oy), Size(obs.width, obs.height))
                            drawRect(PixelRuby, Offset(obs.x + 8f, oy + 4f), Size(8f, obs.height - 8f))
                            drawRect(PixelRuby, Offset(obs.x + 24f, oy + 12f), Size(8f, obs.height - 16f))
                        } else {
                            // Ancient Ruin Stone Pillar
                            drawRect(Color(0xFF475569), Offset(obs.x, oy), Size(obs.width, obs.height))
                            drawRect(Color(0xFF64748B), Offset(obs.x + 4f, oy + 4f), Size(obs.width - 8f, obs.height - 8f))
                            drawRect(PixelMana, Offset(obs.x + 12f, oy + 12f), Size(8f, 12f)) // glowing glyph
                        }
                    }

                    // 6. Draw Collectible Coins
                    for (coin in coins) {
                        if (!coin.collected) {
                            val cy = groundY - coin.y
                            drawCircle(PixelGoldGlow, radius = 12f, center = Offset(coin.x, cy))
                            drawCircle(PixelGold, radius = 9f, center = Offset(coin.x, cy))
                            drawCircle(Color(0xFFB5871D), radius = 5f, center = Offset(coin.x, cy))
                        }
                    }

                    // 7. Draw Player Adventurer Sprite
                    val playerX = 120f
                    val playerBottom = groundY - playerY
                    val isFlicker = (invulnerableFrames / 4) % 2 == 1

                    if (!isFlicker) {
                        val legShift = if (isJumping) -4f else if (animFrame % 2 == 0) 4f else -4f
                        // Shadow
                        drawOval(Color(0x66000000), Offset(playerX - 4f, groundY - 4f), Size(42f, 10f))

                        // Boots
                        drawRect(Color(0xFF5A3E28), Offset(playerX + 6f + legShift, playerBottom - 10f), Size(10f, 10f))
                        drawRect(Color(0xFF5A3E28), Offset(playerX + 20f - legShift, playerBottom - 10f), Size(10f, 10f))

                        // Body / Tunic
                        drawRect(Color(0xFF1D3557), Offset(playerX + 6f, playerBottom - 32f), Size(24f, 22f))
                        // Gold belt buckle
                        drawRect(PixelGold, Offset(playerX + 14f, playerBottom - 18f), Size(8f, 6f))

                        // Cloak
                        drawRect(PixelEmerald, Offset(playerX + 2f, playerBottom - 30f), Size(6f, 18f))

                        // Head & Face
                        drawRect(Color(0xFFFFD1A9), Offset(playerX + 10f, playerBottom - 46f), Size(18f, 16f))
                        // Adventurer Hat / Hair
                        drawRect(Color(0xFF457B9D), Offset(playerX + 8f, playerBottom - 52f), Size(22f, 8f))
                        // Eyes
                        drawRect(Color(0xFF1D3557), Offset(playerX + 20f, playerBottom - 40f), Size(4f, 4f))
                    }

                    // 8. Draw Active Companion Pet
                    if (activePet != null) {
                        val petX = playerX - 34f
                        val petY = groundY - 26f + sin((tickCount * 0.15f).toDouble()).toFloat() * 6f
                        drawCircle(PixelEmeraldGlow.copy(alpha = 0.4f), radius = 16f, center = Offset(petX + 10f, petY + 10f))
                        drawRect(PixelEmerald, Offset(petX, petY), Size(20f, 20f))
                        drawRect(PixelGoldGlow, Offset(petX + 12f, petY + 4f), Size(4f, 4f))
                    }
                }

                // Overlay if not started
                if (!isPlaying && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xAA0C101A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "🌲 ENDLESS ADVENTURE 🌲",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = PixelGoldGlow
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Run through ancient forests, leap over obstacles, and gather sparkling guild gold!",
                                style = MaterialTheme.typography.bodySmall,
                                color = PixelTextParchment,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Best Distance: ${adventureRecord.bestDistance}m • Total Runs: ${adventureRecord.totalRuns}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = PixelMana
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { resetGame() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PixelGold,
                                    contentColor = Color(0xFF1B1400)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("start_adventure_button")
                            ) {
                                Text("EMBARK ON RUN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Game Over Overlay
                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xDD0C101A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "💀 REST AT CAMP 💀",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = PixelRuby
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Distance Traveled: ${distanceTraveled.toInt()}m",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = PixelTextParchment
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PixelCoin(size = 18.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+$coinsCollectedInRun Gold Saved To Bag",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = PixelGoldGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Row {
                                Button(
                                    onClick = { resetGame() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelGold,
                                        contentColor = Color(0xFF1B1400)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("RUN AGAIN", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = onReturnToCamp,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelSurfaceElevated,
                                        contentColor = PixelTextParchment
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("CAMP HAVEN")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Bar: Jump Button & In-Run Potion Use
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Healing Potion Shortcut
                Button(
                    onClick = { usePotion() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelSurfaceElevated,
                        contentColor = PixelTextParchment
                    ),
                    shape = RoundedCornerShape(6.dp),
                    enabled = isPlaying && !isGameOver && potionCount > 0 && currentHp < maxHp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PixelPotionIcon(size = 20.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("POTION ($potionCount)", fontWeight = FontWeight.Bold)
                    }
                }

                // Big Jump Button
                Button(
                    onClick = { jump() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color(0xFF1B1400)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .width(130.dp),
                    enabled = isPlaying && !isGameOver
                ) {
                    Text(
                        text = "JUMP ⬆️",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
