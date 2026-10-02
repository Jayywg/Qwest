package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

data class UserSession(
    val userId: String,
    val email: String,
    val adventurerName: String,
    val sessionToken: String
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("qwest_auth_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "active_user_id"
        private const val KEY_EMAIL = "active_email"
        private const val KEY_NAME = "active_adventurer_name"
        private const val KEY_TOKEN = "session_token"
        private const val KEY_LOGIN_TIME = "login_time"
    }

    fun saveSession(userId: String, email: String, adventurerName: String): UserSession {
        val token = UUID.randomUUID().toString()
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, adventurerName)
            .putString(KEY_TOKEN, token)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .apply()
        return UserSession(userId, email, adventurerName, token)
    }

    fun getActiveSession(): UserSession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_NAME, "Adventurer") ?: "Adventurer"
        val token = prefs.getString(KEY_TOKEN, "") ?: ""
        return UserSession(userId, email, name, token)
    }

    fun isLoggedIn(): Boolean {
        val userId = prefs.getString(KEY_USER_ID, null)
        return !userId.isNullOrBlank()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
