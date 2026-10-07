package com.example.ui.hub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.OddbodCharacter
import com.example.model.OddbodsData
import com.example.ui.components.OddbodCharacterView
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.OddbodsViewModel

@Composable
fun CharacterHubScreen(
    viewModel: OddbodsViewModel,
    onStartGame: (GameScreen) -> Unit,
    onOpenAbilityLab: () -> Unit
) {
    val playerState by viewModel.playerState.collectAsState()
    val allCharacters = remember { OddbodsData.ALL_CHARACTERS }
    val selectedCharacter = playerState.selectedCharacter

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Banner with Oddbods Crew
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_oddbods_crew),
                contentDescription = "Oddbods Crew Banner",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
                contentScale = ContentScale.Crop
            )

            // Gradient tint on bottom of banner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 100f
                        )
                    )
            )

            // Hero Title Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "ODDSVILLE ADVENTURES",
                    color = OddbodsYellowAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Oddbods Mini-Games",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mini-Games Quick Play Carousel
        Text(
            text = "Featured Mini-Games",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "Play with unique character abilities!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MiniGameCard(
                    title = "Fuse's Rage Smash",
                    subtitle = "Obstacle Runner & Super Fury",
                    bgColor = OddbodsFuseRed,
                    icon = Icons.Default.FlashOn,
                    highScore = playerState.fuseSmashHighScore,
                    onClick = { onStartGame(GameScreen.GAME_FUSE_SMASH) },
                    testTag = "card_fuse_game"
                )
            }
            item {
                MiniGameCard(
                    title = "Pogo's Prank Bounce",
                    subtitle = "Vertical Platformer & Springs",
                    bgColor = OddbodsPogoBlue,
                    icon = Icons.Default.RocketLaunch,
                    highScore = playerState.pogoBounceHighScore,
                    highScoreUnit = "m",
                    onClick = { onStartGame(GameScreen.GAME_POGO_BOUNCE) },
                    testTag = "card_pogo_game"
                )
            }
            item {
                MiniGameCard(
                    title = "Bubbles' Gadget Lab",
                    subtitle = "Anti-Grav Runner & Magnet",
                    bgColor = OddbodsBubblesGold,
                    icon = Icons.Default.AllInclusive,
                    highScore = playerState.bubblesLabHighScore,
                    onClick = { onStartGame(GameScreen.GAME_BUBBLES_LAB) },
                    testTag = "card_bubbles_game"
                )
            }
            item {
                MiniGameCard(
                    title = "Zee's Snack Catcher",
                    subtitle = "Food Rush & Snooze Vortex",
                    bgColor = OddbodsZeeGreen,
                    icon = Icons.Default.Bedtime,
                    highScore = playerState.zeeCatcherHighScore,
                    onClick = { onStartGame(GameScreen.GAME_ZEE_CATCHER) },
                    testTag = "card_zee_game"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Character Roster Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Oddbods Crew Roster",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap to choose your active buddy",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Ability Lab shortcut button
            FilledTonalButton(
                onClick = onOpenAbilityLab,
                modifier = Modifier.testTag("btn_hub_ability_lab"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ability Lab", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Character Selection Cards
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allCharacters) { character ->
                val isSelected = character.id == selectedCharacter.id
                val isUnlocked = playerState.unlockedCharacterIds.contains(character.id)

                Card(
                    modifier = Modifier
                        .width(135.dp)
                        .clickable {
                            if (isUnlocked) {
                                viewModel.selectCharacter(character)
                            }
                        }
                        .testTag("hub_char_card_${character.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) character.suitColor.copy(alpha = 0.9f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, character.secondaryColor) else null
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            OddbodCharacterView(
                                character = character,
                                size = 80.dp,
                                animate = isSelected
                            )
                            if (!isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = GoldCoinColor)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = character.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        if (!isUnlocked) {
                            Button(
                                onClick = { viewModel.unlockCharacter(character) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .testTag("btn_unlock_${character.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldCoinColor),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${character.unlockCost}",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = OddbodsBubblesGold
                            )
                        } else {
                            Text(
                                text = "UNLOCKED",
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Selected Character Spotlight Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OddbodCharacterView(
                        character = selectedCharacter,
                        size = 70.dp,
                        animate = true
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedCharacter.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = selectedCharacter.suitColor
                        )
                        Text(
                            text = selectedCharacter.tagline,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = selectedCharacter.personality,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = selectedCharacter.suitColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = selectedCharacter.suitColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Special: ${selectedCharacter.signatureAbilityName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = selectedCharacter.suitColor
                            )
                            Text(
                                text = selectedCharacter.signatureAbilityDesc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MiniGameCard(
    title: String,
    subtitle: String,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highScore: Int,
    highScoreUnit: String = "pts",
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .width(175.dp)
            .height(180.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BEST",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "$highScore $highScoreUnit",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldCoinColor
                )
            }
        }
    }
}
