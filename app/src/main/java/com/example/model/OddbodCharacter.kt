package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.*

data class OddbodCharacter(
    val id: String,
    val name: String,
    val suitColor: Color,
    val secondaryColor: Color,
    val tagline: String,
    val personality: String,
    val signatureAbilityName: String,
    val signatureAbilityDesc: String,
    val abilityEffectType: AbilityEffectType,
    val speedStat: Int,
    val powerStat: Int,
    val mischiefStat: Int,
    val techStat: Int,
    val spriteDrawableRes: Int?,
    val isDefaultUnlocked: Boolean = false,
    val unlockCost: Int = 0
)

enum class AbilityEffectType {
    FURY_BURST,       // Fuse: Explosive smash & flame dash
    PRANK_SPRING,     // Pogo: Mega rocket bounce & balloon burst
    MAGNETIC_PULSE,   // Bubbles: Anti-gravity magnet & gizmo zap
    SNOOZE_VORTEX,    // Zee: Snore food vacuum & snooze shield
    DISCO_GROOVE,     // Slick: Rhythm freeze & music blast
    PRECISION_CLEAN,  // Jeff: Laser vacuum & clean wipe
    CUPCAKE_RUSH      // Newt: Sugar rush & candy sprinkle
}

object OddbodsData {
    val ALL_CHARACTERS = listOf(
        OddbodCharacter(
            id = "fuse",
            name = "Fuse",
            suitColor = OddbodsFuseRed,
            secondaryColor = OddbodsFuseOrange,
            tagline = "Fiery Leader with Super Strength",
            personality = "Quick-tempered, passionate, and fiercely competitive. He loves sports and winning!",
            signatureAbilityName = "Fury Rage Smash",
            signatureAbilityDesc = "Ignites an explosive flaming frenzy! Obliterates boulders, doubles score multiplier, and becomes invincible for 6 seconds.",
            abilityEffectType = AbilityEffectType.FURY_BURST,
            speedStat = 75,
            powerStat = 98,
            mischiefStat = 60,
            techStat = 40,
            spriteDrawableRes = R.drawable.sprite_fuse,
            isDefaultUnlocked = true,
            unlockCost = 0
        ),
        OddbodCharacter(
            id = "pogo",
            name = "Pogo",
            suitColor = OddbodsPogoBlue,
            secondaryColor = OddbodsPogoCyan,
            tagline = "The Ultimate Mischief Prankster",
            personality = "Hyperactive, full of wild ideas, and loves pulling harmless pranks on all his buddies.",
            signatureAbilityName = "Mega Trick Spring",
            signatureAbilityDesc = "Deploys a giant turbo spring that launches Pogo high into the sky, bursting star balloons and raining bonus coins!",
            abilityEffectType = AbilityEffectType.PRANK_SPRING,
            speedStat = 92,
            powerStat = 55,
            mischiefStat = 99,
            techStat = 65,
            spriteDrawableRes = R.drawable.sprite_pogo,
            isDefaultUnlocked = true,
            unlockCost = 0
        ),
        OddbodCharacter(
            id = "bubbles",
            name = "Bubbles",
            suitColor = OddbodsBubblesYellow,
            secondaryColor = OddbodsBubblesGold,
            tagline = "Genius Tech Inventor & Scientist",
            personality = "Brilliant, endlessly curious, and invents quirky gadgets (some of which explode).",
            signatureAbilityName = "Anti-Grav Magnet",
            signatureAbilityDesc = "Generates an electromagnetic polarity pulse that pulls all floating energy cells and coins across the entire screen!",
            abilityEffectType = AbilityEffectType.MAGNETIC_PULSE,
            speedStat = 80,
            powerStat = 45,
            mischiefStat = 50,
            techStat = 99,
            spriteDrawableRes = R.drawable.sprite_bubbles,
            isDefaultUnlocked = true,
            unlockCost = 0
        ),
        OddbodCharacter(
            id = "zee",
            name = "Zee",
            suitColor = OddbodsZeeGreen,
            secondaryColor = OddbodsZeeLime,
            tagline = "Snack King & Nap Professional",
            personality = "Relaxed, loves taking 14-hour naps, and can eat 5 pizzas without moving an inch.",
            signatureAbilityName = "Snooze Snarf Vacuum",
            signatureAbilityDesc = "Emits a deep cartoon snore vortex that inhales every snack item in the air straight into his mouth with delicious bonuses!",
            abilityEffectType = AbilityEffectType.SNOOZE_VORTEX,
            speedStat = 35,
            powerStat = 85,
            mischiefStat = 40,
            techStat = 30,
            spriteDrawableRes = null, // Custom vector avatar
            isDefaultUnlocked = true,
            unlockCost = 0
        ),
        OddbodCharacter(
            id = "slick",
            name = "Slick",
            suitColor = OddbodsSlickOrange,
            secondaryColor = OddbodsSlickAmber,
            tagline = "Groovy DJ & Dancefloor Master",
            personality = "Cool, swaggering, obsessed with rhythm, beats, and looking stylish at all times.",
            signatureAbilityName = "Groove Beatwave",
            signatureAbilityDesc = "Blasts funky musical basswaves that freeze all hazards in groovy dance poses while awarding rhythm points!",
            abilityEffectType = AbilityEffectType.DISCO_GROOVE,
            speedStat = 88,
            powerStat = 60,
            mischiefStat = 75,
            techStat = 70,
            spriteDrawableRes = null,
            isDefaultUnlocked = false,
            unlockCost = 150
        ),
        OddbodCharacter(
            id = "jeff",
            name = "Jeff",
            suitColor = OddbodsJeffPurple,
            secondaryColor = OddbodsJeffIndigo,
            tagline = "Fastidious Perfectionist & Cleaner",
            personality = "Immaculate, orderly, adores straight lines and cannot stand even a single speck of dirt.",
            signatureAbilityName = "Ultra Polish Beam",
            signatureAbilityDesc = "Sweeps the playing field with an ultra-clean laser line, instantly purifying traps and sorting all items into neat stacks!",
            abilityEffectType = AbilityEffectType.PRECISION_CLEAN,
            speedStat = 70,
            powerStat = 65,
            mischiefStat = 30,
            techStat = 85,
            spriteDrawableRes = null,
            isDefaultUnlocked = false,
            unlockCost = 250
        ),
        OddbodCharacter(
            id = "newt",
            name = "Newt",
            suitColor = OddbodsNewtPink,
            secondaryColor = OddbodsNewtRose,
            tagline = "Sweet-Toothed & Adorable",
            personality = "Sweet, bubbly, loves candy, baking cupcakes, taking selfies, and petting cute bugs.",
            signatureAbilityName = "Sugar Rush Pop",
            signatureAbilityDesc = "Sprinkles delicious glittery rainbow frosting that speeds up gameplay with 3x multiplier and spawns rare golden treats!",
            abilityEffectType = AbilityEffectType.CUPCAKE_RUSH,
            speedStat = 85,
            powerStat = 40,
            mischiefStat = 65,
            techStat = 55,
            spriteDrawableRes = null,
            isDefaultUnlocked = false,
            unlockCost = 350
        )
    )

    fun getCharacterById(id: String): OddbodCharacter {
        return ALL_CHARACTERS.firstOrNull { it.id == id } ?: ALL_CHARACTERS[0]
    }
}
