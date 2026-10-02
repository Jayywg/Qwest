package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.AdventureRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AdventureRecordDao {
    @Query("SELECT * FROM adventure_records WHERE id = 1")
    fun getRecord(): Flow<AdventureRecord?>

    @Query("SELECT * FROM adventure_records WHERE id = 1")
    suspend fun getRecordSync(): AdventureRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AdventureRecord)

    @Update
    suspend fun updateRecord(record: AdventureRecord)
}
