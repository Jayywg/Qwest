package com.example.data

import com.example.model.Achievement
import com.example.model.AchievementCatalog
import com.example.model.AdventureRecord
import com.example.model.GameItemCatalog
import com.example.model.InventoryItem
import com.example.model.Pet
import com.example.model.PetCatalog
import com.example.model.Quest
import com.example.model.QuestCompletion
import com.example.model.QuestDifficulty
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class CompletionResult(
    val expGained: Int,
    val goldGained: Int,
    val didLevelUp: Boolean,
    val oldLevel: Int,
    val newLevel: Int,
    val levelUpGoldBonus: Int,
    val streakMilestoneHit: Int?
)

data class EggHatchResult(
    val pet: Pet,
    val isNew: Boolean
)

class GameRepository(private val db: AppDatabase) {
    private val questDao = db.questDao()
    private val profileDao = db.userProfileDao()
    private val inventoryDao = db.inventoryDao()
    private val petDao = db.petDao()
    private val achievementDao = db.achievementDao()
    private val adventureDao = db.adventureRecordDao()

    val userProfile: Flow<UserProfile?> = profileDao.getProfile()
    val activeQuests: Flow<List<Quest>> = questDao.getActiveQuests()
    val archivedQuests: Flow<List<Quest>> = questDao.getArchivedQuests()
    val inventory: Flow<List<InventoryItem>> = inventoryDao.getAllInventory()
    val allPets: Flow<List<Pet>> = petDao.getAllPets()
    val activePet: Flow<Pet?> = petDao.getActivePet()
    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()
    val adventureRecord: Flow<AdventureRecord?> = adventureDao.getRecord()
    val allCompletionDates: Flow<List<String>> = questDao.getAllDistinctCompletionDates()

    fun getCompletionsForDate(date: String): Flow<List<QuestCompletion>> =
        questDao.getCompletionsForDate(date)

    // Format helper for YYYY-MM-DD
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun createQuest(quest: Quest): Long {
        return questDao.insertQuest(quest)
    }

    suspend fun updateQuest(quest: Quest) {
        questDao.updateQuest(quest)
    }

    suspend fun deleteQuest(quest: Quest) {
        questDao.deleteCompletionsForQuest(quest.id)
        questDao.deleteQuest(quest)
    }

    suspend fun archiveQuest(quest: Quest, archive: Boolean) {
        questDao.updateQuest(quest.copy(isArchived = archive))
    }

