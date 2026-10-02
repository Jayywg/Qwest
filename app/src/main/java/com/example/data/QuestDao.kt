package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Quest
import com.example.model.QuestCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quests ORDER BY createdAt DESC")
    fun getAllQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuestById(id: Long): Quest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest): Long

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    @Query("DELETE FROM quests WHERE id = :id")
    suspend fun deleteQuestById(id: Long)

    // Completions
    @Query("SELECT * FROM quest_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<QuestCompletion>>

    @Query("SELECT * FROM quest_completions")
    fun getAllCompletions(): Flow<List<QuestCompletion>>

    @Query("SELECT * FROM quest_completions WHERE questId = :questId AND date = :date LIMIT 1")
    suspend fun getCompletion(questId: Long, date: String): QuestCompletion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: QuestCompletion): Long

    @Query("DELETE FROM quest_completions WHERE questId = :questId AND date = :date")
    suspend fun deleteCompletion(questId: Long, date: String)

    @Query("DELETE FROM quest_completions WHERE questId = :questId")
    suspend fun deleteCompletionsForQuest(questId: Long)

    @Query("SELECT DISTINCT date FROM quest_completions ORDER BY date ASC")
    fun getAllDistinctCompletionDates(): Flow<List<String>>

    @Query("SELECT COUNT(id) FROM quest_completions")
    suspend fun getTotalCompletionCount(): Int
}
