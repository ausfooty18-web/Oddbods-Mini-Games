package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("oddbods_game_prefs", Context.MODE_PRIVATE)

    var coins: Int
        get() = prefs.getInt("player_coins", 50) // starts with 50 coins bonus
        set(value) = prefs.edit().putInt("player_coins", value).apply()

    var selectedCharacterId: String
        get() = prefs.getString("selected_char_id", "fuse") ?: "fuse"
        set(value) = prefs.edit().putString("selected_char_id", value).apply()

    fun getUnlockedCharacters(): Set<String> {
        return prefs.getStringSet("unlocked_chars", setOf("fuse", "pogo", "bubbles", "zee"))
            ?: setOf("fuse", "pogo", "bubbles", "zee")
    }

    fun unlockCharacter(id: String) {
        val current = getUnlockedCharacters().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("unlocked_chars", current).apply()
    }

    fun isCharacterUnlocked(id: String): Boolean {
        return getUnlockedCharacters().contains(id)
    }

    // High scores
    var fuseSmashHighScore: Int
        get() = prefs.getInt("high_score_fuse", 0)
        set(value) = prefs.edit().putInt("high_score_fuse", value).apply()

    var pogoBounceHighScore: Int
        get() = prefs.getInt("high_score_pogo", 0)
        set(value) = prefs.edit().putInt("high_score_pogo", value).apply()

    var bubblesLabHighScore: Int
        get() = prefs.getInt("high_score_bubbles", 0)
        set(value) = prefs.edit().putInt("high_score_bubbles", value).apply()

    var zeeCatcherHighScore: Int
        get() = prefs.getInt("high_score_zee", 0)
        set(value) = prefs.edit().putInt("high_score_zee", value).apply()

    // Lifetime stats
    var totalSmashes: Int
        get() = prefs.getInt("stat_total_smashes", 0)
        set(value) = prefs.edit().putInt("stat_total_smashes", value).apply()

    var highestBounceMeters: Int
        get() = prefs.getInt("stat_highest_bounce", 0)
        set(value) = prefs.edit().putInt("stat_highest_bounce", value).apply()

    var totalGadgetsCollected: Int
        get() = prefs.getInt("stat_total_gadgets", 0)
        set(value) = prefs.edit().putInt("stat_total_gadgets", value).apply()

    var totalSnacksEaten: Int
        get() = prefs.getInt("stat_total_snacks", 0)
        set(value) = prefs.edit().putInt("stat_total_snacks", value).apply()

    var totalGamesPlayed: Int
        get() = prefs.getInt("stat_total_games", 0)
        set(value) = prefs.edit().putInt("stat_total_games", value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("setting_sound", true)
        set(value) = prefs.edit().putBoolean("setting_sound", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("setting_haptics", true)
        set(value) = prefs.edit().putBoolean("setting_haptics", value).apply()

    // Achievements
    fun isAchievementUnlocked(id: String): Boolean {
        return prefs.getBoolean("ach_$id", false)
    }

    fun unlockAchievement(id: String) {
        prefs.edit().putBoolean("ach_$id", true).apply()
    }
}
