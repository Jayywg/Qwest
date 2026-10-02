package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Qwest", appName)
  }

  @Test
  fun `user profile leveling logic calculates required exp correctly`() {
    val profileLevel1 = com.example.model.UserProfile(level = 1)
    assertEquals(100, profileLevel1.expForNextLevel())

    val profileLevel2 = com.example.model.UserProfile(level = 2)
    assertEquals(180, profileLevel2.expForNextLevel())
  }

  @Test
  fun `catalog items and pets are properly populated`() {
    val shopItems = com.example.model.GameItemCatalog.ALL_SHOP_ITEMS
    assertEquals(6, shopItems.size)

    val pets = com.example.model.PetCatalog.ALL_PETS
    assertEquals(4, pets.size)
  }

  @Test
  fun `password validation enforces 8 characters, uppercase, and number`() {
    val weak = com.example.auth.PasswordSecurity.validatePassword("pass")
    assertEquals(false, weak.isValid)

    val noUpper = com.example.auth.PasswordSecurity.validatePassword("password123")
    assertEquals(false, noUpper.hasUppercase)
    assertEquals(false, noUpper.isValid)

    val noNum = com.example.auth.PasswordSecurity.validatePassword("Password")
    assertEquals(false, noNum.hasNumber)
    assertEquals(false, noNum.isValid)

    val valid = com.example.auth.PasswordSecurity.validatePassword("Password123!")
    assertEquals(true, valid.isValid)
  }

  @Test
  fun `email validation validates format`() {
    assertEquals(true, com.example.auth.PasswordSecurity.isValidEmail("hero@qwest.com"))
    assertEquals(false, com.example.auth.PasswordSecurity.isValidEmail("invalid-email"))
    assertEquals(false, com.example.auth.PasswordSecurity.isValidEmail(""))
  }

  @Test
  fun `all required screens are reachable in ScreenDestination`() {
    val destinations = ScreenDestination.values().map { it.name }
    org.junit.Assert.assertTrue(destinations.contains("TAVERN"))
    org.junit.Assert.assertTrue(destinations.contains("QUESTS"))
    org.junit.Assert.assertTrue(destinations.contains("ADVENTURE"))
    org.junit.Assert.assertTrue(destinations.contains("SHOP"))
    org.junit.Assert.assertTrue(destinations.contains("INVENTORY"))
    org.junit.Assert.assertTrue(destinations.contains("STATS"))
    org.junit.Assert.assertTrue(destinations.contains("SETTINGS"))
  }
}
