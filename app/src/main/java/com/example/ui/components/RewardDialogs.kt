package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CompletionResult
import com.example.data.EggHatchResult
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelBorderHighlight
import com.example.ui.theme.PixelEmerald
import com.example.ui.theme.PixelGold
import com.example.ui.theme.PixelGoldGlow
import com.example.ui.theme.PixelMana
import com.example.ui.theme.PixelRuby
import com.example.ui.theme.PixelRubyGlow
import com.example.ui.theme.PixelSurface
import com.example.ui.theme.PixelSurfaceElevated
import com.example.ui.theme.PixelTextMuted
import com.example.ui.theme.PixelTextParchment

@Composable
fun QuestCompleteDialog(
    result: CompletionResult,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PixelSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(2.dp, PixelGold, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚔️ QUEST COMPLETE! ⚔️",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = PixelGoldGlow
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .offset(y = bounceOffset.dp)
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PixelSurface)
                        .border(1.dp, PixelGold, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    PixelCoin(size = 36.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ParchmentBadge(
                        label = "+${result.expGained} EXP",
                        textColor = PixelMana
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    ParchmentBadge(
                        label = "+${result.goldGained} GOLD",
                        textColor = PixelGold
                    )
                }

                if (result.didLevelUp) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⭐ LEVEL UP! (Level ${result.newLevel}) ⭐\n+${result.levelUpGoldBonus} Bonus Gold!",
                        color = PixelEmerald,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color(0xFF1B1400)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CONTINUE JOURNEY",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EggHatchDialog(
    result: EggHatchResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PixelSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(2.dp, PixelMana, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨ PET EGG HATCHED! ✨",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = PixelMana
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PixelSurface)
                        .border(1.5.dp, PixelMana, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    PixelPetIcon(petId = result.pet.id, size = 48.dp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.pet.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = PixelTextParchment
                )

                Text(
                    text = result.pet.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = PixelGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result.pet.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                ParchmentBox(
                    backgroundColor = PixelSurface,
                    borderColor = PixelBorderHighlight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "PASSIVE ABILITY:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PixelEmerald
                        )
                        Text(
                            text = result.pet.abilityDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = PixelTextParchment
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelMana,
                        contentColor = Color(0xFF002233)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "WELCOME COMPANION",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StreakMilestoneDialog(
    streakDays: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PixelSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(2.dp, PixelRuby, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔥 STREAK MILESTONE! 🔥",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = PixelRubyGlow
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelFire(size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$streakDays DAYS CONSECUTIVE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your steadfast dedication has earned valuable supplies from the guild!",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                ParchmentBadge(
                    label = "Bonus Gold & Healing Potions Claimed",
                    textColor = PixelGold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelRuby,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GLORIOUS!",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ParchmentBadge(
    label: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(PixelSurface)
            .border(1.dp, PixelBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = textColor
        )
    }
}
