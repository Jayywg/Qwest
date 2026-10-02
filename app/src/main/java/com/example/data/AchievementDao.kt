package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET progress = :progress, isUnlocked = CASE WHEN :progress >= maxProgress THEN 1 ELSE isUnlocked END, unlockedAt = CASE WHEN :progress >= maxProgress AND unlockedAt IS NULL THEN :now ELSE unlockedAt END WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE achievements SET isRewardClaimed = 1 WHERE id = :id")
    suspend fun claimReward(id: String)
}
