package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey val id: String,
    val name: String,
    val title: String,
    val description: String,
    val abilityDescription: String,
    val unlocked: Boolean = false,
    val active: Boolean = false
)

object PetCatalog {
    val FOREST_CAT = Pet(
        id = "pet_forest_cat",
        name = "Forest Cat",
        title = "Whispering Wanderer",
        description = "A small magical emerald forest cat with keen senses and padded paws.",
        abilityDescription = "15% chance to recover 1 HP when passing distance milestones in Adventure.",
        unlocked = false,
        active = false
    )

    val MOON_OWL = Pet(
        id = "pet_moon_owl",
        name = "Moon Owl",
        title = "Silent Scholar",
        description = "A mysterious nocturnal owl with glowing silver eyes and ancient knowledge.",
        abilityDescription = "Provides +25% bonus EXP from completed real-life quests.",
        unlocked = false,
        active = false
    )

    val MOSS_GOLEM = Pet(
        id = "pet_moss_golem",
        name = "Moss Golem",
        title = "Grove Guardian",
        description = "A tiny, sturdy earthen golem formed from river stones and damp moss.",
        abilityDescription = "20% chance to summon an earth shield and negate collision damage.",
        unlocked = false,
        active = false
    )

    val WOLF_PUP = Pet(
        id = "pet_wolf_pup",
        name = "Wolf Pup",
        title = "Loyal Scout",
        description = "A swift, spirited young fantasy wolf companion with untamed vigor.",
        abilityDescription = "Provides +25% Adventure score bonus and higher coin drop rate.",
        unlocked = false,
        active = false
    )

    val ALL_PETS = listOf(
        FOREST_CAT,
        MOON_OWL,
        MOSS_GOLEM,
        WOLF_PUP
    )
}
