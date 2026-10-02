package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PixelEmerald
import com.example.ui.theme.PixelGold
import com.example.ui.theme.PixelGoldDark
import com.example.ui.theme.PixelGoldGlow
import com.example.ui.theme.PixelMana
import com.example.ui.theme.PixelRuby
import com.example.ui.theme.PixelRubyGlow

/**
 * Pixel Art canvas renderers that draw sharp, retro pixel elements based on integer grid units.
 */

@Composable
fun PixelHeart(
    isFilled: Boolean = true,
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 10f
        val py = h / 10f

        val fillColor = if (isFilled) PixelRuby else Color(0xFF333842)
        val highlightColor = if (isFilled) PixelRubyGlow else Color(0xFF4A5160)
        val borderColor = Color(0xFF0F1115)

        // Heart shape matrix (10x10)
        // Draw main body
        drawRect(borderColor, Offset(2 * px, 1 * py), Size(2 * px, 1 * py))
        drawRect(borderColor, Offset(6 * px, 1 * py), Size(2 * px, 1 * py))
        drawRect(borderColor, Offset(1 * px, 2 * py), Size(1 * px, 3 * py))
        drawRect(borderColor, Offset(8 * px, 2 * py), Size(1 * px, 3 * py))
        drawRect(borderColor, Offset(4 * px, 2 * py), Size(2 * px, 1 * py))
        drawRect(borderColor, Offset(2 * px, 5 * py), Size(1 * px, 2 * py))
        drawRect(borderColor, Offset(7 * px, 5 * py), Size(1 * px, 2 * py))
        drawRect(borderColor, Offset(3 * px, 7 * py), Size(1 * px, 1 * py))
        drawRect(borderColor, Offset(6 * px, 7 * py), Size(1 * px, 1 * py))
        drawRect(borderColor, Offset(4 * px, 8 * py), Size(2 * px, 1 * py))

        // Interior fill
        drawRect(fillColor, Offset(2 * px, 2 * py), Size(2 * px, 3 * py))
        drawRect(fillColor, Offset(6 * px, 2 * py), Size(2 * px, 3 * py))
        drawRect(fillColor, Offset(4 * px, 3 * py), Size(2 * px, 4 * py))
        drawRect(fillColor, Offset(2 * px, 4 * py), Size(6 * px, 2 * py))
        drawRect(fillColor, Offset(3 * px, 6 * py), Size(4 * px, 1 * py))
        drawRect(fillColor, Offset(4 * px, 7 * py), Size(2 * px, 1 * py))

        // Highlight
        if (isFilled) {
            drawRect(highlightColor, Offset(2 * px, 2 * py), Size(1 * px, 1 * py))
            drawRect(highlightColor, Offset(6 * px, 2 * py), Size(1 * px, 1 * py))
        }
    }
}

@Composable
fun PixelCoin(
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 10f
        val py = h / 10f

        val border = Color(0xFF4A3408)
        val outer = PixelGoldDark
        val main = PixelGold
        val shine = PixelGoldGlow

        // Border
        drawRect(border, Offset(3 * px, 0 * py), Size(4 * px, 1 * py))
        drawRect(border, Offset(3 * px, 9 * py), Size(4 * px, 1 * py))
        drawRect(border, Offset(1 * px, 2 * py), Size(1 * px, 6 * py))
        drawRect(border, Offset(8 * px, 2 * py), Size(1 * px, 6 * py))
        drawRect(border, Offset(2 * px, 1 * py), Size(1 * px, 1 * py))
        drawRect(border, Offset(7 * px, 1 * py), Size(1 * px, 1 * py))
        drawRect(border, Offset(2 * px, 8 * py), Size(1 * px, 1 * py))
        drawRect(border, Offset(7 * px, 8 * py), Size(1 * px, 1 * py))

        // Gold Fill
        drawRect(outer, Offset(2 * px, 2 * py), Size(6 * px, 6 * py))
        drawRect(main, Offset(3 * px, 2 * py), Size(4 * px, 6 * py))
        drawRect(main, Offset(2 * px, 3 * py), Size(6 * px, 4 * py))

        // Inner coin face & shine
        drawRect(shine, Offset(3 * px, 2 * py), Size(2 * px, 2 * py))
        drawRect(PixelGoldDark, Offset(4 * px, 4 * py), Size(2 * px, 3 * py))
    }
}