    suspend fun duplicateQuest(quest: Quest): Long {
        val copy = quest.copy(
            id = 0,
            title = "${quest.title} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        return questDao.insertQuest(copy)
    }

    /**
     * Complete a quest for a specific date.
     * Prevents reward exploit: If completion already exists for (questId, date), no duplicate rewards.
     */
    suspend fun completeQuest(quest: Quest, date: String): CompletionResult? {
        val existing = questDao.getCompletion(quest.id, date)
        if (existing != null) {
            // Already completed today, no extra rewards
            return null
        }

        val baseExp = quest.getDifficultyEnum().expReward

        // Check active pet bonus: Moon Owl grants +25% EXP
        val activePet = petDao.getActivePetSync()
        val expMultiplier = if (activePet?.id == "pet_moon_owl") 1.25f else 1.0f
        val expGained = (baseExp * expMultiplier).toInt().coerceAtLeast(1)

        // Random gold 1..10
        val goldGained = Random.nextInt(1, 11)

        val completion = QuestCompletion(
            questId = quest.id,
            date = date,
            expAwarded = expGained,
            goldAwarded = goldGained
        )
        questDao.insertCompletion(completion)

        // Update profile
        var profile = profileDao.getProfileSync() ?: UserProfile()
        var newExp = profile.exp + expGained
        var newGold = profile.gold + goldGained
        var newLevel = profile.level
        var didLevelUp = false
        var levelUpBonus = 0

        var neededExp = profile.expForNextLevel()
        while (newExp >= neededExp) {
            newExp -= neededExp
            newLevel++
            didLevelUp = true
            levelUpBonus += 10
            newGold += 10 // Level up reward
            neededExp = UserProfile(level = newLevel).expForNextLevel()
        }

        // Calculate updated streak
        val newStreak = calculateStreakAfterCompletion(date, profile)
        val longestStreak = maxOf(profile.longestStreak, newStreak)

        val updatedProfile = profile.copy(
            level = newLevel,
            exp = newExp,
            gold = newGold,
            currentStreak = newStreak,
            longestStreak = longestStreak,
            lastActiveDate = date,
            totalQuestsCompleted = profile.totalQuestsCompleted + 1,
            totalExpEarned = profile.totalExpEarned + expGained,
            totalGoldEarned = profile.totalGoldEarned + goldGained + levelUpBonus,
            isFirstLaunch = false
        )
        profileDao.updateProfile(updatedProfile)

        // Check milestone rewards
        val milestoneHit = checkStreakMilestones(newStreak)

        // Update achievements
        updateAchievementsAfterQuest(updatedProfile)

        return CompletionResult(
            expGained = expGained,
            goldGained = goldGained,
            didLevelUp = didLevelUp,
            oldLevel = profile.level,
            newLevel = newLevel,
            levelUpGoldBonus = levelUpBonus,
            streakMilestoneHit = milestoneHit
        )
    }

    /**
     * Uncomplete a quest. Deletes completion record. Currency/EXP gained is kept to avoid negative states.
     */
    suspend fun uncompleteQuest(questId: Long, date: String) {
        questDao.deleteCompletion(questId, date)
        // Recalculate streak
        val profile = profileDao.getProfileSync() ?: return
        val currentStreak = recalculateStreakFromHistory()
        profileDao.updateProfile(profile.copy(currentStreak = currentStreak))
    }

    private suspend fun calculateStreakAfterCompletion(date: String, profile: UserProfile): Int {
        val today = getTodayDateString()
        if (date != today) {
            return recalculateStreakFromHistory()
        }
        val yesterday = getYesterdayDateString()
        return if (profile.lastActiveDate == yesterday) {
            profile.currentStreak + 1
        } else if (profile.lastActiveDate == today) {
            profile.currentStreak.coerceAtLeast(1)
        } else {
            1
        }
    }

    private suspend fun recalculateStreakFromHistory(): Int {
        val dates = questDao.getAllDistinctCompletionDates().firstOrNull() ?: emptyList()
        if (dates.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateSet = dates.toSet()
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        var streak = 0
        // If today has completion, start from today. If not, check if yesterday had completion.
        if (dateSet.contains(todayStr)) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)
            if (!dateSet.contains(yesterdayStr)) {
                return 0
            }
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val prevDateStr = sdf.format(cal.time)
            if (dateSet.contains(prevDateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    private suspend fun checkStreakMilestones(streak: Int): Int? {
        val milestones = listOf(3, 7, 14, 30, 60, 100)
        if (streak in milestones) {
            val profile = profileDao.getProfileSync() ?: return null
            // Award milestone bonus
            val (bonusGold, bonusPotion) = when (streak) {
                3 -> 15 to 1
                7 -> 30 to 1
                14 -> 50 to 2
                30 -> 100 to 3
                60 -> 150 to 4
                100 -> 300 to 5
                else -> 0 to 0
            }
            if (bonusGold > 0) {
                profileDao.updateProfile(
                    profile.copy(
                        gold = profile.gold + bonusGold,
                        totalGoldEarned = profile.totalGoldEarned + bonusGold
                    )
                )
            }
            if (bonusPotion > 0) {
                val potion = inventoryDao.getItem(GameItemCatalog.HEALING_POTION.id)
                val currentQty = potion?.quantity ?: 0
                inventoryDao.insertItem(
                    InventoryItem(itemId = GameItemCatalog.HEALING_POTION.id, quantity = currentQty + bonusPotion)
                )
            }
            return streak
        }
        return null
    }

    private suspend fun updateAchievementsAfterQuest(profile: UserProfile) {
        achievementDao.updateProgress("first_quest", profile.totalQuestsCompleted)
        achievementDao.updateProgress("streak_3", profile.currentStreak)
        achievementDao.updateProgress("streak_7", profile.currentStreak)
        achievementDao.updateProgress("streak_14", profile.currentStreak)
        achievementDao.updateProgress("streak_30", profile.currentStreak)
        achievementDao.updateProgress("gold_100", profile.totalGoldEarned)
        achievementDao.updateProgress("quests_25", profile.totalQuestsCompleted)
    }

    // Shop purchase
    suspend fun buyShopItem(itemId: String): Boolean {
        val def = GameItemCatalog.findDef(itemId) ?: return false
        val profile = profileDao.getProfileSync() ?: return false

        if (profile.gold < def.price) return false

        // Deduct gold
        profileDao.updateProfile(
            profile.copy(
                gold = profile.gold - def.price,
                totalGoldSpent = profile.totalGoldSpent + def.price
            )
        )

        // Add to inventory
        val existing = inventoryDao.getItem(itemId)
        val newQty = (existing?.quantity ?: 0) + 1
        val isEquipped = existing?.isEquipped ?: false
        inventoryDao.insertItem(
            InventoryItem(itemId = itemId, quantity = newQty, isEquipped = isEquipped)
        )

        return true
    }

    // Hatch a pet egg
    suspend fun hatchPetEgg(): EggHatchResult? {
        val eggItem = inventoryDao.getItem(GameItemCatalog.PET_EGG.id)
        if (eggItem == null || eggItem.quantity <= 0) return null

        // Deduct 1 egg
        inventoryDao.insertItem(eggItem.copy(quantity = eggItem.quantity - 1))

        // Find candidate pets
        val pets = petDao.getAllPets().firstOrNull() ?: PetCatalog.ALL_PETS
        val unhatched = pets.filter { !it.unlocked }
        val chosenPet = if (unhatched.isNotEmpty()) {
            unhatched.random()
        } else {
            pets.random()
        }

        val isNew = !chosenPet.unlocked
        if (isNew) {
            petDao.unlockPet(chosenPet.id)
        }
        // Auto-activate hatched pet if no pet active
        val currentActive = petDao.getActivePetSync()
        if (currentActive == null) {
            petDao.setActivePet(chosenPet.id)
        }

        // Check pet collector achievement
        val unlockedCount = pets.count { it.unlocked } + (if (isNew) 1 else 0)
        achievementDao.updateProgress("pet_collector", unlockedCount)

        return EggHatchResult(chosenPet, isNew)
    }

    suspend fun toggleEquip(itemId: String) {
        val item = inventoryDao.getItem(itemId) ?: return
        val newEquipped = !item.isEquipped
        inventoryDao.setEquipped(itemId, newEquipped)
    }

    suspend fun useHealingPotion(): Boolean {
        val potion = inventoryDao.getItem(GameItemCatalog.HEALING_POTION.id) ?: return false
        if (potion.quantity <= 0) return false

        inventoryDao.insertItem(potion.copy(quantity = potion.quantity - 1))
        val profile = profileDao.getProfileSync() ?: return true
        profileDao.updateProfile(profile.copy(potionsUsed = profile.potionsUsed + 1))
        return true
    }

    suspend fun setActivePet(petId: String?) {
        petDao.deactivateAllPets()
        if (petId != null) {
            petDao.setActivePet(petId)
        }
    }

    suspend fun calculateMaxHp(): Int {
        val baseHp = 3
        var extraHp = 0
        val items = inventoryDao.getAllInventory().firstOrNull() ?: emptyList()
        for (item in items) {
            if (item.isEquipped) {
                val def = GameItemCatalog.findDef(item.itemId)
                if (def != null) {
                    extraHp += def.hpBonus
                }
            }
        }
        return baseHp + extraHp
    }

    suspend fun recordAdventureRun(score: Int, distance: Int, coins: Int) {
        val record = adventureDao.getRecordSync() ?: AdventureRecord()
        val newBestScore = maxOf(record.bestScore, score)
        val newBestDistance = maxOf(record.bestDistance, distance)

        adventureDao.updateRecord(
            record.copy(
                bestScore = newBestScore,
                bestDistance = newBestDistance,
                totalRuns = record.totalRuns + 1,
                totalCoinsCollected = record.totalCoinsCollected + coins
            )
        )

        // Award in-run coins to player gold balance
        if (coins > 0) {
            val profile = profileDao.getProfileSync()
            if (profile != null) {
                profileDao.updateProfile(
                    profile.copy(
                        gold = profile.gold + coins,
                        totalGoldEarned = profile.totalGoldEarned + coins
                    )
                )
            }
        }

        achievementDao.updateProgress("adventure_300", distance)
    }

    suspend fun claimAchievementReward(achievementId: String): Boolean {
        val ach = achievementDao.getAchievement(achievementId) ?: return false
        if (!ach.isUnlocked || ach.isRewardClaimed) return false

        val profile = profileDao.getProfileSync() ?: return false
        profileDao.updateProfile(
            profile.copy(
                gold = profile.gold + ach.rewardGold,
                exp = profile.exp + ach.rewardExp,
                totalGoldEarned = profile.totalGoldEarned + ach.rewardGold,
                totalExpEarned = profile.totalExpEarned + ach.rewardExp
            )
        )
        achievementDao.claimReward(achievementId)
        return true
    }

    suspend fun completeOnboarding() {
        val profile = profileDao.getProfileSync() ?: return
        profileDao.updateProfile(profile.copy(isFirstLaunch = false))
    }

    suspend fun exportDataJson(): String {
        val root = JSONObject()
        val profile = profileDao.getProfileSync() ?: UserProfile()
        val profileJson = JSONObject().apply {
            put("level", profile.level)
            put("exp", profile.exp)
            put("gold", profile.gold)
            put("currentStreak", profile.currentStreak)
            put("longestStreak", profile.longestStreak)
            put("totalQuestsCompleted", profile.totalQuestsCompleted)
            put("totalExpEarned", profile.totalExpEarned)
            put("totalGoldEarned", profile.totalGoldEarned)
            put("totalGoldSpent", profile.totalGoldSpent)
            put("potionsUsed", profile.potionsUsed)
        }
        root.put("profile", profileJson)

        val quests = questDao.getAllQuests().firstOrNull() ?: emptyList()
        val questArray = JSONArray()
        for (q in quests) {
            val qObj = JSONObject().apply {
                put("title", q.title)
                put("description", q.description)
                put("category", q.category)
                put("difficulty", q.difficulty)
                put("recurrence", q.recurrence)
                put("startDate", q.startDate)
                put("isArchived", q.isArchived)
            }
            questArray.put(qObj)
        }
        root.put("quests", questArray)

        val inventoryItems = inventoryDao.getAllInventory().firstOrNull() ?: emptyList()
        val invArray = JSONArray()
        for (inv in inventoryItems) {
            val invObj = JSONObject().apply {
                put("itemId", inv.itemId)
                put("quantity", inv.quantity)
                put("isEquipped", inv.isEquipped)
            }
            invArray.put(invObj)
        }
        root.put("inventory", invArray)

        return root.toString(2)
    }

    suspend fun importDataJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                val importedProfile = UserProfile(
                    level = p.optInt("level", 1),
                    exp = p.optInt("exp", 0),
                    gold = p.optInt("gold", 0),
                    currentStreak = p.optInt("currentStreak", 0),
                    longestStreak = p.optInt("longestStreak", 0),
                    totalQuestsCompleted = p.optInt("totalQuestsCompleted", 0),
                    totalExpEarned = p.optInt("totalExpEarned", 0),
                    totalGoldEarned = p.optInt("totalGoldEarned", 0),
                    totalGoldSpent = p.optInt("totalGoldSpent", 0),
                    potionsUsed = p.optInt("potionsUsed", 0),
                    isFirstLaunch = false
                )
                profileDao.insertProfile(importedProfile)
            }

            if (root.has("quests")) {
                val arr = root.getJSONArray("quests")
                for (i in 0 until arr.length()) {
                    val q = arr.getJSONObject(i)
                    questDao.insertQuest(
                        Quest(
                            title = q.getString("title"),
                            description = q.optString("description", ""),
                            category = q.optString("category", "General"),
                            difficulty = q.optString("difficulty", "NORMAL"),
                            recurrence = q.optString("recurrence", "DAILY"),
                            startDate = q.optString("startDate", getTodayDateString()),
                            isArchived = q.optBoolean("isArchived", false)
                        )
                    )
                }
            }

            if (root.has("inventory")) {
                val arr = root.getJSONArray("inventory")
                for (i in 0 until arr.length()) {
                    val inv = arr.getJSONObject(i)
                    inventoryDao.insertItem(
                        InventoryItem(
                            itemId = inv.getString("itemId"),
                            quantity = inv.optInt("quantity", 1),
                            isEquipped = inv.optBoolean("isEquipped", false)
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        profileDao.updateProfile(profile)
    }

    suspend fun resetAllData() {
        db.clearAllTables()
        profileDao.insertProfile(UserProfile())
        adventureDao.insertRecord(AdventureRecord())
        petDao.insertPets(PetCatalog.ALL_PETS)
        achievementDao.insertAchievements(AchievementCatalog.INITIAL_ACHIEVEMENTS)
        inventoryDao.insertItem(
            InventoryItem(itemId = GameItemCatalog.HEALING_POTION.id, quantity = 2)
        )
    }
}
