package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE quantity > 0 OR isEquipped = 1")
    fun getAllInventory(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE itemId = :itemId")
    suspend fun getItem(itemId: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET isEquipped = :equipped WHERE itemId = :itemId")
    suspend fun setEquipped(itemId: String, equipped: Boolean)
}
