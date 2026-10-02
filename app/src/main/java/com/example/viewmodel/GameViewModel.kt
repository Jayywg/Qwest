package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ChiptuneAudio
import com.example.auth.AuthRepository
import com.example.auth.AuthResult
import com.example.auth.SessionManager
import com.example.auth.UserSession
import com.example.data.AppDatabase
import com.example.data.CompletionResult
import com.example.data.EggHatchResult
import com.example.data.GameRepository
import com.example.model.Achievement
import com.example.model.AdventureRecord
import com.example.model.GameItemCatalog
import com.example.model.InventoryItem
import com.example.model.Pet
import com.example.model.Quest
import com.example.model.QuestCompletion
import com.example.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class AuthUiState {
    object Loading : AuthUiState()
    object SignedOut : AuthUiState()
    data class SignedIn(val session: UserSession) : AuthUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    val authRepository = AuthRepository(application, sessionManager)

    private val _isAuthenticated = MutableStateFlow(sessionManager.isLoggedIn())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentSession = MutableStateFlow(sessionManager.getActiveSession())
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(
        if (sessionManager.isLoggedIn() && sessionManager.getActiveSession() != null) {
            AuthUiState.SignedIn(sessionManager.getActiveSession()!!)
        } else {
            AuthUiState.SignedOut
        }
    )
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authLoadingMessage = MutableStateFlow("")
    val authLoadingMessage: StateFlow<String> = _authLoadingMessage.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessTransition = MutableStateFlow<String?>(null)
    val authSuccessTransition: StateFlow<String?> = _authSuccessTransition.asStateFlow()

    // Dynamic repository per authenticated user
    private val _currentRepo = MutableStateFlow(createInitialRepository())
    val currentRepo: StateFlow<GameRepository> = _currentRepo.asStateFlow()

    private val activeRepo: GameRepository get() = _currentRepo.value

    private fun createInitialRepository(): GameRepository {
        val session = sessionManager.getActiveSession()
        val db = if (session != null) {
            AppDatabase.getDatabaseForUser(getApplication(), session.userId, session.adventurerName)
        } else {
            AppDatabase.getDatabase(getApplication())
        }
        return GameRepository(db)
    }

    private fun switchUser(userId: String, adventurerName: String) {
        val newDb = AppDatabase.getDatabaseForUser(getApplication(), userId, adventurerName)
        _currentRepo.value = GameRepository(newDb)
    }

    init {
        viewModelScope.launch {
            try {
                authRepository.initializeAuth()
            } catch (ignored: Exception) {}
            val session = sessionManager.getActiveSession()
            if (session != null) {
                _isAuthenticated.value = true
                _currentSession.value = session
                _authUiState.value = AuthUiState.SignedIn(session)
            } else {
                _isAuthenticated.value = false
                _currentSession.value = null
                _authUiState.value = AuthUiState.SignedOut
            }
        }
    }

    val userProfile: StateFlow<UserProfile> = _currentRepo
        .flatMapLatest { it.userProfile }
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    val todayDateStr = activeRepo.getTodayDateString()

    private val _selectedDate = MutableStateFlow(todayDateStr)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val activeQuests: StateFlow<List<Quest>> = _currentRepo
        .flatMapLatest { it.activeQuests }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val archivedQuests: StateFlow<List<Quest>> = _currentRepo
        .flatMapLatest { it.archivedQuests }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val inventory: StateFlow<List<InventoryItem>> = _currentRepo
        .flatMapLatest { it.inventory }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allPets: StateFlow<List<Pet>> = _currentRepo
        .flatMapLatest { it.allPets }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activePet: StateFlow<Pet?> = _currentRepo
        .flatMapLatest { it.activePet }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val achievements: StateFlow<List<Achievement>> = _currentRepo
        .flatMapLatest { it.allAchievements }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val adventureRecord: StateFlow<AdventureRecord> = _currentRepo
        .flatMapLatest { it.adventureRecord }
        .map { it ?: AdventureRecord() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AdventureRecord())

    val allCompletionDates: StateFlow<List<String>> = _currentRepo
        .flatMapLatest { it.allCompletionDates }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun getCompletionsForDate(date: String): Flow<List<QuestCompletion>> =
        activeRepo.getCompletionsForDate(date)

    // UI Dialog Events
    private val _rewardEvent = MutableStateFlow<CompletionResult?>(null)
    val rewardEvent: StateFlow<CompletionResult?> = _rewardEvent.asStateFlow()

    private val _eggHatchResult = MutableStateFlow<EggHatchResult?>(null)
    val eggHatchResult: StateFlow<EggHatchResult?> = _eggHatchResult.asStateFlow()

    private val _streakMilestone = MutableStateFlow<Int?>(null)
    val streakMilestone: StateFlow<Int?> = _streakMilestone.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(true)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.18f)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    private val _isPlayingMusic = MutableStateFlow(false)
    val isPlayingMusic: StateFlow<Boolean> = _isPlayingMusic.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // Combined: Quests for selected date with completion status
    val selectedDateCompletions = combine(
        _selectedDate,
        activeQuests
    ) { date, _ ->
        activeRepo.getCompletionsForDate(date)
    }

    // Dynamic Max HP calculated from base (3) + equipped items
    val maxHp: StateFlow<Int> = combine(inventory) { invList ->
        var bonus = 0
        for (item in invList[0]) {
            if (item.isEquipped) {
                val def = GameItemCatalog.findDef(item.itemId)
                if (def != null) {
                    bonus += def.hpBonus
                }
            }
        }
        3 + bonus
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 3)

    // Authentication Actions
    fun enterAsGuest() {
        viewModelScope.launch {
            try {
                _authLoading.value = true
                _authLoadingMessage.value = "Entering the Tavern..."
                _authError.value = null
                delay(200)
                val session = sessionManager.saveSession("usr_demo_kaelen", "adventurer@qwest.com", "Kaelen")
                switchUser("usr_demo_kaelen", "Kaelen")
                _currentSession.value = session
                _isAuthenticated.value = true
                _authUiState.value = AuthUiState.SignedIn(session)
                _authSuccessTransition.value = "Welcome to the Tavern, Adventurer!"
                ChiptuneAudio.playLevelUp()
            } catch (e: Exception) {
                _authError.value = "Could not enter Tavern: ${e.localizedMessage}"
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun signIn(email: String, password: String, onDone: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _authLoading.value = true
                _authLoadingMessage.value = "Entering the Tavern..."
                _authError.value = null

                delay(300)
                val result = authRepository.signIn(email, password)
                when (result) {
                    is AuthResult.Success -> {
                        switchUser(result.session.userId, result.session.adventurerName)
                        _currentSession.value = result.session
                        _isAuthenticated.value = true
                        _authUiState.value = AuthUiState.SignedIn(result.session)
                        _authSuccessTransition.value = result.message
                        ChiptuneAudio.playLevelUp()
                        onDone?.invoke(true)
                    }
                    is AuthResult.Error -> {
                        _authError.value = result.message
                        ChiptuneAudio.playHit()
                        onDone?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                _authError.value = "Sign in error: ${e.localizedMessage ?: "Unknown error"}"
                ChiptuneAudio.playHit()
                onDone?.invoke(false)
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun signUp(
        adventurerName: String,
        email: String,
        password: String,
        confirmPassword: String,
        onDone: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                _authLoading.value = true
                _authLoadingMessage.value = "Preparing your adventure..."
                _authError.value = null

                delay(300)
                val result = authRepository.signUp(adventurerName, email, password, confirmPassword)
                when (result) {
                    is AuthResult.Success -> {
                        switchUser(result.session.userId, result.session.adventurerName)
                        _currentSession.value = result.session
                        _isAuthenticated.value = true
                        _authUiState.value = AuthUiState.SignedIn(result.session)
                        _authSuccessTransition.value = result.message
                        ChiptuneAudio.playLevelUp()
                        onDone?.invoke(true)
                    }
                    is AuthResult.Error -> {
                        _authError.value = result.message
                        ChiptuneAudio.playHit()
                        onDone?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                _authError.value = "Sign up error: ${e.localizedMessage ?: "Unknown error"}"
                ChiptuneAudio.playHit()
                onDone?.invoke(false)
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _authLoading.value = true
                _authLoadingMessage.value = "Sending carrier pigeon..."
                delay(300)
                val message = authRepository.sendPasswordReset(email)
                onResult(message)
            } catch (e: Exception) {
                onResult("If an account exists with this email, we've sent instructions to reset your password.")
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentSession.value = null
        _isAuthenticated.value = false
        _authUiState.value = AuthUiState.SignedOut
        _authSuccessTransition.value = null
        _authError.value = null
        _rewardEvent.value = null
        _eggHatchResult.value = null
        _streakMilestone.value = null
        switchUser("guest", "Adventurer")
        ChiptuneAudio.playClick()
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun clearAuthSuccessTransition() {
        _authSuccessTransition.value = null
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        ChiptuneAudio.playClick()
    }

    fun toggleQuestCompletion(quest: Quest, isCurrentlyCompleted: Boolean, date: String = _selectedDate.value) {
        viewModelScope.launch {
            if (isCurrentlyCompleted) {
                activeRepo.uncompleteQuest(quest.id, date)
                ChiptuneAudio.playClick()
            } else {
                val result = activeRepo.completeQuest(quest, date)
                if (result != null) {
                    _rewardEvent.value = result
                    ChiptuneAudio.playCoin()
                    if (result.didLevelUp) {
                        ChiptuneAudio.playLevelUp()
                    }
                    if (result.streakMilestoneHit != null) {
                        _streakMilestone.value = result.streakMilestoneHit
                    }
                }
            }
        }
    }

    fun dismissRewardEvent() {
        _rewardEvent.value = null
    }

    fun dismissEggHatch() {
        _eggHatchResult.value = null
    }

    fun dismissStreakMilestone() {
        _streakMilestone.value = null
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun addQuest(
        title: String,
        description: String,
        category: String,
        difficulty: String,
        recurrence: String,
        reminderTime: String? = null
    ) {
        viewModelScope.launch {
            activeRepo.createQuest(
                Quest(
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    difficulty = difficulty,
                    recurrence = recurrence,
                    startDate = todayDateStr,
                    reminderTime = reminderTime
                )
            )
            ChiptuneAudio.playClick()
            _infoMessage.value = "New Quest Added: $title"
        }
    }

    fun updateQuest(quest: Quest) {
        viewModelScope.launch {
            activeRepo.updateQuest(quest)
            ChiptuneAudio.playClick()
            _infoMessage.value = "Quest Updated"
        }
    }

    fun deleteQuest(quest: Quest) {
        viewModelScope.launch {
            activeRepo.deleteQuest(quest)
            ChiptuneAudio.playClick()
            _infoMessage.value = "Quest Deleted"
        }
    }

    fun duplicateQuest(quest: Quest) {
        viewModelScope.launch {
            activeRepo.duplicateQuest(quest)
            ChiptuneAudio.playClick()
            _infoMessage.value = "Quest Duplicated"
        }
    }

    fun archiveQuest(quest: Quest, archive: Boolean) {
        viewModelScope.launch {
            activeRepo.archiveQuest(quest, archive)
            ChiptuneAudio.playClick()
            _infoMessage.value = if (archive) "Quest Archived" else "Quest Restored"
        }
    }

    fun buyItem(itemId: String) {
        viewModelScope.launch {
            val success = activeRepo.buyShopItem(itemId)
            if (success) {
                ChiptuneAudio.playCoin()
                val def = GameItemCatalog.findDef(itemId)
                _infoMessage.value = "Purchased: ${def?.name ?: "Item"}!"
            } else {
                ChiptuneAudio.playHit()
                _infoMessage.value = "Not enough Gold!"
            }
        }
    }

    fun hatchPetEgg() {
        viewModelScope.launch {
            val result = activeRepo.hatchPetEgg()
            if (result != null) {
                ChiptuneAudio.playEggHatch()
                _eggHatchResult.value = result
            } else {
                ChiptuneAudio.playHit()
                _infoMessage.value = "No Pet Eggs in inventory!"
            }
        }
    }

    fun toggleEquip(itemId: String) {
        viewModelScope.launch {
            activeRepo.toggleEquip(itemId)
            ChiptuneAudio.playClick()
        }
    }

    fun setActivePet(petId: String?) {
        viewModelScope.launch {
            activeRepo.setActivePet(petId)
            ChiptuneAudio.playClick()
        }
    }

    fun useHealingPotion(): Boolean {
        var used = false
        viewModelScope.launch {
            used = activeRepo.useHealingPotion()
            if (used) {
                ChiptuneAudio.playHeal()
            }
        }
        return used
    }

    fun claimAchievement(id: String) {
        viewModelScope.launch {
            val success = activeRepo.claimAchievementReward(id)
            if (success) {
                ChiptuneAudio.playLevelUp()
                _infoMessage.value = "Achievement Reward Claimed!"
            }
        }
    }

    fun recordAdventureEnd(score: Int, distance: Int, coins: Int) {
        viewModelScope.launch {
            activeRepo.recordAdventureRun(score, distance, coins)
        }
    }

    fun toggleSound() {
        val next = !_soundEnabled.value
        _soundEnabled.value = next
        ChiptuneAudio.isSoundEnabled = next
    }

    fun toggleMusic() {
        val next = !_musicEnabled.value
        _musicEnabled.value = next
        ChiptuneAudio.isMusicEnabled = next
        _isPlayingMusic.value = ChiptuneAudio.isPlayingBgm
    }

    fun toggleMusicPlay() {
        val playing = ChiptuneAudio.toggleBgm()
        _isPlayingMusic.value = playing
    }

    fun setMusicVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        _musicVolume.value = v
        ChiptuneAudio.musicVolume = v
    }

    fun updateCharacterCustomization(
        name: String,
        gender: String,
        hairStyle: String,
        hairColor: String,
        skinTone: String,
        outfitColor: String,
        accessory: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                characterName = name.trim().ifEmpty { "Adventurer" },
                characterGender = gender,
                hairStyle = hairStyle,
                hairColor = hairColor,
                skinTone = skinTone,
                outfitColor = outfitColor,
                accessory = accessory
            )
            activeRepo.updateUserProfile(updated)
            _infoMessage.value = "Character updated!"
        }
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            activeRepo.completeOnboarding()
        }
    }

    fun exportData(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = activeRepo.exportDataJson()
            onResult(json)
        }
    }

    fun importData(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = activeRepo.importDataJson(json)
            onResult(success)
            if (success) {
                _infoMessage.value = "Data imported successfully!"
            } else {
                _infoMessage.value = "Invalid backup data."
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            activeRepo.resetAllData()
            _infoMessage.value = "Game data reset to fresh start."
        }
    }

    // Helper: generate 7-day strip around today
    fun getWeekDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val numFormat = SimpleDateFormat("d", Locale.getDefault())

        // Start 3 days before today to 3 days after today
        cal.add(Calendar.DAY_OF_YEAR, -3)
        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            val dayName = dayFormat.format(cal.time).uppercase()
            val dayNum = numFormat.format(cal.time)
            val isToday = dateStr == todayDateStr
            days.add(CalendarDay(dateStr, dayName, dayNum, isToday))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }
}

data class CalendarDay(
    val dateStr: String,
    val dayOfWeek: String,
    val dayNumber: String,
    val isToday: Boolean
)
