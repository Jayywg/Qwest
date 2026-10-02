package com.example.ui.screens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameItemCatalog
import com.example.model.ItemCategory
import com.example.model.ShopItemDef
import com.example.ui.components.ParchmentBox
import com.example.ui.components.PixelCoin
import com.example.ui.components.PixelEggIcon
import com.example.ui.components.PixelEquipmentIcon
import com.example.ui.components.PixelPotionIcon
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
fun ShopScreen(
    viewModel: GameViewModel,
    onNavigateToInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    var selectedCategory by remember { mutableStateOf<ItemCategory?>(null) }
    var itemToBuy by remember { mutableStateOf<ShopItemDef?>(null) }

    val allItems = GameItemCatalog.ALL_SHOP_ITEMS
    val filteredItems = if (selectedCategory == null) {
        allItems
    } else {
        allItems.filter { it.category == selectedCategory }
    }

    Box(modifier = modifier.fillMaxSize().background(PixelBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header & Gold Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GUILD SHOP",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = PixelGoldGlow
                )

                // Gold pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PixelSurfaceElevated)
                        .border(1.dp, PixelGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    PixelCoin(size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${profile.gold} GOLD",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = PixelGoldGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAll = selectedCategory == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isAll) PixelGold else PixelSurface)
                            .border(1.dp, if (isAll) PixelGold else PixelBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "ALL",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isAll) Color(0xFF1B1400) else PixelTextMuted
                        )
                    }
                }
                items(ItemCategory.values()) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) PixelGold else PixelSurface)
                            .border(1.dp, if (isSelected) PixelGold else PixelBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            cat.name,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color(0xFF1B1400) else PixelTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shop Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val canAfford = profile.gold >= item.price
                    ShopItemRow(
                        item = item,
                        canAfford = canAfford,
                        onBuy = { itemToBuy = item }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Purchase Confirmation Dialog
    if (itemToBuy != null) {
        val item = itemToBuy!!
        val canAfford = profile.gold >= item.price

        AlertDialog(
            onDismissRequest = { itemToBuy = null },
            containerColor = PixelSurfaceElevated,
            title = {
                Text(
                    text = "PURCHASE ITEM?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PixelGoldGlow
                )
            },
            text = {
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PixelTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Effect: ${item.effectDescription}",
                        style = MaterialTheme.typography.labelMedium,
                        color = PixelEmerald
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Cost: ", color = PixelTextMuted)
                        PixelCoin(size = 16.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.price} Gold",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PixelGold
                        )
                    }
                    if (!canAfford) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You need ${item.price - profile.gold} more Gold to purchase this item.",
                            color = PixelRuby,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.buyItem(item.id)
                        itemToBuy = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color(0xFF1B1400)
                    ),
                    enabled = canAfford
                ) {
                    Text("BUY", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToBuy = null }) {
                    Text("CANCEL", color = PixelTextMuted)
                }
            }
        )
    }
}

@Composable
fun ShopItemRow(
    item: ShopItemDef,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PixelSurface,
        borderColor = PixelBorderHighlight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceElevated)
                    .border(1.dp, PixelBorder, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (item.category) {
                    ItemCategory.POTIONS -> PixelPotionIcon(size = 32.dp)
                    ItemCategory.PETS -> PixelEggIcon(size = 32.dp)
                    ItemCategory.EQUIPMENT -> PixelEquipmentIcon(type = item.id, size = 32.dp)
                    ItemCategory.COSMETICS -> PixelEquipmentIcon(type = item.id, size = 32.dp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = PixelTextParchment
                )
                Text(
                    text = item.effectDescription,
                    style = MaterialTheme.typography.labelSmall,
                    color = PixelEmerald
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price & Buy Button
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) PixelGold else PixelSurfaceElevated,
                    contentColor = if (canAfford) Color(0xFF1B1400) else PixelTextDim
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelCoin(size = 14.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.price}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}