@Composable
fun PixelFire(
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 10f
        val py = h / 10f

        val red = PixelRuby
        val orange = Color(0xFFFB8500)
        val yellow = PixelGoldGlow

        // Outer flame (Red/Orange)
        drawRect(red, Offset(4 * px, 0 * py), Size(2 * px, 2 * py))
        drawRect(orange, Offset(3 * px, 2 * py), Size(4 * px, 3 * py))
        drawRect(orange, Offset(2 * px, 4 * py), Size(6 * px, 4 * py))
        drawRect(red, Offset(1 * px, 6 * py), Size(8 * px, 3 * py))
        drawRect(red, Offset(3 * px, 9 * py), Size(4 * px, 1 * py))

        // Core flame (Yellow)
        drawRect(yellow, Offset(4 * px, 4 * py), Size(2 * px, 4 * py))
        drawRect(yellow, Offset(3 * px, 6 * py), Size(4 * px, 2 * py))
    }
}

@Composable
fun PixelPotionIcon(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 12f
        val py = h / 12f

        val cork = Color(0xFF9C6644)
        val glass = Color(0xFFC7E2FE)
        val liquid = PixelRuby
        val shine = PixelRubyGlow

        // Cork
        drawRect(cork, Offset(5 * px, 1 * py), Size(2 * px, 2 * py))
        // Bottle neck
        drawRect(glass, Offset(4 * px, 3 * py), Size(4 * px, 2 * py))
        // Bottle body
        drawRect(glass, Offset(2 * px, 5 * py), Size(8 * px, 6 * py))
        // Liquid
        drawRect(liquid, Offset(3 * px, 7 * py), Size(6 * px, 3 * py))
        drawRect(shine, Offset(3 * px, 7 * py), Size(2 * px, 1 * py))
    }
}

@Composable
fun PixelEggIcon(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 12f
        val py = h / 12f

        val shell = Color(0xFFE9D8A6)
        val spots = PixelMana
        val border = Color(0xFF4A3E2D)

        // Outline & Shell
        drawRect(border, Offset(4 * px, 1 * py), Size(4 * px, 1 * py))
        drawRect(border, Offset(2 * px, 3 * py), Size(8 * px, 7 * py))
        drawRect(border, Offset(3 * px, 10 * py), Size(6 * px, 1 * py))

        drawRect(shell, Offset(4 * px, 2 * py), Size(4 * px, 8 * py))
        drawRect(shell, Offset(3 * px, 3 * py), Size(6 * px, 6 * py))

        // Magic spots
        drawRect(spots, Offset(4 * px, 4 * py), Size(2 * px, 2 * py))
        drawRect(spots, Offset(6 * px, 7 * py), Size(2 * px, 2 * py))
    }
}

@Composable
fun PixelEquipmentIcon(
    type: String,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 12f
        val py = h / 12f

        when (type) {
            "equip_iron_boots" -> {
                val iron = Color(0xFF9EAAAF)
                val ironDark = Color(0xFF505A60)
                drawRect(iron, Offset(3 * px, 3 * py), Size(3 * px, 5 * py))
                drawRect(iron, Offset(3 * px, 7 * py), Size(6 * px, 3 * py))
                drawRect(ironDark, Offset(3 * px, 9 * py), Size(6 * px, 1 * py))
            }
            "equip_traveler_cloak" -> {
                val cloak = Color(0xFF2B4C3F)
                val trim = PixelGold
                drawRect(cloak, Offset(3 * px, 2 * py), Size(6 * px, 8 * py))
                drawRect(trim, Offset(5 * px, 2 * py), Size(2 * px, 2 * py))
                drawRect(trim, Offset(3 * px, 9 * py), Size(6 * px, 1 * py))
            }
            "equip_moonlit_pendant" -> {
                val chain = Color(0xFFC0C7CE)
                val gem = PixelMana
                drawRect(chain, Offset(4 * px, 2 * py), Size(4 * px, 3 * py))
                drawRect(gem, Offset(4 * px, 5 * py), Size(4 * px, 4 * py))
                drawRect(PixelGoldGlow, Offset(5 * px, 6 * py), Size(2 * px, 2 * py))
            }
            else -> {
                drawRect(PixelGold, Offset(3 * px, 3 * py), Size(6 * px, 6 * py))
            }
        }
    }
}

