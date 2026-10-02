package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets")
    fun getAllPets(): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getPetById(id: String): Pet?

    @Query("SELECT * FROM pets WHERE active = 1 LIMIT 1")
    fun getActivePet(): Flow<Pet?>

    @Query("SELECT * FROM pets WHERE active = 1 LIMIT 1")
    suspend fun getActivePetSync(): Pet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPets(pets: List<Pet>)

    @Update
    suspend fun updatePet(pet: Pet)

    @Query("UPDATE pets SET active = 0")
    suspend fun deactivateAllPets()

    @Query("UPDATE pets SET active = 1 WHERE id = :id")
    suspend fun setActivePet(id: String)

    @Query("UPDATE pets SET unlocked = 1 WHERE id = :id")
    suspend fun unlockPet(id: String)
}
