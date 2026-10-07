package com.example.ui.trophies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.OddbodsViewModel

data class GameTrophy(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val rewardCoins: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrophyScreen(
    viewModel: OddbodsViewModel
) {
    val playerState by viewModel.playerState.collectAsState()

    val trophies = remember(playerState) {
        listOf(
            GameTrophy(
                id = "first_smash",
                title = "Rock Smasher",
                description = "Smash 10 boulders with Fuse",
                isUnlocked = playerState.totalSmashes >= 10,
                rewardCoins = 30,
                icon = Icons.Default.FlashOn,
                accentColor = OddbodsFuseRed
            ),
            GameTrophy(
                id = "sky_high",
                title = "Sky Bouncer",
                description = "Reach 250m altitude with Pogo",
                isUnlocked = playerState.highestBounce >= 250,
                rewardCoins = 40,
                icon = Icons.Default.RocketLaunch,
                accentColor = OddbodsPogoBlue
            ),
            GameTrophy(
                id = "tech_whiz",
                title = "Gadget Whiz",
                description = "Collect 20 battery cells with Bubbles",
                isUnlocked = playerState.totalGadgets >= 20,
                rewardCoins = 35,
                icon = Icons.Default.AllInclusive,
                accentColor = OddbodsBubblesGold
            ),
            GameTrophy(
                id = "snack_master",
                title = "Midnight Snacker",
                description = "Catch 25 delicious snacks with Zee",
                isUnlocked = playerState.totalSnacks >= 25,
                rewardCoins = 30,
                icon = Icons.Default.Bedtime,
                accentColor = OddbodsZeeGreen
            ),
            GameTrophy(
                id = "crew_gatherer",
                title = "Oddbods Collector",
                description = "Unlock at least 5 Oddbods characters",
                isUnlocked = playerState.unlockedCharacterIds.size >= 5,
                rewardCoins = 60,
                icon = Icons.Default.Groups,
                accentColor = OddbodsJeffPurple
            ),
            GameTrophy(
                id = "coin_tycoon",
                title = "Coin Tycoon",
                description = "Amass 150 total Oddbods coins",
                isUnlocked = playerState.coins >= 150,
                rewardCoins = 50,
                icon = Icons.Default.MonetizationOn,
                accentColor = GoldCoinColor
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Trophies & Statistics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Complete achievements to earn coins!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Lifetime Stats Overview Grid
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIFETIME GAME STATS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatTile(label = "Boulders Smashed", value = "${playerState.totalSmashes}", color = OddbodsFuseRed)
                        StatTile(label = "Highest Bounce", value = "${playerState.highestBounce}m", color = OddbodsPogoBlue)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatTile(label = "Gadgets Harvested", value = "${playerState.totalGadgets}", color = TechCyan)
                        StatTile(label = "Snacks Eaten", value = "${playerState.totalSnacks}", color = OddbodsZeeLime)
                    }
                }
            }
        }

        // Audio & Haptics Settings
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIO & FEEDBACK SETTINGS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = OddbodsPurpleSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Game Sound Effects", fontWeight = FontWeight.Medium)
                        }
                        Switch(
                            checked = playerState.isSoundEnabled,
                            onCheckedChange = { viewModel.toggleSound() },
                            modifier = Modifier.testTag("switch_sound")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = OddbodsFuseOrange)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Haptic Vibrations", fontWeight = FontWeight.Medium)
                        }
                        Switch(
                            checked = playerState.isHapticsEnabled,
                            onCheckedChange = { viewModel.toggleHaptics() },
                            modifier = Modifier.testTag("switch_haptics")
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Trophy Showcase (${trophies.count { it.isUnlocked }}/${trophies.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Trophy Items
        items(trophies) { trophy ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (trophy.isUnlocked) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ),
                border = if (trophy.isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, trophy.accentColor.copy(alpha = 0.5f)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                if (trophy.isUnlocked) trophy.accentColor else Color.Gray.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = trophy.icon,
                            contentDescription = null,
                            tint = if (trophy.isUnlocked) Color.White else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trophy.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (trophy.isUnlocked) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = trophy.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    if (trophy.isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = OddbodsZeeLime,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GoldCoinColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "+${trophy.rewardCoins}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GoldCoinColor
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatTile(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
