package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // "STREAK", "QUESTS", "ECONOMY", "ADVENTURE", "PETS"
    val icon: String = "🏆",
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val rewardGold: Int = 10,
    val rewardExp: Int = 20,
    val isRewardClaimed: Boolean = false
)

object AchievementCatalog {
    val INITIAL_ACHIEVEMENTS = listOf(
        Achievement(
            id = "first_quest",
            title = "First Step",
            description = "Complete your very first real-life quest.",
            category = "QUESTS",
            maxProgress = 1,
            rewardGold = 10,
            rewardExp = 25
        ),
        Achievement(
            id = "streak_3",
            title = "Spark of Habit",
            description = "Maintain a 3-day quest streak.",
            category = "STREAK",
            maxProgress = 3,
            rewardGold = 15,
            rewardExp = 30
        ),
        Achievement(
            id = "streak_7",
            title = "Steadfast Ranger",
            description = "Maintain a 7-day quest streak.",
            category = "STREAK",
            maxProgress = 7,
            rewardGold = 35,
            rewardExp = 70
        ),
        Achievement(
            id = "streak_14",
            title = "Disciplined Journey",
            description = "Maintain a 14-day quest streak.",
            category = "STREAK",
            maxProgress = 14,
            rewardGold = 60,
            rewardExp = 120
        ),
        Achievement(
            id = "streak_30",
            title = "Living Legend",
            description = "Reach a glorious 30-day quest streak.",
            category = "STREAK",
            maxProgress = 30,
            rewardGold = 150,
            rewardExp = 300
        ),
        Achievement(
            id = "gold_100",
            title = "Treasure Hunter",
            description = "Accumulate a lifetime total of 100 Gold.",
            category = "ECONOMY",
            maxProgress = 100,
            rewardGold = 25,
            rewardExp = 50
        ),
        Achievement(
            id = "quests_25",
            title = "Seasoned Adventurer",
            description = "Complete 25 total quests.",
            category = "QUESTS",
            maxProgress = 25,
            rewardGold = 30,
            rewardExp = 80
        ),
        Achievement(
            id = "adventure_300",
            title = "Far Wanderer",
            description = "Survive to reach a distance of 300m in Adventure Mode.",
            category = "ADVENTURE",
            maxProgress = 300,
            rewardGold = 25,
            rewardExp = 50
        ),
        Achievement(
            id = "pet_collector",
            title = "Beastmaster",
            description = "Hatch and unlock at least 2 fantasy companions.",
            category = "PETS",
            maxProgress = 2,
            rewardGold = 40,
            rewardExp = 90
        )
    )
}

@Entity(tableName = "adventure_records")
data class AdventureRecord(
    @PrimaryKey val id: Int = 1,
    val bestScore: Int = 0,
    val bestDistance: Int = 0,
    val totalRuns: Int = 0,
    val totalCoinsCollected: Int = 0
)
