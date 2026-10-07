package com.example.ui.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OddbodsData
import com.example.ui.components.OddbodCharacterView
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.OddbodsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

enum class ObstacleType {
    BOULDER,    // Smashable (+100 pts)
    CRATE,      // Smashable (+50 pts)
    SPIKE_TRAP, // Hazard (-1 life, unless Super Rage)
    FLAME_ORB,  // Fills Rage (+25%)
    COIN        // Gives +1 coin
}

data class LaneObstacle(
    val id: Long,
    val lane: Int, // 0, 1, 2
    var yPos: Float, // 0.0 (top) to 1.0 (bottom)
    val type: ObstacleType
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val life: Float // 1.0 to 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuseRageSmashGame(
    viewModel: OddbodsViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val fuse = remember { OddbodsData.getCharacterById("fuse") }

    // Game states
    var currentLane by remember { mutableIntStateOf(1) } // 0: Left, 1: Center, 2: Right
    var score by remember { mutableIntStateOf(0) }
    var smashesCount by remember { mutableIntStateOf(0) }
    var coinsCollected by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var rageMeter by remember { mutableFloatStateOf(0.2f) } // 0.0 to 1.0
    var isSuperRageActive by remember { mutableStateOf(false) }
    var superRageTimeRemaining by remember { mutableFloatStateOf(0f) }
    var isGameOver by remember { mutableStateOf(false) }
    var combo by remember { mutableIntStateOf(0) }

    val obstacles = remember { mutableStateListOf<LaneObstacle>() }
    val particles = remember { mutableStateListOf<Particle>() }

    var punchAnimTrigger by remember { mutableStateOf(false) }

    // Rage Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "fury_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Game loop
    LaunchedEffect(isGameOver) {
        if (isGameOver) return@LaunchedEffect

        var obstacleCounter = 0L
        var spawnTimer = 0

        while (isActive && !isGameOver) {
            delay(32) // ~30 fps tick

            // Update particles
            val deadParticles = mutableListOf<Particle>()
            for (i in particles.indices) {
                val p = particles[i]
                val newLife = p.life - 0.06f
                if (newLife <= 0f) {
                    deadParticles.add(p)
                } else {
                    particles[i] = p.copy(
                        x = p.x + p.vx,
                        y = p.y + p.vy,
                        life = newLife
                    )
                }
            }
            particles.removeAll(deadParticles)

            // Super rage duration
            if (isSuperRageActive) {
                superRageTimeRemaining -= 0.032f
                if (superRageTimeRemaining <= 0f) {
                    isSuperRageActive = false
                    rageMeter = 0f
                }
            }

            // Spawn obstacles
            spawnTimer++
            val spawnRate = if (isSuperRageActive) 16 else (32 - (score / 300).coerceAtMost(16))
            if (spawnTimer >= spawnRate) {
                spawnTimer = 0
                val lane = Random.nextInt(3)
                val typeRoll = Random.nextInt(100)
                val type = when {
                    typeRoll < 40 -> ObstacleType.BOULDER
                    typeRoll < 65 -> ObstacleType.CRATE
                    typeRoll < 80 -> ObstacleType.SPIKE_TRAP
                    typeRoll < 90 -> ObstacleType.FLAME_ORB
                    else -> ObstacleType.COIN
                }
                obstacles.add(LaneObstacle(obstacleCounter++, lane, 0f, type))
            }

            // Move and collide obstacles
            val speed = if (isSuperRageActive) 0.045f else 0.022f + (score / 2000f).coerceAtMost(0.02f)
            val toRemove = mutableListOf<LaneObstacle>()

            for (obs in obstacles) {
                obs.yPos += speed

                // Check collision at player position (~y = 0.82)
                if (obs.yPos in 0.72f..0.88f && obs.lane == currentLane) {
                    if (isSuperRageActive) {
                        // Obliterate everything in super rage!
                        smashesCount++
                        score += 200
                        viewModel.soundEffects.playSmash()
                        // Spawn explosion particles
                        repeat(12) {
                            particles.add(
                                Particle(
                                    x = obs.lane * 0.33f + 0.16f,
                                    y = 0.8f,
                                    vx = (Random.nextFloat() - 0.5f) * 0.04f,
                                    vy = (Random.nextFloat() - 0.5f) * 0.04f,
                                    color = if (Random.nextBoolean()) RageFireRed else OddbodsBubblesGold,
                                    life = 1f
                                )
                            )
                        }
                        toRemove.add(obs)
                    } else {
                        when (obs.type) {
                            ObstacleType.COIN -> {
                                coinsCollected++
                                score += 30
                                viewModel.soundEffects.playCoin()
                                toRemove.add(obs)
                            }
                            ObstacleType.FLAME_ORB -> {
                                rageMeter = (rageMeter + 0.3f).coerceAtMost(1.0f)
                                score += 50
                                viewModel.soundEffects.playZap()
                                toRemove.add(obs)
                            }
                            ObstacleType.SPIKE_TRAP -> {
                                lives--
                                combo = 0
                                viewModel.soundEffects.playGameOver()
                                toRemove.add(obs)
                                if (lives <= 0) {
                                    isGameOver = true
                                    viewModel.recordFuseGameScore(score, smashesCount)
                                }
                            }
                            ObstacleType.BOULDER, ObstacleType.CRATE -> {
                                // Hit without smashing
                                lives--
                                combo = 0
                                viewModel.soundEffects.playGameOver()
                                toRemove.add(obs)
                                if (lives <= 0) {
                                    isGameOver = true
                                    viewModel.recordFuseGameScore(score, smashesCount)
                                }
                            }
                        }
                    }
                } else if (obs.yPos > 1.05f) {
                    toRemove.add(obs)
                }
            }
            obstacles.removeAll(toRemove)
        }
    }

    // Function to perform Smash Punch
    fun performSmash() {
        if (isGameOver) return
        punchAnimTrigger = true
        viewModel.soundEffects.playSmash()

        // Check if obstacle is close in current lane to smash
        val target = obstacles.firstOrNull { it.lane == currentLane && it.yPos in 0.55f..0.85f }
        if (target != null) {
            when (target.type) {
                ObstacleType.BOULDER -> {
                    smashesCount++
                    combo++
                    val points = 100 + combo * 20
                    score += points
                    rageMeter = (rageMeter + 0.15f).coerceAtMost(1.0f)
                    // Fire explosion particles
                    repeat(14) {
                        particles.add(
                            Particle(
                                x = currentLane * 0.33f + 0.16f,
                                y = 0.75f,
                                vx = (Random.nextFloat() - 0.5f) * 0.05f,
                                vy = (Random.nextFloat() - 0.5f) * 0.05f,
                                color = OddbodsFuseOrange,
                                life = 1f
                            )
                        )
                    }
                    obstacles.remove(target)
                }
                ObstacleType.CRATE -> {
                    smashesCount++
                    combo++
                    score += 60 + combo * 10
                    rageMeter = (rageMeter + 0.12f).coerceAtMost(1.0f)
                    repeat(8) {
                        particles.add(
                            Particle(
                                x = currentLane * 0.33f + 0.16f,
                                y = 0.75f,
                                vx = (Random.nextFloat() - 0.5f) * 0.04f,
                                vy = (Random.nextFloat() - 0.5f) * 0.04f,
                                color = Color(0xFF8D6E63),
                                life = 1f
                            )
                        )
                    }
                    obstacles.remove(target)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fuse's Rage Smash",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isSuperRageActive) "🔥 SUPER FURY DASH ACTIVE! (2X)" else "Smash boulders & charge rage!",
                            fontSize = 12.sp,
                            color = if (isSuperRageActive) GoldCoinColor else Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_fuse")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isSuperRageActive) RageFireRed else OddbodsFuseRed
                ),
                actions = {
                    // Lives counter
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(lives.coerceAtLeast(0)) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Life",
                                tint = Color.Yellow,
                                modifier = Modifier.size(20.dp).padding(horizontal = 1.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = if (isSuperRageActive) {
                            listOf(Color(0xFF800E13), Color(0xFF38040E), Color(0xFF1E0207))
                        } else {
                            listOf(Color(0xFF2C1B18), Color(0xFF1F1412), Color(0xFF130C0B))
                        }
                    )
                )
        ) {
            // Lane Track and Game Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw 3 lanes dividers
                val laneWidth = w / 3f
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(laneWidth, 0f),
                    end = Offset(laneWidth, h),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(laneWidth * 2f, 0f),
                    end = Offset(laneWidth * 2f, h),
                    strokeWidth = 3f
                )

                // Draw obstacles
                for (obs in obstacles) {
                    val obsX = obs.lane * laneWidth + laneWidth * 0.5f
                    val obsY = obs.yPos * h
                    val obsRadius = laneWidth * 0.28f

                    when (obs.type) {
                        ObstacleType.BOULDER -> {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF78909C), Color(0xFF37474F)),
                                    center = Offset(obsX - obsRadius * 0.3f, obsY - obsRadius * 0.3f),
                                    radius = obsRadius
                                ),
                                radius = obsRadius,
                                center = Offset(obsX, obsY)
                            )
                            // Cracks detail
                            drawLine(
                                color = Color.Black.copy(alpha = 0.6f),
                                start = Offset(obsX - obsRadius * 0.4f, obsY - obsRadius * 0.2f),
                                end = Offset(obsX + obsRadius * 0.3f, obsY + obsRadius * 0.4f),
                                strokeWidth = 3f
                            )
                        }
                        ObstacleType.CRATE -> {
                            drawRoundRect(
                                color = Color(0xFF8D6E63),
                                topLeft = Offset(obsX - obsRadius, obsY - obsRadius),
                                size = Size(obsRadius * 2, obsRadius * 2),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                            )
                            drawRoundRect(
                                color = Color(0xFFD7CCC8),
                                topLeft = Offset(obsX - obsRadius * 0.7f, obsY - obsRadius * 0.7f),
                                size = Size(obsRadius * 1.4f, obsRadius * 1.4f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                        }
                        ObstacleType.SPIKE_TRAP -> {
                            drawCircle(
                                color = Color(0xFFD32F2F),
                                radius = obsRadius * 0.8f,
                                center = Offset(obsX, obsY)
                            )
                            // Warning symbol
                            drawLine(
                                color = Color.White,
                                start = Offset(obsX - obsRadius * 0.4f, obsY - obsRadius * 0.4f),
                                end = Offset(obsX + obsRadius * 0.4f, obsY + obsRadius * 0.4f),
                                strokeWidth = 5f
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(obsX + obsRadius * 0.4f, obsY - obsRadius * 0.4f),
                                end = Offset(obsX - obsRadius * 0.4f, obsY + obsRadius * 0.4f),
                                strokeWidth = 5f
                            )
                        }
                        ObstacleType.FLAME_ORB -> {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(OddbodsFuseOrange, RageFireRed, Color.Transparent),
                                    center = Offset(obsX, obsY),
                                    radius = obsRadius * 1.2f
                                ),
                                radius = obsRadius * 1.2f,
                                center = Offset(obsX, obsY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = obsRadius * 0.4f,
                                center = Offset(obsX, obsY)
                            )
                        }
                        ObstacleType.COIN -> {
                            drawCircle(
                                color = GoldCoinColor,
                                radius = obsRadius * 0.7f,
                                center = Offset(obsX, obsY)
                            )
                            drawCircle(
                                color = Color(0xFFFF8F00),
                                radius = obsRadius * 0.5f,
                                center = Offset(obsX, obsY)
                            )
                        }
                    }
                }

                // Draw Particles
                for (p in particles) {
                    drawCircle(
                        color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                        radius = 8f * p.life,
                        center = Offset(p.x * w, p.y * h)
                    )
                }
            }

            // Interactive Lane Tap Zones for easy mobile play!
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { currentLane = 0 }
                        .testTag("lane_left")
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { currentLane = 1 }
                        .testTag("lane_center")
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { currentLane = 2 }
                        .testTag("lane_right")
                )
            }

            // Top HUD: Score, Combo, Coins
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCORE: $score",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        if (combo > 1) {
                            Text(
                                text = " x$combo",
                                fontWeight = FontWeight.ExtraBold,
                                color = OddbodsBubblesYellow,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                            text = "$coinsCollected",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Player Fuse Character Sprite at current lane
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val laneW = maxWidth / 3
                val playerX = laneW * currentLane + (laneW - 90.dp) / 2

                Box(
                    modifier = Modifier
                        .offset(x = playerX, y = maxHeight * 0.72f)
                        .size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OddbodCharacterView(
                        character = fuse,
                        size = 85.dp,
                        animate = true,
                        isSuperPowered = isSuperRageActive
                    )
                }
            }

            // Bottom Controls Bar: Lane Switchers, Smash Punch Button, Super Ability Button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rage Meter Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isSuperRageActive) "SUPER FURY RAGE: ${(superRageTimeRemaining).toInt()}s" else "FURY RAGE METER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuperRageActive) RageFireRed else Color.White
                        )
                        Text(
                            text = "${(rageMeter * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OddbodsFuseOrange
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (isSuperRageActive) (superRageTimeRemaining / 6f) else rageMeter },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isSuperRageActive) RageFireRed else OddbodsFuseOrange,
                        trackColor = Color.DarkGray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Lane Button
                    FilledTonalButton(
                        onClick = {
                            if (currentLane > 0) {
                                currentLane--
                                viewModel.soundEffects.playBoing()
                            }
                        },
                        modifier = Modifier.size(56.dp).testTag("btn_lane_left"),
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Move Left", tint = Color.White)
                    }

                    // SMASH PUNCH PRIMARY BUTTON
                    Button(
                        onClick = { performSmash() },
                        modifier = Modifier
                            .height(60.dp)
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("btn_smash_punch"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OddbodsFuseRed
                        )
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Smash", tint = Color.Yellow)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SMASH!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Right Lane Button
                    FilledTonalButton(
                        onClick = {
                            if (currentLane < 2) {
                                currentLane++
                                viewModel.soundEffects.playBoing()
                            }
                        },
                        modifier = Modifier.size(56.dp).testTag("btn_lane_right"),
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Move Right", tint = Color.White)
                    }
                }

                // Super Ability Activate Button (Enabled when rage = 100%)
                AnimatedVisibility(visible = rageMeter >= 0.99f && !isSuperRageActive) {
                    Button(
                        onClick = {
                            isSuperRageActive = true
                            superRageTimeRemaining = 6f
                            viewModel.soundEffects.playAbilityTrigger()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .scale(pulseScale)
                            .testTag("btn_activate_super_fury"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RageFireRed
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Super Fury",
                            tint = Color.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTIVATE SUPER FURY DASH!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Game Over Dialog
            if (isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "Smash Out!",
                            fontWeight = FontWeight.Bold,
                            color = OddbodsFuseRed
                        )
                    },
                    text = {
                        Column {
                            Text(text = "Fuse ran out of stamina!")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Final Score: $score", fontWeight = FontWeight.Bold)
                            Text(text = "Boulders Smashed: $smashesCount")
                            Text(text = "Coins Earned: +${(score / 15).coerceAtLeast(2)}")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Reset game
                                lives = 3
                                score = 0
                                smashesCount = 0
                                coinsCollected = 0
                                rageMeter = 0.2f
                                isSuperRageActive = false
                                obstacles.clear()
                                isGameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OddbodsFuseRed),
                            modifier = Modifier.testTag("btn_play_again")
                        ) {
                            Text("Play Again")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("btn_quit_to_menu")
                        ) {
                            Text("Menu")
                        }
                    }
                )
            }
        }
    }
}
