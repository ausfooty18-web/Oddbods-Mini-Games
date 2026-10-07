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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

enum class PlatformType {
    NORMAL,
    SUPER_BOUNCE,
    WHOOPEE_CUSHION,
    MOVING
}

data class BouncePlatform(
    val id: Long,
    var x: Float, // 0.0 to 1.0 (relative width)
    var y: Float, // screen y or camera relative
    val width: Float = 0.22f,
    val type: PlatformType,
    var vx: Float = 0f
)

data class SkyItem(
    val id: Long,
    val x: Float,
    val y: Float,
    val isBalloon: Boolean // true: prank balloon, false: star coin
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PogoPrankBounceGame(
    viewModel: OddbodsViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val pogo = remember { OddbodsData.getCharacterById("pogo") }

    // Physics state
    var playerX by remember { mutableFloatStateOf(0.5f) } // 0.0 to 1.0
    var playerY by remember { mutableFloatStateOf(600f) }
    var velocityY by remember { mutableFloatStateOf(-14f) }
    val gravity = 0.65f

    var highestAltitude by remember { mutableIntStateOf(0) }
    var balloonsPopped by remember { mutableIntStateOf(0) }
    var prankPowerMeter by remember { mutableFloatStateOf(0.3f) }
    var isMegaSpringActive by remember { mutableStateOf(false) }
    var megaSpringTimer by remember { mutableFloatStateOf(0f) }
    var isGameOver by remember { mutableStateOf(false) }

    val platforms = remember { mutableStateListOf<BouncePlatform>() }
    val skyItems = remember { mutableStateListOf<SkyItem>() }
    val confettiParticles = remember { mutableStateListOf<Particle>() }

    // Initialize platforms
    LaunchedEffect(Unit) {
        platforms.clear()
        skyItems.clear()
        // Starting base platform
        platforms.add(BouncePlatform(0, 0.4f, 650f, 0.3f, PlatformType.NORMAL))
        for (i in 1..8) {
            val type = when {
                i % 4 == 0 -> PlatformType.WHOOPEE_CUSHION
                i % 3 == 0 -> PlatformType.SUPER_BOUNCE
                i % 5 == 0 -> PlatformType.MOVING
                else -> PlatformType.NORMAL
            }
            val plat = BouncePlatform(
                id = i.toLong(),
                x = Random.nextFloat() * 0.7f + 0.05f,
                y = 650f - i * 110f,
                width = 0.22f,
                type = type,
                vx = if (type == PlatformType.MOVING) 0.006f else 0f
            )
            platforms.add(plat)

            if (Random.nextBoolean()) {
                skyItems.add(
                    SkyItem(
                        id = i.toLong(),
                        x = plat.x + 0.1f,
                        y = plat.y - 45f,
                        isBalloon = Random.nextBoolean()
                    )
                )
            }
        }
    }

    // Main physics tick
    LaunchedEffect(isGameOver) {
        if (isGameOver) return@LaunchedEffect

        var platCounter = 20L
        while (isActive && !isGameOver) {
            delay(28) // ~35 fps

            // Mega Spring ability mode
            if (isMegaSpringActive) {
                megaSpringTimer -= 0.028f
                velocityY = -22f
                if (megaSpringTimer <= 0f) {
                    isMegaSpringActive = false
                    prankPowerMeter = 0f
                }
            } else {
                velocityY += gravity
            }

            playerY += velocityY

            // Update moving platforms
            for (p in platforms) {
                if (p.type == PlatformType.MOVING) {
                    p.x += p.vx
                    if (p.x <= 0.05f || p.x >= 0.75f) {
                        p.vx = -p.vx
                    }
                }
            }

            // Camera scroll up when player rises above middle screen (y < 400)
            if (playerY < 380f) {
                val delta = 380f - playerY
                playerY = 380f
                highestAltitude += (delta * 0.4f).toInt()

                // Shift all platforms down
                for (p in platforms) {
                    p.y += delta
                }
                for (item in skyItems) {
                    // Recreate item with new y
                    val idx = skyItems.indexOf(item)
                    if (idx != -1) {
                        skyItems[idx] = item.copy(y = item.y + delta)
                    }
                }

                // Recycle platforms that fall below screen
                val belowList = platforms.filter { it.y > 850f }
                for (b in belowList) {
                    platforms.remove(b)
                    val topPlatY = platforms.minOfOrNull { it.y } ?: 300f
                    val newType = when {
                        Random.nextInt(10) < 2 -> PlatformType.WHOOPEE_CUSHION
                        Random.nextInt(10) < 4 -> PlatformType.SUPER_BOUNCE
                        Random.nextInt(10) < 6 -> PlatformType.MOVING
                        else -> PlatformType.NORMAL
                    }
                    val newPlat = BouncePlatform(
                        id = platCounter++,
                        x = Random.nextFloat() * 0.7f + 0.05f,
                        y = topPlatY - Random.nextInt(90, 130).toFloat(),
                        width = 0.22f,
                        type = newType,
                        vx = if (newType == PlatformType.MOVING) 0.007f else 0f
                    )
                    platforms.add(newPlat)

                    if (Random.nextInt(100) < 55) {
                        skyItems.add(
                            SkyItem(
                                id = platCounter++,
                                x = newPlat.x + 0.08f,
                                y = newPlat.y - 45f,
                                isBalloon = Random.nextBoolean()
                            )
                        )
                    }
                }
                skyItems.removeAll { it.y > 850f }
            }

            // Platform collision (only when moving downwards!)
            if (velocityY > 0f) {
                val playerFootY = playerY + 35f
                for (p in platforms) {
                    val pLeft = p.x
                    val pRight = p.x + p.width
                    if (playerX in pLeft..pRight && playerFootY in (p.y - 15f)..(p.y + 20f)) {
                        // Bounce!
                        when (p.type) {
                            PlatformType.NORMAL -> {
                                velocityY = -15f
                                viewModel.soundEffects.playBoing()
                            }
                            PlatformType.SUPER_BOUNCE -> {
                                velocityY = -21f
                                viewModel.soundEffects.playAbilityTrigger()
                                prankPowerMeter = (prankPowerMeter + 0.15f).coerceAtMost(1f)
                            }
                            PlatformType.WHOOPEE_CUSHION -> {
                                velocityY = -18f
                                viewModel.soundEffects.playSmash()
                                prankPowerMeter = (prankPowerMeter + 0.25f).coerceAtMost(1f)
                            }
                            PlatformType.MOVING -> {
                                velocityY = -15f
                                viewModel.soundEffects.playBoing()
                            }
                        }
                        break
                    }
                }
            }

            // Item pickup check
            val collectedItems = mutableListOf<SkyItem>()
            for (item in skyItems) {
                val dx = kotlin.math.abs(playerX - item.x)
                val dy = kotlin.math.abs(playerY - item.y)
                if (dx < 0.12f && dy < 45f) {
                    collectedItems.add(item)
                    if (item.isBalloon) {
                        balloonsPopped++
                        prankPowerMeter = (prankPowerMeter + 0.2f).coerceAtMost(1f)
                        viewModel.soundEffects.playZap()
                        // Confetti explosion
                        repeat(10) {
                            confettiParticles.add(
                                Particle(
                                    x = item.x,
                                    y = item.y / 800f,
                                    vx = (Random.nextFloat() - 0.5f) * 0.04f,
                                    vy = (Random.nextFloat() - 0.5f) * 0.04f,
                                    color = listOf(OddbodsPogoCyan, OddbodsFuseOrange, OddbodsBubblesYellow).random(),
                                    life = 1f
                                )
                            )
                        }
                    } else {
                        viewModel.soundEffects.playCoin()
                    }
                }
            }
            skyItems.removeAll(collectedItems)

            // Update confetti particles
            val deadP = mutableListOf<Particle>()
            for (i in confettiParticles.indices) {
                val p = confettiParticles[i]
                val newLife = p.life - 0.05f
                if (newLife <= 0f) deadP.add(p)
                else {
                    confettiParticles[i] = p.copy(x = p.x + p.vx, y = p.y + p.vy, life = newLife)
                }
            }
            confettiParticles.removeAll(deadP)

            // Game over check (fell below bottom)
            if (playerY > 800f) {
                isGameOver = true
                viewModel.soundEffects.playGameOver()
                viewModel.recordPogoGameScore(highestAltitude, balloonsPopped)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pogo's Prank Bounce",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isMegaSpringActive) "🚀 SUPER PRANK SPRING HYPER-BOUNCE!" else "Tilt/drag to bounce on cushions!",
                            fontSize = 12.sp,
                            color = if (isMegaSpringActive) OddbodsBubblesYellow else Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_pogo")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isMegaSpringActive) OddbodsPogoCyan else OddbodsPogoBlue
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
                            Color(0xFF0D47A1),
                            Color(0xFF1976D2),
                            Color(0xFF42A5F5),
                            Color(0xFF90CAF9)
                        )
                    )
                )
                // Drag gesture to move Pogo smoothly
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val screenWidth = size.width
                        val dx = dragAmount.x / screenWidth
                        playerX = (playerX + dx).coerceIn(0.05f, 0.95f)
                    }
                }
        ) {
            // Sky & Platforms Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw clouds background decor
                drawCircle(Color.White.copy(alpha = 0.25f), radius = 70f, center = Offset(w * 0.2f, 200f))
                drawCircle(Color.White.copy(alpha = 0.25f), radius = 100f, center = Offset(w * 0.35f, 220f))
                drawCircle(Color.White.copy(alpha = 0.25f), radius = 80f, center = Offset(w * 0.8f, 450f))

                // Draw Platforms
                for (p in platforms) {
                    val pX = p.x * w
                    val pW = p.width * w
                    val pY = (p.y / 800f) * h
                    val pH = 18f

                    when (p.type) {
                        PlatformType.NORMAL -> {
                            drawRoundRect(
                                color = OddbodsZeeGreen,
                                topLeft = Offset(pX, pY),
                                size = Size(pW, pH),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = OddbodsZeeLime,
                                topLeft = Offset(pX + 4f, pY + 2f),
                                size = Size(pW - 8f, pH - 6f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                        PlatformType.SUPER_BOUNCE -> {
                            // Trampoline with springs
                            drawRoundRect(
                                color = OddbodsBubblesGold,
                                topLeft = Offset(pX, pY),
                                size = Size(pW, pH),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            // Trampoline spring indicators
                            drawCircle(Color.White, radius = 5f, center = Offset(pX + pW * 0.3f, pY + pH * 0.5f))
                            drawCircle(Color.White, radius = 5f, center = Offset(pX + pW * 0.7f, pY + pH * 0.5f))
                        }
                        PlatformType.WHOOPEE_CUSHION -> {
                            // Funny prank whoopee cushion pad
                            drawRoundRect(
                                color = OddbodsFuseOrange,
                                topLeft = Offset(pX, pY),
                                size = Size(pW, pH * 1.3f),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                            drawCircle(Color.White, radius = 6f, center = Offset(pX + pW * 0.5f, pY + pH * 0.65f))
                        }
                        PlatformType.MOVING -> {
                            // Moving sky glider
                            drawRoundRect(
                                color = OddbodsPogoCyan,
                                topLeft = Offset(pX, pY),
                                size = Size(pW, pH),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }
                }

                // Draw Sky Items (Balloons & Coins)
                for (item in skyItems) {
                    val iX = item.x * w
                    val iY = (item.y / 800f) * h
                    if (item.isBalloon) {
                        // Prank Balloon
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFF4081), Color(0xFFC2185B)),
                                center = Offset(iX - 5f, iY - 5f),
                                radius = 22f
                            ),
                            radius = 20f,
                            center = Offset(iX, iY)
                        )
                        // Balloon string
                        drawLine(
                            color = Color.White,
                            start = Offset(iX, iY + 20f),
                            end = Offset(iX, iY + 36f),
                            strokeWidth = 2f
                        )
                    } else {
                        // Gold Star Coin
                        drawCircle(color = GoldCoinColor, radius = 14f, center = Offset(iX, iY))
                        drawCircle(color = Color(0xFFFF8F00), radius = 8f, center = Offset(iX, iY))
                    }
                }

                // Draw Confetti Particles
                for (p in confettiParticles) {
                    drawCircle(
                        color = p.color.copy(alpha = p.life),
                        radius = 8f * p.life,
                        center = Offset(p.x * w, p.y * h)
                    )
                }
            }

            // Tap left/right side controls for players who prefer tapping
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                playerX = (playerX - 0.12f).coerceAtLeast(0.05f)
                            }
                        }
                        .testTag("tap_steer_left")
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                playerX = (playerX + 0.12f).coerceAtMost(0.95f)
                            }
                        }
                        .testTag("tap_steer_right")
                )
            }

            // Player Pogo Character View
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val pX = maxWidth * playerX - 40.dp
                val pY = (maxHeight * (playerY / 800f)) - 40.dp

                Box(
                    modifier = Modifier
                        .offset(x = pX, y = pY)
                        .size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OddbodCharacterView(
                        character = pogo,
                        size = 75.dp,
                        animate = true,
                        isSuperPowered = isMegaSpringActive
                    )
                }
            }

            // HUD Top Cards
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
                            text = "HEIGHT: ${highestAltitude}m",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Balloons: $balloonsPopped",
                            color = OddbodsBubblesYellow,
                            fontSize = 12.sp
                        )
                    }
                }

                // Super Trick Spring Button
                if (prankPowerMeter >= 0.99f && !isMegaSpringActive) {
                    Button(
                        onClick = {
                            isMegaSpringActive = true
                            megaSpringTimer = 4.5f
                            viewModel.soundEffects.playAbilityTrigger()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OddbodsFuseOrange),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("btn_pogo_super_spring")
                    ) {
                        Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = "Super Spring", tint = Color.Yellow)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SUPER SPRING!", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = "TRICK SPRING: ${(prankPowerMeter * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            LinearProgressIndicator(
                                progress = { prankPowerMeter },
                                modifier = Modifier.width(100.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = OddbodsPogoCyan,
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
                            text = "Bounced Out!",
                            fontWeight = FontWeight.Bold,
                            color = OddbodsPogoBlue
                        )
                    },
                    text = {
                        Column {
                            Text(text = "Pogo bounced too far down!")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Altitude Reached: ${highestAltitude}m", fontWeight = FontWeight.Bold)
                            Text(text = "Balloons Popped: $balloonsPopped")
                            Text(text = "Coins Earned: +${(highestAltitude / 20).coerceAtLeast(3) + balloonsPopped}")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                playerX = 0.5f
                                playerY = 600f
                                velocityY = -14f
                                highestAltitude = 0
                                balloonsPopped = 0
                                prankPowerMeter = 0.3f
                                isMegaSpringActive = false
                                isGameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OddbodsPogoBlue),
                            modifier = Modifier.testTag("btn_pogo_retry")
                        ) {
                            Text("Bounce Again")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = onBack, modifier = Modifier.testTag("btn_pogo_menu")) {
                            Text("Menu")
                        }
                    }
                )
            }
        }
    }
}
