package com.example.auth

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.AuthDatabase
import com.example.model.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class AuthResult {
    data class Success(val session: UserSession, val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val context: Context,
    private val sessionManager: SessionManager
) {
    private val authDb = AuthDatabase.getDatabase(context)
    private val userDao = authDb.userAccountDao()

    suspend fun initializeAuth() = withContext(Dispatchers.IO) {
        // Pre-seed a default demo account if no accounts exist
        if (userDao.getUserCount() == 0) {
            val demoSalt = PasswordSecurity.generateSalt()
            val demoHash = PasswordSecurity.hashPassword("Password123!", demoSalt)
            val demoAccount = UserAccount(
                id = "usr_demo_kaelen",
                email = "adventurer@qwest.com",
                adventurerName = "Kaelen",
                passwordHash = demoHash,
                salt = demoSalt
            )
            try {
                userDao.insertUser(demoAccount)
                // Initialize demo user's database with starter data
                AppDatabase.getDatabaseForUser(context, demoAccount.id, demoAccount.adventurerName)
            } catch (ignored: Exception) {}
        }
    }

    suspend fun signUp(
        adventurerName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val trimmedName = adventurerName.trim()
        val trimmedEmail = email.trim()

        // Validation
        if (trimmedName.isEmpty()) {
            return@withContext AuthResult.Error("Your adventure needs a name.")
        }
        if (!PasswordSecurity.isValidAdventurerName(trimmedName)) {
            return@withContext AuthResult.Error("Adventurer name must be 2 to 24 characters with valid characters.")
        }
        if (!PasswordSecurity.isValidEmail(trimmedEmail)) {
            return@withContext AuthResult.Error("That email doesn't look quite right.")
        }

        val requirements = PasswordSecurity.validatePassword(password)
        if (!requirements.isValid) {
            val missing = buildList {
                if (!requirements.hasMinLength) add("at least 8 characters")
                if (!requirements.hasUppercase) add("one uppercase letter")
                if (!requirements.hasNumber) add("one number")
            }
            return@withContext AuthResult.Error("Password needs ${missing.joinToString(", ")}.")
        }

        if (password != confirmPassword) {
            return@withContext AuthResult.Error("Your passwords don't match.")
        }

        // Check if account already exists
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext AuthResult.Error("An adventurer with this email already exists.")
        }

        // Create secure hash
        val salt = PasswordSecurity.generateSalt()
        val hash = PasswordSecurity.hashPassword(password, salt)
        val newUserId = "usr_${UUID.randomUUID()}"

        val newAccount = UserAccount(
            id = newUserId,
            email = trimmedEmail.lowercase(),
            adventurerName = trimmedName,
            passwordHash = hash,
            salt = salt,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        try {
            userDao.insertUser(newAccount)
            // Initialize isolated user database
            AppDatabase.getDatabaseForUser(context, newUserId, trimmedName)
            // Save active session
            val session = sessionManager.saveSession(newUserId, trimmedEmail.lowercase(), trimmedName)
            AuthResult.Success(session, "Welcome, Adventurer! Entering the Tavern...")
        } catch (e: Exception) {
            AuthResult.Error("Could not create your adventurer account: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            return@withContext AuthResult.Error("Please enter your email and password.")
        }

        if (!PasswordSecurity.isValidEmail(trimmedEmail)) {
            return@withContext AuthResult.Error("That email doesn't look quite right.")
        }

        val user = userDao.getUserByEmail(trimmedEmail)
        if (user == null) {
            return@withContext AuthResult.Error("Invalid adventurer email or password.")
        }

        val isPasswordValid = PasswordSecurity.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordValid) {
            return@withContext AuthResult.Error("Invalid adventurer email or password.")
        }

        // Update last login
        userDao.updateUser(user.copy(lastLoginAt = System.currentTimeMillis()))

        // Save session
        val session = sessionManager.saveSession(user.id, user.email, user.adventurerName)
        AuthResult.Success(session, "Welcome back, ${user.adventurerName}!")
    }

    suspend fun sendPasswordReset(email: String): String = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (!PasswordSecurity.isValidEmail(trimmedEmail)) {
            return@withContext "That email doesn't look quite right."
        }
        // Requirement 9: Do not reveal whether a specific email address is registered
        "If an account exists with this email, we've sent instructions to reset your password."
    }

    fun signOut() {
        sessionManager.clearSession()
    }

    fun getActiveSession(): UserSession? {
        return sessionManager.getActiveSession()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
