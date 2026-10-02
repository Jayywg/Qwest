package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QuestDifficulty(val expReward: Int, val displayName: String) {
    EASY(5, "Easy"),
    NORMAL(10, "Normal"),
    HARD(20, "Hard"),
    EPIC(40, "Epic")
}

enum class QuestRecurrence(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    ONE_TIME("One-Time")
}

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val difficulty: String = QuestDifficulty.NORMAL.name,
    val recurrence: String = QuestRecurrence.DAILY.name,
    val startDate: String = "",
    val endDate: String? = null,
    val reminderTime: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDifficultyEnum(): QuestDifficulty {
        return try {
            QuestDifficulty.valueOf(difficulty)
        } catch (e: Exception) {
            QuestDifficulty.NORMAL
        }
    }

    fun getRecurrenceEnum(): QuestRecurrence {
        return try {
            QuestRecurrence.valueOf(recurrence)
        } catch (e: Exception) {
            QuestRecurrence.DAILY
        }
    }
}
