package com.example.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordSecurityTest {

    @Test
    fun testEmailValidation() {
        assertTrue(PasswordSecurity.isValidEmail("adventurer@qwest.com"))
        assertTrue(PasswordSecurity.isValidEmail("hero.paladin@sub.domain.org"))
        assertFalse(PasswordSecurity.isValidEmail("invalid-email"))
        assertFalse(PasswordSecurity.isValidEmail("adventurer@"))
        assertFalse(PasswordSecurity.isValidEmail("@qwest.com"))
        assertFalse(PasswordSecurity.isValidEmail(""))
    }

    @Test
    fun testAdventurerNameValidation() {
        assertTrue(PasswordSecurity.isValidAdventurerName("Sir Lancelot"))
        assertTrue(PasswordSecurity.isValidAdventurerName("Val"))
        assertFalse(PasswordSecurity.isValidAdventurerName("A")) // too short
        assertFalse(PasswordSecurity.isValidAdventurerName("   ")) // empty
        assertFalse(PasswordSecurity.isValidAdventurerName("SuperExtraLongAdventurerNameThatExceedsLimit")) // > 24 chars
    }

    @Test
    fun testPasswordRequirements() {
        val weakReq = PasswordSecurity.validatePassword("short")
        assertFalse(weakReq.hasMinLength)
        assertFalse(weakReq.hasUppercase)
        assertFalse(weakReq.hasNumber)
        assertFalse(weakReq.isValid)

        val noUpperReq = PasswordSecurity.validatePassword("password123")
        assertTrue(noUpperReq.hasMinLength)
        assertFalse(noUpperReq.hasUppercase)
        assertTrue(noUpperReq.hasNumber)
        assertFalse(noUpperReq.isValid)

        val noNumberReq = PasswordSecurity.validatePassword("PasswordABC")
        assertTrue(noNumberReq.hasMinLength)
        assertTrue(noNumberReq.hasUppercase)
        assertFalse(noNumberReq.hasNumber)
        assertFalse(noNumberReq.isValid)

        val validReq = PasswordSecurity.validatePassword("TavernHero1")
        assertTrue(validReq.hasMinLength)
        assertTrue(validReq.hasUppercase)
        assertTrue(validReq.hasNumber)
        assertTrue(validReq.isValid)
    }

    @Test
    fun testPasswordHashingAndVerification() {
        val rawPassword = "SecretTavernPassword99"
        val salt = PasswordSecurity.generateSalt()
        val hash = PasswordSecurity.hashPassword(rawPassword, salt)

        // Ensure hash is not equal to raw password
        assertNotEquals(rawPassword, hash)
        assertTrue(hash.isNotEmpty())

        // Ensure correct password matches
        assertTrue(PasswordSecurity.verifyPassword(rawPassword, salt, hash))

        // Ensure incorrect password fails
        assertFalse(PasswordSecurity.verifyPassword("WrongPassword", salt, hash))
        assertFalse(PasswordSecurity.verifyPassword("secretTavernPassword99", salt, hash))
    }
}
