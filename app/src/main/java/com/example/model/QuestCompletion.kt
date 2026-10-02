package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest_completions",
    indices = [
        Index(value = ["questId", "date"], unique = true)
    ]
)
data class QuestCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questId: Long,
    val date: String, // "YYYY-MM-DD"
    val expAwarded: Int,
    val goldAwarded: Int,
    val completedAt: Long = System.currentTimeMillis()
)
