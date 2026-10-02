package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted user account credentials.
 * Passwords are NEVER stored in plaintext; only cryptographically salted PBKDF2 hashes.
 */
@Entity(
    tableName = "user_accounts",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserAccount(
    @PrimaryKey val id: String,
    val email: String,
    val adventurerName: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
