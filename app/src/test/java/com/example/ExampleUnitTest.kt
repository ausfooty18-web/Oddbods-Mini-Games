package com.example

import com.example.model.OddbodsData
import com.example.model.AbilityEffectType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun oddbodsCharacters_areLoadedCorrectly() {
        val characters = OddbodsData.ALL_CHARACTERS
        assertEquals(7, characters.size)

        val fuse = OddbodsData.getCharacterById("fuse")
        assertEquals("Fuse", fuse.name)
        assertEquals(AbilityEffectType.FURY_BURST, fuse.abilityEffectType)
        assertTrue(fuse.isDefaultUnlocked)

        val pogo = OddbodsData.getCharacterById("pogo")
        assertEquals("Pogo", pogo.name)
        assertEquals(AbilityEffectType.PRANK_SPRING, pogo.abilityEffectType)

        val bubbles = OddbodsData.getCharacterById("bubbles")
        assertEquals("Bubbles", bubbles.name)
        assertEquals(AbilityEffectType.MAGNETIC_PULSE, bubbles.abilityEffectType)

        val zee = OddbodsData.getCharacterById("zee")
        assertEquals("Zee", zee.name)
        assertEquals(AbilityEffectType.SNOOZE_VORTEX, zee.abilityEffectType)
    }

    @Test
    fun characterAbilities_areUnique() {
        val abilities = OddbodsData.ALL_CHARACTERS.map { it.abilityEffectType }
        val uniqueAbilities = abilities.toSet()
        assertEquals(OddbodsData.ALL_CHARACTERS.size, uniqueAbilities.size)
    }
}

