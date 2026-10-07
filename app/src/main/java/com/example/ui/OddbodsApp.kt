package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.games.BubblesGadgetLabGame
import com.example.ui.games.FuseRageSmashGame
import com.example.ui.games.PogoPrankBounceGame
import com.example.ui.games.ZeeSnackCatcherGame
import com.example.ui.hub.AbilityShowcaseScreen
import com.example.ui.hub.CharacterHubScreen
import com.example.ui.hub.MiniGamesMenuScreen
import com.example.ui.theme.GoldCoinColor
import com.example.ui.theme.OddbodsPurplePrimary
import com.example.ui.theme.OddbodsYellowAccent
import com.example.ui.trophies.TrophyScreen
import com.example.viewmodel.GameScreen
import com.example.viewmodel.OddbodsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OddbodsApp(
    viewModel: OddbodsViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    // Determine if we are in an active full-screen mini-game
    val isPlayingMiniGame = currentScreen in listOf(
        GameScreen.GAME_FUSE_SMASH,
        GameScreen.GAME_POGO_BOUNCE,
        GameScreen.GAME_BUBBLES_LAB,
        GameScreen.GAME_ZEE_CATCHER
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                if (!isPlayingMiniGame && currentScreen != GameScreen.ABILITY_SHOWCASE) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(OddbodsYellowAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = "Oddbods",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Oddbods",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = OddbodsPurplePrimary
                        ),
                        actions = {
                            // Player coins badge
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = "Coins",
                                        tint = GoldCoinColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${playerState.coins}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Sound toggle icon
                            IconButton(
                                onClick = { viewModel.toggleSound() },
                                modifier = Modifier.testTag("btn_top_sound_toggle")
                            ) {
                                Icon(
                                    imageVector = if (playerState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Sound Toggle",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!isPlayingMiniGame && currentScreen != GameScreen.ABILITY_SHOWCASE && !isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == GameScreen.HOME_HUB,
                            onClick = { viewModel.navigateTo(GameScreen.HOME_HUB) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Hub") },
                            modifier = Modifier.testTag("nav_tab_hub")
                        )
                        NavigationBarItem(
                            selected = currentScreen == GameScreen.MINI_GAMES_MENU,
                            onClick = { viewModel.navigateTo(GameScreen.MINI_GAMES_MENU) },
                            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Games") },
                            label = { Text("Games") },
                            modifier = Modifier.testTag("nav_tab_games")
                        )
                        NavigationBarItem(
                            selected = currentScreen == GameScreen.ABILITY_SHOWCASE,
                            onClick = { viewModel.navigateTo(GameScreen.ABILITY_SHOWCASE) },
                            icon = { Icon(Icons.Default.Bolt, contentDescription = "Abilities") },
                            label = { Text("Abilities") },
                            modifier = Modifier.testTag("nav_tab_abilities")
                        )
                        NavigationBarItem(
                            selected = currentScreen == GameScreen.TROPHIES,
                            onClick = { viewModel.navigateTo(GameScreen.TROPHIES) },
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Trophies") },
                            label = { Text("Trophies") },
                            modifier = Modifier.testTag("nav_tab_trophies")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Wide Screen Navigation Rail for Tablets
                if (!isPlayingMiniGame && currentScreen != GameScreen.ABILITY_SHOWCASE && isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationRailItem(
                            selected = currentScreen == GameScreen.HOME_HUB,
                            onClick = { viewModel.navigateTo(GameScreen.HOME_HUB) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Hub") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == GameScreen.MINI_GAMES_MENU,
                            onClick = { viewModel.navigateTo(GameScreen.MINI_GAMES_MENU) },
                            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Games") },
                            label = { Text("Games") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == GameScreen.ABILITY_SHOWCASE,
                            onClick = { viewModel.navigateTo(GameScreen.ABILITY_SHOWCASE) },
                            icon = { Icon(Icons.Default.Bolt, contentDescription = "Abilities") },
                            label = { Text("Abilities") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == GameScreen.TROPHIES,
                            onClick = { viewModel.navigateTo(GameScreen.TROPHIES) },
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Trophies") },
                            label = { Text("Trophies") }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_crossfade"
                    ) { target ->
                        when (target) {
                            GameScreen.HOME_HUB -> {
                                CharacterHubScreen(
                                    viewModel = viewModel,
                                    onStartGame = { screen -> viewModel.navigateTo(screen) },
                                    onOpenAbilityLab = { viewModel.navigateTo(GameScreen.ABILITY_SHOWCASE) }
                                )
                            }
                            GameScreen.MINI_GAMES_MENU -> {
                                MiniGamesMenuScreen(
                                    viewModel = viewModel,
                                    onStartGame = { screen -> viewModel.navigateTo(screen) }
                                )
                            }
                            GameScreen.GAME_FUSE_SMASH -> {
                                FuseRageSmashGame(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(GameScreen.HOME_HUB) }
                                )
                            }
                            GameScreen.GAME_POGO_BOUNCE -> {
                                PogoPrankBounceGame(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(GameScreen.HOME_HUB) }
                                )
                            }
                            GameScreen.GAME_BUBBLES_LAB -> {
                                BubblesGadgetLabGame(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(GameScreen.HOME_HUB) }
                                )
                            }
                            GameScreen.GAME_ZEE_CATCHER -> {
                                ZeeSnackCatcherGame(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(GameScreen.HOME_HUB) }
                                )
                            }
                            GameScreen.ABILITY_SHOWCASE -> {
                                AbilityShowcaseScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.navigateTo(GameScreen.HOME_HUB) }
                                )
                            }
                            GameScreen.TROPHIES -> {
                                TrophyScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
