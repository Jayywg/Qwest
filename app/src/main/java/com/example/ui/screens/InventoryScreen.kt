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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameItemCatalog
import com.example.model.InventoryItem
import com.example.model.ItemCategory
import com.example.model.Pet
import com.example.ui.components.ParchmentBox
import com.example.ui.components.PixelEggIcon
import com.example.ui.components.PixelEquipmentIcon
import com.example.ui.components.PixelHeart
import com.example.ui.components.PixelPetIcon
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
import com.example.ui.theme.PixelTextMuted
import com.example.ui.theme.PixelTextParchment
import com.example.viewmodel.GameViewModel

@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    onNavigateToShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    val allPets by viewModel.allPets.collectAsState()
    val activePet by viewModel.activePet.collectAsState()
    val maxHp by viewModel.maxHp.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Equipment & Items, 1: Pets

    val eggItem = inventory.find { it.itemId == GameItemCatalog.PET_EGG.id }
    val eggCount = eggItem?.quantity ?: 0

    val potionItem = inventory.find { it.itemId == GameItemCatalog.HEALING_POTION.id }
    val potionCount = potionItem?.quantity ?: 0

    Box(modifier = modifier.fillMaxSize().background(PixelBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INVENTORY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = PixelGoldGlow
                )

                // HP Modifier Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PixelSurfaceElevated)
                        .border(1.dp, PixelRuby, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    PixelHeart(isFilled = true, size = 16.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MAX HP: $maxHp",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = PixelRuby
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Row
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
                            "GEAR & ITEMS",
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
                            "PETS (${allPets.count { it.unlocked }})",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTabIndex == 0) {
                // Gear & Consumables Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Quick Action: Hatch Egg Banner if eggs available
                    if (eggCount > 0) {
                        item {
                            ParchmentBox(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = PixelSurfaceElevated,
                                borderColor = PixelMana,
                                highlightBorder = true
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PixelEggIcon(size = 36.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Pet Egg Ready ($eggCount in bag)",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PixelMana
                                        )
                                        Text(
                                            text = "Hatch to unlock a companion!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PixelTextMuted
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.hatchPetEgg() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PixelMana,
                                            contentColor = Color(0xFF002233)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("HATCH", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Inventory Items list
                    val nonPetItems = inventory.filter { it.itemId != GameItemCatalog.PET_EGG.id }
                    if (nonPetItems.isEmpty() && eggCount == 0) {
                        item {
                            EmptyInventoryPlaceholder(onNavigateToShop = onNavigateToShop)
                        }
                    } else {
                        items(nonPetItems, key = { it.itemId }) { item ->
                            val def = GameItemCatalog.findDef(item.itemId)
                            if (def != null) {
                                InventoryItemCard(
                                    item = item,
                                    def = def,
                                    onToggleEquip = { viewModel.toggleEquip(item.itemId) },
                                    onUsePotion = { viewModel.useHealingPotion() }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // Pets Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(allPets, key = { it.id }) { pet ->
                        val isEquipped = activePet?.id == pet.id
                        PetCard(
                            pet = pet,
                            isActive = isEquipped,
                            onToggleActive = {
                                if (isEquipped) {
                                    viewModel.setActivePet(null)
                                } else if (pet.unlocked) {
                                    viewModel.setActivePet(pet.id)
                                }
                            }
                        )
                    }

                    if (eggCount > 0) {
                        item {
                            Button(
                                onClick = { viewModel.hatchPetEgg() },
                                colors = ButtonDefaults.buttonColors(containerColor = PixelMana, contentColor = Color(0xFF002233)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("HATCH PET EGG ($eggCount AVAILABLE)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    def: com.example.model.ShopItemDef,
    onToggleEquip: () -> Unit,
    onUsePotion: () -> Unit
) {
    val isEquipment = def.category == ItemCategory.EQUIPMENT
    val isPotion = def.category == ItemCategory.POTIONS

    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (item.isEquipped) PixelSurfaceElevated else PixelSurface,
        borderColor = if (item.isEquipped) PixelGold else PixelBorderHighlight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceElevated)
                    .border(1.dp, if (item.isEquipped) PixelGold else PixelBorder, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isPotion) {
                    PixelPotionIcon(size = 32.dp)
                } else {
                    PixelEquipmentIcon(type = def.id, size = 32.dp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = def.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PixelTextParchment
                    )
                    if (item.quantity > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "x${item.quantity}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PixelGold
                        )
                    }
                }
                Text(
                    text = def.effectDescription,
                    style = MaterialTheme.typography.labelSmall,
                    color = PixelEmerald
                )
                Text(
                    text = def.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isEquipment) {
                Button(
                    onClick = onToggleEquip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isEquipped) PixelGold else PixelSurfaceElevated,
                        contentColor = if (item.isEquipped) Color(0xFF1B1400) else PixelTextParchment
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (item.isEquipped) "EQUIPPED" else "EQUIP",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else if (isPotion) {
                Button(
                    onClick = onUsePotion,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelRuby,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = item.quantity > 0,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "USE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
    isActive: Boolean,
    onToggleActive: () -> Unit
) {
    ParchmentBox(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isActive) PixelSurfaceElevated else PixelSurface,
        borderColor = if (isActive) PixelEmerald else if (pet.unlocked) PixelBorderHighlight else PixelBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelSurfaceElevated)
                    .border(
                        1.5.dp,
                        if (isActive) PixelEmerald else PixelBorder,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (pet.unlocked) {
                    PixelPetIcon(petId = pet.id, size = 36.dp)
                } else {
                    Text("🔒", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (pet.unlocked) pet.name else "??? (Locked)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (pet.unlocked) PixelTextParchment else PixelTextMuted
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PixelEmerald
                        )
                    }
                }
                Text(
                    text = if (pet.unlocked) pet.title else "Hatch from Pet Egg",
                    style = MaterialTheme.typography.labelSmall,
                    color = PixelGold
                )
                Text(
                    text = if (pet.unlocked) pet.abilityDescription else "Ability locked until hatched.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelTextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (pet.unlocked) {
                Button(
                    onClick = onToggleActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) PixelEmerald else PixelSurfaceElevated,
                        contentColor = if (isActive) Color(0xFF032600) else PixelTextParchment
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isActive) "ACTIVE" else "SELECT",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyInventoryPlaceholder(
    onNavigateToShop: () -> Unit
) {
    ParchmentBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎒", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your pack is empty.",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = PixelTextParchment
            )
            Text(
                text = "Complete quests and visit the shop to begin collecting items.",
                style = MaterialTheme.typography.bodySmall,
                color = PixelTextMuted
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onNavigateToShop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelGold,
                    contentColor = Color(0xFF1B1400)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("VISIT GUILD SHOP", fontWeight = FontWeight.Bold)
            }
        }
    }
}