@Composable
fun PixelPetIcon(
    petId: String,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 12f
        val py = h / 12f

        when (petId) {
            "pet_forest_cat" -> {
                val catColor = PixelEmerald
                val eye = PixelGoldGlow
                // Ears
                drawRect(catColor, Offset(3 * px, 2 * py), Size(2 * px, 2 * py))
                drawRect(catColor, Offset(7 * px, 2 * py), Size(2 * px, 2 * py))
                // Head
                drawRect(catColor, Offset(3 * px, 4 * py), Size(6 * px, 5 * py))
                // Eyes
                drawRect(eye, Offset(4 * px, 5 * py), Size(1 * px, 1 * py))
                drawRect(eye, Offset(7 * px, 5 * py), Size(1 * px, 1 * py))
                // Body & Tail
                drawRect(catColor, Offset(4 * px, 9 * py), Size(4 * px, 2 * py))
                drawRect(PixelGold, Offset(8 * px, 8 * py), Size(2 * px, 2 * py))
            }
            "pet_moon_owl" -> {
                val feather = Color(0xFF4A5568)
                val glowEye = PixelMana
                drawRect(feather, Offset(3 * px, 2 * py), Size(6 * px, 7 * py))
                drawRect(glowEye, Offset(4 * px, 4 * py), Size(2 * px, 2 * py))
                drawRect(glowEye, Offset(6 * px, 4 * py), Size(2 * px, 2 * py))
                drawRect(PixelGold, Offset(5 * px, 6 * py), Size(2 * px, 1 * py)) // Beak
            }
            "pet_moss_golem" -> {
                val stone = Color(0xFF6C757D)
                val moss = PixelEmerald
                drawRect(stone, Offset(3 * px, 2 * py), Size(6 * px, 8 * py))
                drawRect(moss, Offset(3 * px, 2 * py), Size(6 * px, 2 * py))
                drawRect(PixelGoldGlow, Offset(4 * px, 5 * py), Size(1 * px, 1 * py))
                drawRect(PixelGoldGlow, Offset(7 * px, 5 * py), Size(1 * px, 1 * py))
            }
            "pet_wolf_pup" -> {
                val fur = Color(0xFF7F8C8D)
                val snout = Color(0xFFBDC3C7)
                drawRect(fur, Offset(3 * px, 2 * py), Size(2 * px, 2 * py))
                drawRect(fur, Offset(7 * px, 2 * py), Size(2 * px, 2 * py))
                drawRect(fur, Offset(3 * px, 4 * py), Size(6 * px, 6 * py))
                drawRect(snout, Offset(5 * px, 6 * py), Size(2 * px, 2 * py))
                drawRect(PixelRubyGlow, Offset(4 * px, 5 * py), Size(1 * px, 1 * py))
                drawRect(PixelRubyGlow, Offset(7 * px, 5 * py), Size(1 * px, 1 * py))
            }
            else -> {
                drawRect(PixelMana, Offset(4 * px, 4 * py), Size(4 * px, 4 * py))
            }
        }
    }
}

@Composable
fun PixelTavernFireplace(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FireplaceAnim")
    val flamePhase: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlamePhase"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 16f
        val py = h / 16f

        val stoneDark = Color(0xFF2C2523)
        val stoneMid = Color(0xFF453B37)
        val stoneLight = Color(0xFF6B5D57)
        val hearthHole = Color(0xFF140F0D)
        val woodLog = Color(0xFF5A3825)

        // Stone chimney / fireplace arch
        drawRect(stoneDark, Offset(1 * px, 1 * py), Size(14 * px, 14 * py))
        drawRect(stoneMid, Offset(2 * px, 2 * py), Size(12 * px, 13 * py))
        // Arch lintel stones
        drawRect(stoneLight, Offset(3 * px, 3 * py), Size(10 * px, 2 * py))

        // Fireplace interior hearth
        drawRect(hearthHole, Offset(4 * px, 5 * py), Size(8 * px, 9 * py))

        // Fire logs
        drawRect(woodLog, Offset(4 * px, 12 * py), Size(8 * px, 2 * py))
        drawRect(Color(0xFF3B2014), Offset(5 * px, 11 * py), Size(6 * px, 2 * py))

        // Flickering fire
        val flameShift = (flamePhase * 1.5f * px)
        // Red core
        drawRect(PixelRuby, Offset(5 * px, 9 * py - flameShift), Size(6 * px, 4 * py + flameShift))
        // Amber / Orange mid
        drawRect(PixelGoldDark, Offset(6 * px, 8 * py - flameShift), Size(4 * px, 4 * py))
        // Yellow flicker tip
        drawRect(PixelGoldGlow, Offset(6.5f * px, 6.5f * py - flameShift), Size(3 * px, 3 * py))
        // Tiny spark
        if (flamePhase >= 0.5f) {
            drawRect(PixelGoldGlow, Offset(5 * px, 5.5f * py), Size(1.5f * px, 1.5f * px))
            drawRect(PixelRubyGlow, Offset(9.5f * px, 6 * py), Size(1.5f * px, 1.5f * px))
        }
    }
}

