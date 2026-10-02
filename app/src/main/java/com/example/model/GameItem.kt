package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemCategory {
    POTIONS,
    PETS,
    EQUIPMENT,
    COSMETICS
}

data class ShopItemDef(
    val id: String,
    val name: String,
    val description: String,
    val category: ItemCategory,
    val price: Int,
    val effectDescription: String,
    val hpBonus: Int = 0
)

object GameItemCatalog {
    val HEALING_POTION = ShopItemDef(
        id = "potion_heal",
        name = "Healing Potion",
        description = "A brewed vial of soothing mountain herbs. Restores 1 HP during Adventure Mode.",
        category = ItemCategory.POTIONS,
        price = 10,
        effectDescription = "+1 HP during Adventure"
    )

    val PET_EGG = ShopItemDef(
        id = "pet_egg",
        name = "Pet Egg",
        description = "A warm, patterned egg found in ancient ruins. Randomly hatches into a loyal companion.",
        category = ItemCategory.PETS,
        price = 50,
        effectDescription = "Hatches a companion"
    )

    val IRON_BOOTS = ShopItemDef(
        id = "equip_iron_boots",
        name = "Iron Boots",
        description = "Sturdy steel-reinforced greaves worn by wandering rangers. Grants +1 maximum HP.",
        category = ItemCategory.EQUIPMENT,
        price = 30,
        effectDescription = "+1 Max HP in Adventure",
        hpBonus = 1
    )

    val TRAVELERS_CLOAK = ShopItemDef(
        id = "equip_traveler_cloak",
        name = "Traveler's Cloak",
        description = "A weather-beaten cloak lined with protective enchantments. Grants +1 maximum HP.",
        category = ItemCategory.EQUIPMENT,
        price = 50,
        effectDescription = "+1 Max HP in Adventure",
        hpBonus = 1
    )

    val MOONLIT_PENDANT = ShopItemDef(
        id = "equip_moonlit_pendant",
        name = "Moonlit Pendant",
        description = "A silver talisman blessed by quiet lunar spirits. Grants +2 maximum HP.",
        category = ItemCategory.EQUIPMENT,
        price = 80,
        effectDescription = "+2 Max HP in Adventure",
        hpBonus = 2
    )

    val STARLIT_BADGE = ShopItemDef(
        id = "cosmetic_starlit_badge",
        name = "Starlit Badge",
        description = "An ornate lapel pin glowing with faint starlight. A mark of distinction for devoted questers.",
        category = ItemCategory.COSMETICS,
        price = 45,
        effectDescription = "Cosmetic Title Flair"
    )

    val ALL_SHOP_ITEMS = listOf(
        HEALING_POTION,
        PET_EGG,
        IRON_BOOTS,
        TRAVELERS_CLOAK,
        MOONLIT_PENDANT,
        STARLIT_BADGE
    )

    fun findDef(id: String): ShopItemDef? = ALL_SHOP_ITEMS.find { it.id == id }
}

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val itemId: String,
    val quantity: Int = 0,
    val isEquipped: Boolean = false
)
