package com.example.ui.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

enum class SnackType {
    PIZZA,        // +100
    BURGER,       // +150
    DONUT,        // +80
    ICE_CREAM,    // +120
    ALARM_CLOCK,  // Hazard (-1 Nap Heart)
    DUMBBELL      // Hazard (-1 Nap Heart)
}

data class FallingSnack(
    val id: Long,
    var x: Float, // 0.0 to 1.0
    var y: Float, // 0.0 to 1.0
    val type: SnackType,
    val speed: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeeSnackCatcherGame(
    viewModel: OddbodsViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val zee = remember { OddbodsData.getCharacterById("zee") }

    var playerX by remember { mutableFloatStateOf(0.5f) } // 0.05 to 0.95
    var score by remember { mutableIntStateOf(0) }
    var snacksEaten by remember { mutableIntStateOf(0) }
    var napHearts by remember { mutableIntStateOf(3) }
    var snoozeVortexMeter by remember { mutableFloatStateOf(0.2f) }
    var isVortexActive by remember { mutableStateOf(false) }
    var vortexDuration by remember { mutableFloatStateOf(0f) }
    var isGameOver by remember { mutableStateOf(false) }

    val fallingSnacks = remember { mutableStateListOf<FallingSnack>() }
    val crumbParticles = remember { mutableStateListOf<Particle>() }

    // Game loop
    LaunchedEffect(isGameOver) {
        if (isGameOver) return@LaunchedEffect

        var snackId = 0L
        var spawnTimer = 0

        while (isActive && !isGameOver) {
            delay(30) // ~33 fps

            // Vortex mode
            if (isVortexActive) {
                vortexDuration -= 0.03f
                if (vortexDuration <= 0f) {
                    isVortexActive = false
                    snoozeVortexMeter = 0f
                }
            }

            // Spawn snacks
            spawnTimer++
            if (spawnTimer >= 28) {
                spawnTimer = 0
                val roll = Random.nextInt(100)
                val type = when {
                    roll < 30 -> SnackType.PIZZA
                    roll < 55 -> SnackType.BURGER
                    roll < 75 -> SnackType.DONUT
                    roll < 85 -> SnackType.ICE_CREAM
                    roll < 93 -> SnackType.ALARM_CLOCK
                    else -> SnackType.DUMBBELL
                }
                val speed = Random.nextFloat() * 0.008f + 0.015f + (score / 4000f).coerceAtMost(0.015f)
                fallingSnacks.add(
                    FallingSnack(
                        id = snackId++,
                        x = Random.nextFloat() * 0.85f + 0.07f,
                        y = -0.05f,
                        type = type,
                        speed = speed
                    )
                )
            }

            // Update snacks
            val toRemove = mutableListOf<FallingSnack>()
            for (snack in fallingSnacks) {
                snack.y += snack.speed

                // In vortex mode, pull all foods straight to player
                if (isVortexActive && snack.type != SnackType.ALARM_CLOCK && snack.type != SnackType.DUMBBELL) {
                    snack.x += (playerX - snack.x) * 0.12f
                    snack.y += 0.02f
                }

                // Check catch by Zee at y in 0.78..0.88
                if (snack.y in 0.76f..0.88f) {
                    val dist = kotlin.math.abs(snack.x - playerX)
                    if (dist < 0.14f) {
                        toRemove.add(snack)
                        when (snack.type) {
                            SnackType.PIZZA -> {
                                snacksEaten++
                                score += 100
                                snoozeVortexMeter = (snoozeVortexMeter + 0.15f).coerceAtMost(1f)
                                viewModel.soundEffects.playYum()
                            }
                            SnackType.BURGER -> {
                                snacksEaten++
                                score += 150
                                snoozeVortexMeter = (snoozeVortexMeter + 0.2f).coerceAtMost(1f)
                                viewModel.soundEffects.playYum()
                            }
                            SnackType.DONUT -> {
                                snacksEaten++
                                score += 80
                                snoozeVortexMeter = (snoozeVortexMeter + 0.12f).coerceAtMost(1f)
                                viewModel.soundEffects.playYum()
                            }
                            SnackType.ICE_CREAM -> {
                                snacksEaten++
                                score += 120
                                snoozeVortexMeter = (snoozeVortexMeter + 0.18f).coerceAtMost(1f)
                                viewModel.soundEffects.playYum()
                            }
                            SnackType.ALARM_CLOCK, SnackType.DUMBBELL -> {
                                if (!isVortexActive) {
                                    napHearts--
                                    viewModel.soundEffects.playGameOver()
                                    if (napHearts <= 0) {
                                        isGameOver = true
                                        viewModel.recordZeeGameScore(score, snacksEaten)
                                    }
                                }
                            }
                        }

                        // Add crumbs
                        repeat(6) {
                            crumbParticles.add(
                                Particle(
                                    x = playerX,
                                    y = 0.82f,
                                    vx = (Random.nextFloat() - 0.5f) * 0.03f,
                                    vy = (Random.nextFloat() - 0.5f) * 0.03f,
                                    color = listOf(GoldCoinColor, OddbodsZeeLime, Color.White).random(),
                                    life = 1f
                                )
                            )
                        }
                    }
                } else if (snack.y > 1.05f) {
                    toRemove.add(snack)
                }
            }
            fallingSnacks.removeAll(toRemove)

            // Update crumbs
            val deadCrumb = mutableListOf<Particle>()
            for (i in crumbParticles.indices) {
                val p = crumbParticles[i]
                val newLife = p.life - 0.06f
                if (newLife <= 0f) deadCrumb.add(p)
                else crumbParticles[i] = p.copy(x = p.x + p.vx, y = p.y + p.vy, life = newLife)
            }
            crumbParticles.removeAll(deadCrumb)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Zee's Snack Nap Catcher",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isVortexActive) "💤 SNOOZE VORTEX VACUUM ACTIVE!" else "Catch tasty snacks, avoid alarm clocks!",
                            fontSize = 12.sp,
                            color = if (isVortexActive) GoldCoinColor else Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_zee")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isVortexActive) OddbodsZeeLime else OddbodsZeeGreen
                ),
                actions = {
                    Row(modifier = Modifier.padding(end = 12.dp)) {
                        repeat(napHearts.coerceAtLeast(0)) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Nap Heart",
                                tint = GoldCoinColor,
                                modifier = Modifier.size(22.dp).padding(horizontal = 2.dp)
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
                        colors = listOf(
                            Color(0xFF1B5E20),
                            Color(0xFF2E7D32),
                            Color(0xFF388E3C),
                            Color(0xFF1B381E)
                        )
                    )
                )
                // Slide/drag Zee smoothly
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x / size.width
                        playerX = (playerX + dx).coerceIn(0.08f, 0.92f)
                    }
                }
        ) {
            // Food & Clouds Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw falling snacks
                for (s in fallingSnacks) {
                    val sx = s.x * w
                    val sy = s.y * h

                    when (s.type) {
                        SnackType.PIZZA -> {
                            // Pizza wedge triangle
                            drawCircle(color = Color(0xFFFFB300), radius = 20f, center = Offset(sx, sy))
                            drawCircle(color = Color(0xFFD32F2F), radius = 6f, center = Offset(sx - 5f, sy - 5f))
                            drawCircle(color = Color(0xFFD32F2F), radius = 5f, center = Offset(sx + 6f, sy + 4f))
                        }
                        SnackType.BURGER -> {
                            // Juicy burger
                            drawRoundRect(
                                color = Color(0xFFFFB74D),
                                topLeft = Offset(sx - 20f, sy - 14f),
                                size = androidx.compose.ui.geometry.Size(40f, 14f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = Color(0xFF5D4037),
                                topLeft = Offset(sx - 18f, sy - 1f),
                                size = androidx.compose.ui.geometry.Size(36f, 8f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                            drawRoundRect(
                                color = Color(0xFFFFB74D),
                                topLeft = Offset(sx - 20f, sy + 7f),
                                size = androidx.compose.ui.geometry.Size(40f, 10f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                        }
                        SnackType.DONUT -> {
                            // Pink frosted donut
                            drawCircle(color = Color(0xFF8D6E63), radius = 18f, center = Offset(sx, sy))
                            drawCircle(color = Color(0xFFFF80AB), radius = 14f, center = Offset(sx, sy))
                            drawCircle(color = Color(0xFF1B5E20), radius = 6f, center = Offset(sx, sy))
                        }
                        SnackType.ICE_CREAM -> {
                            // Ice cream cone
                            drawCircle(color = Color(0xFFE1BEE7), radius = 16f, center = Offset(sx, sy - 6f))
                            drawCircle(color = Color(0xFFE91E63), radius = 5f, center = Offset(sx, sy - 18f))
                        }
                        SnackType.ALARM_CLOCK -> {
                            // Annoying red alarm clock
                            drawCircle(color = Color(0xFFD32F2F), radius = 18f, center = Offset(sx, sy))
                            drawCircle(color = Color.White, radius = 13f, center = Offset(sx, sy))
                            drawLine(color = Color.Black, start = Offset(sx, sy), end = Offset(sx, sy - 8f), strokeWidth = 3f)
                        }
                        SnackType.DUMBBELL -> {
                            // Heavy iron barbell
                            drawLine(color = Color.LightGray, start = Offset(sx - 18f, sy), end = Offset(sx + 18f, sy), strokeWidth = 5f)
                            drawCircle(color = Color(0xFF424242), radius = 10f, center = Offset(sx - 18f, sy))
                            drawCircle(color = Color(0xFF424242), radius = 10f, center = Offset(sx + 18f, sy))
                        }
                    }
                }

                // Draw crumbs
                for (p in crumbParticles) {
                    drawCircle(color = p.color.copy(alpha = p.life), radius = 6f * p.life, center = Offset(p.x * w, p.y * h))
                }
            }

            // Tap left/right side controls
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures { playerX = (playerX - 0.12f).coerceAtLeast(0.08f) }
                        }
                        .testTag("tap_zee_left")
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures { playerX = (playerX + 0.12f).coerceAtMost(0.92f) }
                        }
                        .testTag("tap_zee_right")
                )
            }

            // Player Zee Character View
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val zx = maxWidth * playerX - 45.dp
                val zy = maxHeight * 0.78f

                Box(
                    modifier = Modifier
                        .offset(x = zx, y = zy)
                        .size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OddbodCharacterView(
                        character = zee,
                        size = 85.dp,
                        animate = true,
                        isSuperPowered = isVortexActive
                    )
                }
            }

            // HUD Top overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
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
                            text = "Snacks Eaten: $snacksEaten",
                            color = GoldCoinColor,
                            fontSize = 12.sp
                        )
                    }
                }

                // Snooze Vortex Button
                if (snoozeVortexMeter >= 0.99f && !isVortexActive) {
                    Button(
                        onClick = {
                            isVortexActive = true
                            vortexDuration = 5f
                            viewModel.soundEffects.playAbilityTrigger()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OddbodsZeeLime),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("btn_zee_vortex")
                    ) {
                        Icon(imageVector = Icons.Default.Bedtime, contentDescription = "Snooze Vortex", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SNOOZE VORTEX!", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = "SNOOZE VORTEX: ${(snoozeVortexMeter * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            LinearProgressIndicator(
                                progress = { snoozeVortexMeter },
                                modifier = Modifier.width(100.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = OddbodsZeeLime,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Game Over Dialog
            if (isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "Zee Was Woken Up!",
                            fontWeight = FontWeight.Bold,
                            color = OddbodsZeeGreen
                        )
                    },
                    text = {
                        Column {
                            Text(text = "An alarm clock interrupted Zee's nap!")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Final Score: $score", fontWeight = FontWeight.Bold)
                            Text(text = "Snacks Enjoyed: $snacksEaten")
                            Text(text = "Coins Earned: +${(score / 18).coerceAtLeast(3)}")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                napHearts = 3
                                score = 0
                                snacksEaten = 0
                                snoozeVortexMeter = 0.2f
                                isVortexActive = false
                                fallingSnacks.clear()
                                isGameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OddbodsZeeGreen),
                            modifier = Modifier.testTag("btn_zee_retry")
                        ) {
                            Text("Snack Again")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = onBack, modifier = Modifier.testTag("btn_zee_menu")) {
                            Text("Menu")
                        }
                    }
                )
            }
        }
    }
}