@Composable
fun PixelCharacterSprite(
    skinTone: String = "Fair",
    hairStyle: String = "Messy",
    hairColor: String = "Chestnut",
    outfitColor: String = "Navy",
    accessory: String = "None",
    hasBoots: Boolean = false,
    hasCloak: Boolean = false,
    hasPendant: Boolean = false,
    size: Dp = 72.dp,
    idleBob: Float = 0f,
    modifier: Modifier = Modifier
) {
    val skin = when (skinTone.lowercase()) {
        "warm" -> Color(0xFFFCD5B5)
        "tan" -> Color(0xFFE0AC69)
        "olive" -> Color(0xFFC68642)
        "deep" -> Color(0xFF8D5524)
        else -> Color(0xFFFFE0BD) // Fair
    }

    val hair = when (hairColor.lowercase()) {
        "golden" -> Color(0xFFF1C40F)
        "obsidian" -> Color(0xFF2C3E50)
        "silver" -> Color(0xFFBDC3C7)
        "crimson" -> Color(0xFF962D2D)
        "emerald" -> Color(0xFF1E824C)
        else -> Color(0xFF6E4720) // Chestnut
    }

    val outfit = when (outfitColor.lowercase()) {
        "crimson" -> Color(0xFF9E2A2B)
        "forest" -> Color(0xFF2E6F40)
        "royal" -> Color(0xFF5B3286)
        "charcoal" -> Color(0xFF333533)
        else -> Color(0xFF1D3557) // Navy
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val px = w / 16f
        val py = h / 16f
        val bob = idleBob * py

        // Shadow under feet
        drawOval(Color(0x55000000), Offset(3 * px, 14 * py), Size(10 * px, 2 * py))

        // Boots / Shoes
        val bootColor = if (hasBoots) Color(0xFF9EAAAF) else Color(0xFF4A3420) // Steel if equipped, else leather
        drawRect(bootColor, Offset(4.5f * px, 13 * py + bob), Size(3 * px, 2 * py))
        drawRect(bootColor, Offset(8.5f * px, 13 * py + bob), Size(3 * px, 2 * py))

        // Legs / Pants
        val pantsColor = Color(0xFF2B3A42)
        drawRect(pantsColor, Offset(5 * px, 10 * py + bob), Size(2.5f * px, 3 * py))
        drawRect(pantsColor, Offset(8.5f * px, 10 * py + bob), Size(2.5f * px, 3 * py))

        // Cloak back if equipped
        if (hasCloak) {
            val cloakColor = Color(0xFF1B4332)
            drawRect(cloakColor, Offset(3 * px, 5.5f * py + bob), Size(10 * px, 8 * py))
            drawRect(PixelGold, Offset(3 * px, 12.5f * py + bob), Size(10 * px, 1 * py))
        }

        // Torso / Tunics
        drawRect(outfit, Offset(4.5f * px, 6 * py + bob), Size(7 * px, 4.5f * py))
        // Belt
        drawRect(Color(0xFF38220F), Offset(4.5f * px, 9.5f * py + bob), Size(7 * px, 1.2f * py))
        drawRect(PixelGold, Offset(7 * px, 9.5f * py + bob), Size(2 * px, 1.2f * py))

        // Moonlit Pendant if equipped
        if (hasPendant) {
            drawRect(Color(0xFFC0C7CE), Offset(7 * px, 6.5f * py + bob), Size(2 * px, 1 * py))
            drawRect(PixelMana, Offset(7.2f * px, 7.5f * py + bob), Size(1.6f * px, 1.6f * py))
        }

        // Arms & Hands
        drawRect(outfit, Offset(3 * px, 6.5f * py + bob), Size(1.8f * px, 3.5f * py))
        drawRect(skin, Offset(3 * px, 10 * py + bob), Size(1.8f * px, 1.5f * py))
        drawRect(outfit, Offset(11.2f * px, 6.5f * py + bob), Size(1.8f * px, 3.5f * py))
        drawRect(skin, Offset(11.2f * px, 10 * py + bob), Size(1.8f * px, 1.5f * py))

        // Neck
        drawRect(skin, Offset(6.5f * px, 5 * py + bob), Size(3 * px, 1.5f * py))

        // Head / Face
        drawRect(skin, Offset(4.5f * px, 2 * py + bob), Size(7 * px, 4 * py))

        // Eyes
        drawRect(Color(0xFF1B1B1B), Offset(6 * px, 3.5f * py + bob), Size(1.2f * px, 1.2f * py))
        drawRect(Color(0xFF1B1B1B), Offset(8.8f * px, 3.5f * py + bob), Size(1.2f * px, 1.2f * py))
        // Blushing cheeks
        drawRect(Color(0x44FF6B6B), Offset(5 * px, 4.5f * py + bob), Size(1.5f * px, 0.8f * py))
        drawRect(Color(0x44FF6B6B), Offset(9.5f * px, 4.5f * py + bob), Size(1.5f * px, 0.8f * py))

        // Hair Styles
        when (hairStyle.lowercase()) {
            "short" -> {
                drawRect(hair, Offset(4 * px, 1 * py + bob), Size(8 * px, 2 * py))
                drawRect(hair, Offset(3.5f * px, 2 * py + bob), Size(1.5f * px, 2.5f * py))
                drawRect(hair, Offset(11 * px, 2 * py + bob), Size(1.5f * px, 2.5f * py))
            }
            "long" -> {
                drawRect(hair, Offset(4 * px, 1 * py + bob), Size(8 * px, 2 * py))
                drawRect(hair, Offset(3.5f * px, 2 * py + bob), Size(2 * px, 7 * py))
                drawRect(hair, Offset(10.5f * px, 2 * py + bob), Size(2 * px, 7 * py))
            }
            "ponytail" -> {
                drawRect(hair, Offset(4 * px, 1 * py + bob), Size(8 * px, 2 * py))
                drawRect(hair, Offset(3.5f * px, 2 * py + bob), Size(1.5f * px, 2.5f * py))
                drawRect(hair, Offset(11 * px, 2 * py + bob), Size(1.5f * px, 2.5f * py))
                // Ponytail to the side
                drawRect(hair, Offset(11.5f * px, 2.5f * py + bob), Size(3 * px, 5 * py))
                drawRect(PixelRuby, Offset(11.5f * px, 2.5f * py + bob), Size(1.2f * px, 1.2f * py)) // Ribbon
            }
            else -> { // Messy / Classic
                drawRect(hair, Offset(4 * px, 0.8f * py + bob), Size(8 * px, 2.2f * py))
                drawRect(hair, Offset(3.5f * px, 1.5f * py + bob), Size(2 * px, 3.5f * py))
                drawRect(hair, Offset(10.5f * px, 1.5f * py + bob), Size(2 * px, 3.5f * py))
                drawRect(hair, Offset(6 * px, 2.5f * py + bob), Size(2 * px, 1 * py)) // Bangs
            }
        }

        // Accessory
        when (accessory.lowercase()) {
            "headband" -> {
                drawRect(PixelRuby, Offset(3.5f * px, 2 * py + bob), Size(9 * px, 1 * py))
                drawRect(PixelGold, Offset(7.5f * px, 1.8f * py + bob), Size(1 * px, 1.2f * py))
            }
            "glasses" -> {
                drawRect(PixelGold, Offset(5.5f * px, 3.2f * py + bob), Size(2.2f * px, 1.6f * py))
                drawRect(PixelGold, Offset(8.3f * px, 3.2f * py + bob), Size(2.2f * px, 1.6f * py))
                drawRect(PixelGold, Offset(7.7f * px, 3.6f * py + bob), Size(0.6f * px, 0.6f * py))
            }
            "eyepatch" -> {
                drawRect(Color(0xFF1B1B1B), Offset(8.5f * px, 3.2f * py + bob), Size(2 * px, 2 * py))
                drawLine(Color(0xFF1B1B1B), Offset(4 * px, 2.2f * py + bob), Offset(11 * px, 4.5f * py + bob), strokeWidth = 1.2f * px)
            }
            "feather" -> {
                drawRect(PixelEmerald, Offset(10.5f * px, 0 * py + bob), Size(1.5f * px, 2.5f * py))
                drawRect(PixelGoldGlow, Offset(11 * px, 0.5f * py + bob), Size(1.5f * px, 1.5f * py))
            }
        }
    }
}

