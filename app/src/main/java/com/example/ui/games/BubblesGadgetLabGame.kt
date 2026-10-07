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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
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
import com.example.viewmodel.OddbodsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

enum class LabObstacleType {
    LASER_BEAM,      // Vertical laser gate
    FLOOR_TESLA,     // Tesla trap on floor
    CEILING_TESLA,   // Tesla trap on ceiling
    BATTERY_CELL,    // Tech Battery (+100 pts, +25% magnet charge)
    QUANTUM_CHIP     // Microchip (+50 pts)
}

data class LabEntity(
    val id: Long,
    var x: Float, // 1.1 (right) to -0.2 (offscreen left)
    val type: LabObstacleType,
    val isCeiling: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubblesGadgetLabGame(
    viewModel: OddbodsViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val bubbles = remember { OddbodsData.getCharacterById("bubbles") }

    // Lab state
    var isCeilingGravity by remember { mutableStateOf(false) } // false = floor, true = ceiling
    var playerY by remember { mutableFloatStateOf(0.75f) } // target y: floor 0.75, ceiling 0.22
    var score by remember { mutableIntStateOf(0) }
    var gadgetsCollected by remember { mutableIntStateOf(0) }
    var magnetCharge by remember { mutableFloatStateOf(0.3f) } // 0.0 to 1.0
    var isMagnetActive by remember { mutableStateOf(false) }
    var magnetDuration by remember { mutableFloatStateOf(0f) }
    var isGameOver by remember { mutableStateOf(false) }

    val entities = remember { mutableStateListOf<LabEntity>() }
    val sparkParticles = remember { mutableStateListOf<Particle>() }

    // Smooth transition for player vertical movement
    val targetY = if (isCeilingGravity) 0.22f else 0.75f
    val animatedPlayerY by animateFloatAsState(
        targetValue = targetY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "player_gravity"
    )

    // Game loop
    LaunchedEffect(isGameOver) {
        if (isGameOver) return@LaunchedEffect

        var entityCounter = 0L
        var spawnTimer = 0

        while (isActive && !isGameOver) {
            delay(30) // ~33 fps

            // Magnet pulse duration
            if (isMagnetActive) {
                magnetDuration -= 0.03f
                if (magnetDuration <= 0f) {
                    isMagnetActive = false
                    magnetCharge = 0f
                }
            }

            // Spawn lab hazards & batteries
            spawnTimer++
            if (spawnTimer >= 35) {
                spawnTimer = 0
                val roll = Random.nextInt(100)
                val type = when {
                    roll < 35 -> LabObstacleType.BATTERY_CELL
                    roll < 55 -> LabObstacleType.QUANTUM_CHIP
                    roll < 75 -> if (Random.nextBoolean()) LabObstacleType.FLOOR_TESLA else LabObstacleType.CEILING_TESLA
                    else -> LabObstacleType.LASER_BEAM
                }
                entities.add(LabEntity(entityCounter++, 1.15f, type, type == LabObstacleType.CEILING_TESLA))
            }

            // Update entities
            val speed = 0.024f + (score / 3500f).coerceAtMost(0.02f)
            val toRemove = mutableListOf<LabEntity>()

            for (ent in entities) {
                ent.x -= speed

                // If magnet active, attract batteries and chips towards player
                if (isMagnetActive && (ent.type == LabObstacleType.BATTERY_CELL || ent.type == LabObstacleType.QUANTUM_CHIP)) {
                    ent.x -= 0.03f // pulled faster
                }

                // Check collision near player x = 0.25f
                if (ent.x in 0.16f..0.34f) {
                    when (ent.type) {
                        LabObstacleType.BATTERY_CELL -> {
                            gadgetsCollected++
                            score += 100
                            magnetCharge = (magnetCharge + 0.25f).coerceAtMost(1f)
                            viewModel.soundEffects.playZap()
                            repeat(8) {
                                sparkParticles.add(
                                    Particle(
                                        x = 0.25f,
                                        y = animatedPlayerY,
                                        vx = (Random.nextFloat() - 0.5f) * 0.04f,
                                        vy = (Random.nextFloat() - 0.5f) * 0.04f,
                                        color = TechCyan,
                                        life = 1f
                                    )
                                )
                            }
                            toRemove.add(ent)
                        }
                        LabObstacleType.QUANTUM_CHIP -> {
                            gadgetsCollected++
                            score += 60
                            magnetCharge = (magnetCharge + 0.15f).coerceAtMost(1f)
                            viewModel.soundEffects.playCoin()
                            toRemove.add(ent)
                        }
                        LabObstacleType.FLOOR_TESLA -> {
                            if (!isCeilingGravity && !isMagnetActive) {
                                // Hit floor trap!
                                isGameOver = true
                                viewModel.soundEffects.playGameOver()
                                viewModel.recordBubblesGameScore(score, gadgetsCollected)
                            }
                        }
                        LabObstacleType.CEILING_TESLA -> {
                            if (isCeilingGravity && !isMagnetActive) {
                                // Hit ceiling trap!
                                isGameOver = true
                                viewModel.soundEffects.playGameOver()
                                viewModel.recordBubblesGameScore(score, gadgetsCollected)
                            }
                        }
                        LabObstacleType.LASER_BEAM -> {
                            if (!isMagnetActive) {
                                // Laser crosses whole height unless magnet pulse disables it!
                                isGameOver = true
                                viewModel.soundEffects.playGameOver()
                                viewModel.recordBubblesGameScore(score, gadgetsCollected)
                            }
                        }
                    }
                } else if (ent.x < -0.2f) {
                    toRemove.add(ent)
                    score += 15 // survived passing hazard
                }
            }
            entities.removeAll(toRemove)

            // Spark particles
            val deadSparks = mutableListOf<Particle>()
            for (i in sparkParticles.indices) {
                val p = sparkParticles[i]
                val newLife = p.life - 0.06f
                if (newLife <= 0f) deadSparks.add(p)
                else {
                    sparkParticles[i] = p.copy(x = p.x + p.vx, y = p.y + p.vy, life = newLife)
                }
            }
            sparkParticles.removeAll(deadSparks)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bubbles' Gadget Lab",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isMagnetActive) "⚡ ANTI-GRAV MAGNET OVERCHARGE ACTIVE!" else "Tap screen to invert gravity ceiling/floor!",
                            fontSize = 12.sp,
                            color = if (isMagnetActive) OddbodsBubblesGold else Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_bubbles")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isMagnetActive) TechCyan else Color(0xFFFBC02D)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A237E),
                            Color(0xFF0D1B2A),
                            Color(0xFF1B263B)
                        )
                    )
                )
                // Tap anywhere to Flip Gravity!
                .clickable {
                    if (!isGameOver) {
                        isCeilingGravity = !isCeilingGravity
                        viewModel.soundEffects.playBoing()
                    }
                }
                .testTag("tap_gravity_flip")
        ) {
            // Lab Environment Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Ceiling track
                drawRect(
                    color = Color(0xFF263238),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, h * 0.16f)
                )
                drawLine(
                    color = TechCyan,
                    start = Offset(0f, h * 0.16f),
                    end = Offset(w, h * 0.16f),
                    strokeWidth = 6f
                )

                // Floor track
                drawRect(
                    color = Color(0xFF263238),
                    topLeft = Offset(0f, h * 0.84f),
                    size = Size(w, h * 0.16f)
                )
                drawLine(
                    color = TechCyan,
                    start = Offset(0f, h * 0.84f),
                    end = Offset(w, h * 0.84f),
                    strokeWidth = 6f
                )

                // Draw Entities
                for (ent in entities) {
                    val ex = ent.x * w

                    when (ent.type) {
                        LabObstacleType.LASER_BEAM -> {
                            // Vertical laser fence
                            val laserColor = if (isMagnetActive) Color.Gray.copy(alpha = 0.3f) else Color(0xFFFF1744)
                            drawLine(
                                color = laserColor,
                                start = Offset(ex, h * 0.16f),
                                end = Offset(ex, h * 0.84f),
                                strokeWidth = if (isMagnetActive) 2f else 8f
                            )
                            if (!isMagnetActive) {
                                drawCircle(Color.White, radius = 10f, center = Offset(ex, h * 0.5f))
                            }
                        }
                        LabObstacleType.FLOOR_TESLA -> {
                            // Electrical spike trap on floor
                            drawRoundRect(
                                color = if (isMagnetActive) Color.Gray else Color(0xFFFF9100),
                                topLeft = Offset(ex - 20f, h * 0.77f),
                                size = Size(40f, h * 0.07f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                        LabObstacleType.CEILING_TESLA -> {
                            // Electrical spike trap on ceiling
                            drawRoundRect(
                                color = if (isMagnetActive) Color.Gray else Color(0xFFFF9100),
                                topLeft = Offset(ex - 20f, h * 0.16f),
                                size = Size(40f, h * 0.07f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                        LabObstacleType.BATTERY_CELL -> {
                            // Glowing green/cyan energy battery
                            val by = if (ent.isCeiling) h * 0.3f else h * 0.7f
                            drawRoundRect(
                                color = TechCyan,
                                topLeft = Offset(ex - 18f, by - 24f),
                                size = Size(36f, 48f),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawCircle(Color.White, radius = 7f, center = Offset(ex, by))
                        }
                        LabObstacleType.QUANTUM_CHIP -> {
                            val cy = h * 0.5f
                            drawCircle(color = OddbodsBubblesGold, radius = 16f, center = Offset(ex, cy))
                            drawCircle(color = Color.White, radius = 8f, center = Offset(ex, cy))
                        }
                    }
                }

                // Draw Sparks
                for (p in sparkParticles) {
                    drawCircle(color = p.color.copy(alpha = p.life), radius = 6f * p.life, center = Offset(p.x * w, p.y * h))
                }
            }

            // Player Bubbles Character View
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val bx = maxWidth * 0.25f - 40.dp
                val by = maxHeight * animatedPlayerY - 40.dp

                Box(
                    modifier = Modifier
                        .offset(x = bx, y = by)
                        .size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OddbodCharacterView(
                        character = bubbles,
                        size = 75.dp,
                        animate = true,
                        isSuperPowered = isMagnetActive
                    )
                }
            }

            // Top HUD Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Text(
                            text = "SCORE: $score",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Gadgets: $gadgetsCollected",
                            color = TechCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                // Anti-Grav Magnet Button
                if (magnetCharge >= 0.99f && !isMagnetActive) {
                    Button(
                        onClick = {
                            isMagnetActive = true
                            magnetDuration = 5f
                            viewModel.soundEffects.playAbilityTrigger()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("btn_activate_magnet")
                    ) {
                        Icon(imageVector = Icons.Default.AllInclusive, contentDescription = "Magnet Pulse", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MAGNET PULSE!", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = "MAGNET CHARGE: ${(magnetCharge * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            LinearProgressIndicator(
                                progress = { magnetCharge },
                                modifier = Modifier.width(100.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = TechCyan,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Flip Gravity Helper Button at Bottom Center
            Button(
                onClick = {
                    if (!isGameOver) {
                        isCeilingGravity = !isCeilingGravity
                        viewModel.soundEffects.playBoing()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .testTag("btn_flip_gravity_action"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OddbodsBubblesGold)
            ) {
                Icon(
                    imageVector = if (isCeilingGravity) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = "Flip",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCeilingGravity) "FLIP TO FLOOR" else "FLIP TO CEILING",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Game Over Dialog
            if (isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "Lab Experiment Over!",
                            fontWeight = FontWeight.Bold,
                            color = OddbodsBubblesGold
                        )
                    },
                    text = {
                        Column {
                            Text(text = "Bubbles hit a high-voltage barrier!")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Final Score: $score", fontWeight = FontWeight.Bold)
                            Text(text = "Gadgets Collected: $gadgetsCollected")
                            Text(text = "Coins Earned: +${(score / 20).coerceAtLeast(3)}")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isCeilingGravity = false
                                score = 0
                                gadgetsCollected = 0
                                magnetCharge = 0.3f
                                isMagnetActive = false
                                entities.clear()
                                isGameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OddbodsBubblesGold),
                            modifier = Modifier.testTag("btn_bubbles_retry")
                        ) {
                            Text("Try Again", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = onBack, modifier = Modifier.testTag("btn_bubbles_menu")) {
                            Text("Menu")
                        }
                    }
                )
            }
        }
    }
}
