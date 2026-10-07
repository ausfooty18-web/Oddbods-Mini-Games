package com.example.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.OddbodsViewModel

@Composable
fun MiniGamesMenuScreen(
    viewModel: OddbodsViewModel,
    onStartGame: (GameScreen) -> Unit
) {
    val playerState by viewModel.playerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Oddbods Mini-Games",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Pick a mini-game and unleash character powers!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                MiniGameCard(
                    title = "Fuse's Rage Smash",
                    subtitle = "Smash Boulders & Rage Dash",
                    bgColor = OddbodsFuseRed,
                    icon = Icons.Default.FlashOn,
                    highScore = playerState.fuseSmashHighScore,
                    onClick = { onStartGame(GameScreen.GAME_FUSE_SMASH) },
                    testTag = "menu_btn_fuse"
                )
            }
            item {
                MiniGameCard(
                    title = "Pogo's Prank Bounce",
                    subtitle = "Bouncy Cushions & Rocket Springs",
                    bgColor = OddbodsPogoBlue,
                    icon = Icons.Default.RocketLaunch,
                    highScore = playerState.pogoBounceHighScore,
                    highScoreUnit = "m",
                    onClick = { onStartGame(GameScreen.GAME_POGO_BOUNCE) },
                    testTag = "menu_btn_pogo"
                )
            }
            item {
                MiniGameCard(
                    title = "Bubbles' Gadget Lab",
                    subtitle = "Anti-Grav Magnet & Lasers",
                    bgColor = OddbodsBubblesGold,
                    icon = Icons.Default.AllInclusive,
                    highScore = playerState.bubblesLabHighScore,
                    onClick = { onStartGame(GameScreen.GAME_BUBBLES_LAB) },
                    testTag = "menu_btn_bubbles"
                )
            }
            item {
                MiniGameCard(
                    title = "Zee's Snack Catcher",
                    subtitle = "Falling Treats & Snooze Vortex",
                    bgColor = OddbodsZeeGreen,
                    icon = Icons.Default.Bedtime,
                    highScore = playerState.zeeCatcherHighScore,
                    onClick = { onStartGame(GameScreen.GAME_ZEE_CATCHER) },
                    testTag = "menu_btn_zee"
                )
            }
        }
    }
}
