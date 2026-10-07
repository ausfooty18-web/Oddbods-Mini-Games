package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.audio.SoundEffects
import com.example.data.GamePreferences
import com.example.model.OddbodCharacter
import com.example.model.OddbodsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class GameScreen {
    HOME_HUB,
    MINI_GAMES_MENU,
    GAME_FUSE_SMASH,
    GAME_POGO_BOUNCE,
    GAME_BUBBLES_LAB,
    GAME_ZEE_CATCHER,
    ABILITY_SHOWCASE,
    TROPHIES
}

data class PlayerState(
    val coins: Int = 50,
    val selectedCharacter: OddbodCharacter = OddbodsData.ALL_CHARACTERS[0],
    val unlockedCharacterIds: Set<String> = setOf("fuse", "pogo", "bubbles", "zee"),
    val fuseSmashHighScore: Int = 0,
    val pogoBounceHighScore: Int = 0,
    val bubblesLabHighScore: Int = 0,
    val zeeCatcherHighScore: Int = 0,
    val totalSmashes: Int = 0,
    val highestBounce: Int = 0,
    val totalGadgets: Int = 0,
    val totalSnacks: Int = 0,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true
)

class OddbodsViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = GamePreferences(application)
    val soundEffects = SoundEffects(application)

    private val _currentScreen = MutableStateFlow<GameScreen>(GameScreen.HOME_HUB)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        loadStateFromPrefs()
    }

    private fun loadStateFromPrefs() {
        val selectedId = prefs.selectedCharacterId
        val character = OddbodsData.getCharacterById(selectedId)
        val unlocked = prefs.getUnlockedCharacters()
        soundEffects.isSoundEnabled = prefs.soundEnabled
        soundEffects.isHapticsEnabled = prefs.hapticsEnabled

        _playerState.value = PlayerState(
            coins = prefs.coins,
            selectedCharacter = character,
            unlockedCharacterIds = unlocked,
            fuseSmashHighScore = prefs.fuseSmashHighScore,
            pogoBounceHighScore = prefs.pogoBounceHighScore,
            bubblesLabHighScore = prefs.bubblesLabHighScore,
            zeeCatcherHighScore = prefs.zeeCatcherHighScore,
            totalSmashes = prefs.totalSmashes,
            highestBounce = prefs.highestBounceMeters,
            totalGadgets = prefs.totalGadgetsCollected,
            totalSnacks = prefs.totalSnacksEaten,
            isSoundEnabled = prefs.soundEnabled,
            isHapticsEnabled = prefs.hapticsEnabled
        )
    }

    fun navigateTo(screen: GameScreen) {
        soundEffects.playBoing()
        _currentScreen.value = screen
    }

    fun selectCharacter(character: OddbodCharacter) {
        if (_playerState.value.unlockedCharacterIds.contains(character.id)) {
            prefs.selectedCharacterId = character.id
            _playerState.update { it.copy(selectedCharacter = character) }
            soundEffects.playBoing()
        }
    }

    fun unlockCharacter(character: OddbodCharacter): Boolean {
        if (_playerState.value.coins >= character.unlockCost) {
            val newCoins = _playerState.value.coins - character.unlockCost
            prefs.coins = newCoins
            prefs.unlockCharacter(character.id)
            prefs.selectedCharacterId = character.id
            soundEffects.playAbilityTrigger()

            _playerState.update {
                it.copy(
                    coins = newCoins,
                    selectedCharacter = character,
                    unlockedCharacterIds = prefs.getUnlockedCharacters()
                )
            }
            return true
        } else {
            soundEffects.playGameOver()
            return false
        }
    }

    fun addCoins(amount: Int) {
        val newCoins = _playerState.value.coins + amount
        prefs.coins = newCoins
        _playerState.update { it.copy(coins = newCoins) }
        soundEffects.playCoin()
    }

    fun recordFuseGameScore(score: Int, smashes: Int) {
        val newHigh = if (score > prefs.fuseSmashHighScore) {
            prefs.fuseSmashHighScore = score
            score
        } else prefs.fuseSmashHighScore

        val totalSmashes = prefs.totalSmashes + smashes
        prefs.totalSmashes = totalSmashes
        prefs.totalGamesPlayed += 1

        val coinsEarned = (score / 15).coerceAtLeast(2)
        addCoins(coinsEarned)

        _playerState.update {
            it.copy(
                fuseSmashHighScore = newHigh,
                totalSmashes = totalSmashes
            )
        }
    }

    fun recordPogoGameScore(height: Int, balloons: Int) {
        val newHigh = if (height > prefs.pogoBounceHighScore) {
            prefs.pogoBounceHighScore = height
            height
        } else prefs.pogoBounceHighScore

        val highestBounce = if (height > prefs.highestBounceMeters) {
            prefs.highestBounceMeters = height
            height
        } else prefs.highestBounceMeters

        prefs.totalGamesPlayed += 1
        val coinsEarned = (height / 20).coerceAtLeast(3) + balloons
        addCoins(coinsEarned)

        _playerState.update {
            it.copy(
                pogoBounceHighScore = newHigh,
                highestBounce = highestBounce
            )
        }
    }

    fun recordBubblesGameScore(score: Int, gadgets: Int) {
        val newHigh = if (score > prefs.bubblesLabHighScore) {
            prefs.bubblesLabHighScore = score
            score
        } else prefs.bubblesLabHighScore

        val totalGadgets = prefs.totalGadgetsCollected + gadgets
        prefs.totalGadgetsCollected = totalGadgets
        prefs.totalGamesPlayed += 1

        val coinsEarned = (score / 20).coerceAtLeast(3)
        addCoins(coinsEarned)

        _playerState.update {
            it.copy(
                bubblesLabHighScore = newHigh,
                totalGadgets = totalGadgets
            )
        }
    }

    fun recordZeeGameScore(score: Int, snacks: Int) {
        val newHigh = if (score > prefs.zeeCatcherHighScore) {
            prefs.zeeCatcherHighScore = score
            score
        } else prefs.zeeCatcherHighScore

        val totalSnacks = prefs.totalSnacksEaten + snacks
        prefs.totalSnacksEaten = totalSnacks
        prefs.totalGamesPlayed += 1

        val coinsEarned = (score / 18).coerceAtLeast(3)
        addCoins(coinsEarned)

        _playerState.update {
            it.copy(
                zeeCatcherHighScore = newHigh,
                totalSnacks = totalSnacks
            )
        }
    }

    fun toggleSound() {
        val newState = !prefs.soundEnabled
        prefs.soundEnabled = newState
        soundEffects.isSoundEnabled = newState
        _playerState.update { it.copy(isSoundEnabled = newState) }
        if (newState) soundEffects.playBoing()
    }

    fun toggleHaptics() {
        val newState = !prefs.hapticsEnabled
        prefs.hapticsEnabled = newState
        soundEffects.isHapticsEnabled = newState
        _playerState.update { it.copy(isHapticsEnabled = newState) }
    }

    override fun onCleared() {
        super.onCleared()
        soundEffects.release()
    }
}
