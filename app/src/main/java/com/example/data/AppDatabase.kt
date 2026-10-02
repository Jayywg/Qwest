package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.Achievement
import com.example.model.AchievementCatalog
import com.example.model.AdventureRecord
import com.example.model.GameItemCatalog
import com.example.model.InventoryItem
import com.example.model.Pet
import com.example.model.PetCatalog
import com.example.model.Quest
import com.example.model.QuestCompletion
import com.example.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        Quest::class,
        QuestCompletion::class,
        InventoryItem::class,
        Pet::class,
        Achievement::class,
        AdventureRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun questDao(): QuestDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun petDao(): PetDao
    abstract fun achievementDao(): AchievementDao
    abstract fun adventureRecordDao(): AdventureRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val USER_DATABASES = mutableMapOf<String, AppDatabase>()

        fun getDatabaseForUser(
            context: Context,
            userId: String,
            initialAdventurerName: String? = null
        ): AppDatabase {
            val safeUserId = userId.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val dbName = "qwest_user_${safeUserId}.db"

            return synchronized(this) {
                USER_DATABASES.getOrPut(safeUserId) {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        dbName
                    ).fallbackToDestructiveMigration().addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initialize new user player state:
                            // Level 1, 0 EXP, 0 Gold, 0 Day Streak, starter character with user's name, no equipment, locked pets, no quests
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabaseForUser(context, userId, initialAdventurerName)
                                val heroName = initialAdventurerName?.takeIf { it.isNotBlank() } ?: "Adventurer"
                                database.userProfileDao().insertProfile(
                                    UserProfile(
                                        id = 1,
                                        characterName = heroName,
                                        level = 1,
                                        exp = 0,
                                        gold = 0,
                                        currentStreak = 0,
                                        longestStreak = 0,
                                        isFirstLaunch = false
                                    )
                                )
                                database.adventureRecordDao().insertRecord(AdventureRecord())
                                // Lock all pets for new accounts
                                database.petDao().insertPets(
                                    PetCatalog.ALL_PETS.map { it.copy(unlocked = false, active = false) }
                                )
                                database.achievementDao().insertAchievements(
                                    AchievementCatalog.INITIAL_ACHIEVEMENTS.map {
                                        it.copy(progress = 0, isUnlocked = false, isRewardClaimed = false)
                                    }
                                )
                                // Starter healing potion
                                database.inventoryDao().insertItem(
                                    InventoryItem(
                                        itemId = GameItemCatalog.HEALING_POTION.id,
                                        quantity = 2,
                                        isEquipped = false
                                    )
                                )
                            }
                        }
                    }).build()
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "questlog_rpg.db"
                ).fallbackToDestructiveMigration().addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate initial data
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            database.userProfileDao().insertProfile(UserProfile())
                            database.adventureRecordDao().insertRecord(AdventureRecord())
                            database.petDao().insertPets(PetCatalog.ALL_PETS)
                            database.achievementDao().insertAchievements(AchievementCatalog.INITIAL_ACHIEVEMENTS)
                            // Initial starter inventory
                            database.inventoryDao().insertItem(
                                InventoryItem(itemId = GameItemCatalog.HEALING_POTION.id, quantity = 2)
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
