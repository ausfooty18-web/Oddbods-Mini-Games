package com.example.ui.hub

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AbilityEffectType
import com.example.model.OddbodCharacter
import com.example.model.OddbodsData
import com.example.ui.components.OddbodCharacterView
import com.example.ui.games.Particle
import com.example.ui.theme.*
import com.example.viewmodel.OddbodsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityShowcaseScreen(
    viewModel: OddbodsViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val allCharacters = remember { OddbodsData.ALL_CHARACTERS }
    var selectedCharacter by remember { mutableStateOf(allCharacters[0]) }
    var isAbilityActive by remember { mutableStateOf(false) }
    var abilityTimer by remember { mutableFloatStateOf(0f) }

    val effectParticles = remember { mutableStateListOf<Particle>() }

    // Playground effect loop
    LaunchedEffect(isAbilityActive, selectedCharacter) {
        if (!isAbilityActive) return@LaunchedEffect

        abilityTimer = 4.0f
        while (isActive && abilityTimer > 0f) {
            delay(32)
            abilityTimer -= 0.032f

            // Spawn dynamic themed particles
            when (selectedCharacter.abilityEffectType) {
                AbilityEffectType.FURY_BURST -> {
                    // Fire and sparks
                    repeat(3) {
                        effectParticles.add(
                            Particle(
                                x = Random.nextFloat() * 0.8f + 0.1f,
                                y = 0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                                vx = (Random.nextFloat() - 0.5f) * 0.05f,
                                vy = -Random.nextFloat() * 0.04f - 0.01f,
                                color = listOf(RageFireRed, OddbodsFuseOrange, OddbodsBubblesGold).random(),
                                life = 1f
                            )
                        )
                    }
                }
                AbilityEffectType.PRANK_SPRING -> {
                    // Confetti and bouncy stars
                    repeat(3) {
                        effectParticles.add(
                            Particle(
                                x = 0.5f,
                                y = 0.7f,
                                vx = (Random.nextFloat() - 0.5f) * 0.08f,
                                vy = -Random.nextFloat() * 0.06f - 0.03f,
                                color = listOf(OddbodsPogoCyan, Color(0xFFFF4081), GoldCoinColor).random(),
                                life = 1f
                            )
                        )
                    }
                }
                AbilityEffectType.MAGNETIC_PULSE -> {
                    // Lightning tech orbs
                    repeat(3) {
                        effectParticles.add(
                            Particle(
                                x = Random.nextFloat(),
                                y = Random.nextFloat(),
                                vx = (0.5f - Random.nextFloat()) * 0.04f,
                                vy = (0.5f - Random.nextFloat()) * 0.04f,
                                color = listOf(TechCyan, Color(0xFF7C4DFF), Color.White).random(),
                                life = 1f
                            )
                        )
                    }
                }
                AbilityEffectType.SNOOZE_VORTEX -> {
                    // Floating Zzz & green sparkles
                    repeat(2) {
                        effectParticles.add(
                            Particle(
                                x = 0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                                y = 0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                                vx = (Random.nextFloat() - 0.5f) * 0.03f,
                                vy = -0.02f,
                                color = listOf(OddbodsZeeLime, Color.White, GoldCoinColor).random(),
                                life = 1f
                            )
                        )
                    }
                }
                AbilityEffectType.DISCO_GROOVE -> {
                    // Musical notes and disco lights
                    repeat(3) {
                        effectParticles.add(
                            Particle(
                                x = Random.nextFloat(),
                                y = Random.nextFloat() * 0.8f + 0.1f,
                                vx = (Random.nextFloat() - 0.5f) * 0.04f,
                                vy = (Random.nextFloat() - 0.5f) * 0.04f,
                                color = listOf(OddbodsSlickOrange, Color(0xFFFF4081), Color(0xFF00E5FF)).random(),
                                life = 1f
                            )
                        )
                    }
                }
                AbilityEffectType.PRECISION_CLEAN -> {
                    // Clean sparkles and crystal stars
                    repeat(3) {
                        effectParticles.add(
                            Particle(
                                x = Random.nextFloat(),
                                y = Random.nextFloat(),
                                vx = 0f,
                                vy = -0.015f,
                                color = listOf(OddbodsJeffPurple, Color(0xFFE1BEE7), Color.White).random(),
                                life = 1f
                            )
                        )
                    }
                }
                AbilityEffectType.CUPCAKE_RUSH -> {
                    // Cupcake sprinkles & heart glitter
                    repeat(3) {
                        effectParticles.add(
                            Particle(
                                x = Random.nextFloat(),
                                y = 0.1f,
                                vx = (Random.nextFloat() - 0.5f) * 0.02f,
                                vy = Random.nextFloat() * 0.03f + 0.02f,
                                color = listOf(OddbodsNewtPink, Color(0xFFFF80AB), Color.White).random(),
                                life = 1f
                            )
                        )
                    }
                }
            }

            // Update particles
            val dead = mutableListOf<Particle>()
            for (i in effectParticles.indices) {
                val p = effectParticles[i]
                val nl = p.life - 0.05f
                if (nl <= 0f) dead.add(p)
                else effectParticles[i] = p.copy(x = p.x + p.vx, y = p.y + p.vy, life = nl)
            }
            effectParticles.removeAll(dead)
        }

        isAbilityActive = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Character Ability Lab",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_ability_lab")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = selectedCharacter.suitColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Character Selector Row
            Text(
                text = "Select an Oddbod to Test:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allCharacters) { char ->
                    val isSelected = char.id == selectedCharacter.id
                    Card(
                        modifier = Modifier
                            .clickable {
                                selectedCharacter = char
                                isAbilityActive = false
                                effectParticles.clear()
                                viewModel.soundEffects.playBoing()
                            }
                            .testTag("select_char_${char.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) char.suitColor else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color.White) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OddbodCharacterView(
                                character = char,
                                size = 52.dp,
                                animate = isSelected
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = char.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Playground Sandbox
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16122E)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            isAbilityActive = true
                            viewModel.soundEffects.playAbilityTrigger()
                        }
                        .testTag("interactive_ability_sandbox"),
                    contentAlignment = Alignment.Center
                ) {
                    // Playground Canvas with live effects
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Grid lines
                        for (i in 1..5) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(w * (i / 6f), 0f),
                                end = Offset(w * (i / 6f), h),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, h * (i / 6f)),
                                end = Offset(w, h * (i / 6f)),
                                strokeWidth = 2f
                            )
                        }

                        // Specific background blast when active
                        if (isAbilityActive) {
                            when (selectedCharacter.abilityEffectType) {
                                AbilityEffectType.FURY_BURST -> {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(RageFireRed.copy(alpha = 0.45f), Color.Transparent)
                                        ),
                                        radius = w * 0.6f,
                                        center = Offset(w * 0.5f, h * 0.5f)
                                    )
                                }
                                AbilityEffectType.MAGNETIC_PULSE -> {
                                    drawCircle(
                                        color = TechCyan.copy(alpha = 0.3f),
                                        radius = w * 0.5f,
                                        center = Offset(w * 0.5f, h * 0.5f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                                    )
                                }
                                else -> {}
                            }
                        }

                        // Draw effect particles
                        for (p in effectParticles) {
                            drawCircle(
                                color = p.color.copy(alpha = p.life),
                                radius = 9f * p.life,
                                center = Offset(p.x * w, p.y * h)
                            )
                        }
                    }

                    // Main Oddbod inside the test arena
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        OddbodCharacterView(
                            character = selectedCharacter,
                            size = 110.dp,
                            animate = true,
                            isSuperPowered = isAbilityActive
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAbilityActive) "⚡ ${selectedCharacter.signatureAbilityName.uppercase()}!" else "TAP TO TEST ABILITY",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAbilityActive) selectedCharacter.secondaryColor else Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Test Ability Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        isAbilityActive = true
                        viewModel.soundEffects.playAbilityTrigger()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_trigger_ability_test"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedCharacter.suitColor)
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = "Trigger Ability", tint = Color.Yellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRIGGER ${selectedCharacter.signatureAbilityName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            // Ability & Character Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(selectedCharacter.suitColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = selectedCharacter.suitColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedCharacter.signatureAbilityName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedCharacter.tagline,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = selectedCharacter.signatureAbilityDesc,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CHARACTER STATS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    StatBar(label = "Speed", value = selectedCharacter.speedStat, color = OddbodsPogoCyan)
                    StatBar(label = "Power", value = selectedCharacter.powerStat, color = OddbodsFuseRed)
                    StatBar(label = "Mischief", value = selectedCharacter.mischiefStat, color = OddbodsFuseOrange)
                    StatBar(label = "Tech & IQ", value = selectedCharacter.techStat, color = OddbodsBubblesGold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(90.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$value%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
