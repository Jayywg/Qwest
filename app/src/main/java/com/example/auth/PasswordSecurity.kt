package com.example.auth

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PasswordRequirements(
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasNumber: Boolean
) {
    val isValid: Boolean get() = hasMinLength && hasUppercase && hasNumber
}

object PasswordSecurity {
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, saltHex: String): String {
        val saltBytes = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, saltHex: String, expectedHash: String): Boolean {
        return try {
            val calculated = hashPassword(password, saltHex)
            slowEquals(calculated, expectedHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun slowEquals(a: String, b: String): Boolean {
        var diff = a.length xor b.length
        val minLen = minOf(a.length, b.length)
        for (i in 0 until minLen) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }

    fun validatePassword(password: String): PasswordRequirements {
        return PasswordRequirements(
            hasMinLength = password.length >= 8,
            hasUppercase = password.any { it.isUpperCase() },
            hasNumber = password.any { it.isDigit() }
        )
    }

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() && EMAIL_REGEX.matches(trimmed)
    }

    fun isValidAdventurerName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.length in 2..24 && trimmed.all { it.isLetterOrDigit() || it == ' ' || it == '-' || it == '\'' }
    }
}
