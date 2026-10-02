package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val level: Int = 1,
    val exp: Int = 0,
    val gold: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = "",
    val totalQuestsCompleted: Int = 0,
    val totalExpEarned: Int = 0,
    val totalGoldEarned: Int = 0,
    val totalGoldSpent: Int = 0,
    val potionsUsed: Int = 0,
    val isFirstLaunch: Boolean = true,
    val characterName: String = "Adventurer",
    val characterGender: String = "Adventurer", // Adventurer, Knight, Mage, Ranger
    val hairStyle: String = "Messy", // Classic, Short, Long, Messy, Ponytail
    val hairColor: String = "Chestnut", // Chestnut, Golden, Obsidian, Silver, Crimson, Emerald
    val skinTone: String = "Fair", // Fair, Warm, Tan, Olive, Deep
    val outfitColor: String = "Navy", // Navy, Crimson, Forest, Royal, Charcoal
    val accessory: String = "None" // None, Headband, Glasses, Eyepatch, Feather
) {
    /**
     * EXP required to reach the next level.
     * Level 1: 100 EXP, Level 2: 180 EXP, Level 3: 260 EXP, etc.
     */
    fun expForNextLevel(): Int = level * 80 + 20
}
