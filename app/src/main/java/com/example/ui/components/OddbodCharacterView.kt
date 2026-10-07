package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OddbodCharacter

@Composable
fun OddbodCharacterView(
    character: OddbodCharacter,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animate: Boolean = true,
    isSuperPowered: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oddbod_anim")
    val bounceOffset by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounce"
        )
    } else {
        rememberUpdatedState(0f)
    }

    val glowAlpha by if (isSuperPowered) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
    } else {
        rememberUpdatedState(0f)
    }

    Box(
        modifier = modifier
            .size(size)
            .offset(y = bounceOffset.dp)
            .testTag("oddbod_character_${character.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Super ability aura
        if (isSuperPowered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.2f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                character.secondaryColor.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        if (character.spriteDrawableRes != null) {
            Image(
                painter = painterResource(id = character.spriteDrawableRes),
                contentDescription = "${character.name} Oddbod Sprite",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            // Stylized Cartoon Canvas Oddbod (For Zee, Slick, Jeff, Newt)
            StylizedOddbodCanvas(
                character = character,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun StylizedOddbodCanvas(
    character: OddbodCharacter,
    modifier: Modifier = Modifier
) {
    val suitColor = character.suitColor
    val secondaryColor = character.secondaryColor

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Hood / Body outline
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(secondaryColor, suitColor)
            ),
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.75f),
            cornerRadius = CornerRadius(w * 0.35f, w * 0.35f)
        )

        // Head Horns / Antennas depending on character
        when (character.id) {
            "fuse" -> {
                // Pointy horns
                drawCircle(Color.DarkGray, radius = w * 0.08f, center = Offset(w * 0.25f, h * 0.14f))
                drawCircle(Color.DarkGray, radius = w * 0.08f, center = Offset(w * 0.75f, h * 0.14f))
            }
            "pogo" -> {
                // Two rounded ear nubs
                drawRoundRect(
                    color = suitColor,
                    topLeft = Offset(w * 0.2f, h * 0.02f),
                    size = Size(w * 0.12f, h * 0.18f),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                )
                drawRoundRect(
                    color = suitColor,
                    topLeft = Offset(w * 0.68f, h * 0.02f),
                    size = Size(w * 0.12f, h * 0.18f),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                )
            }
            "bubbles" -> {
                // Top antenna
                drawLine(
                    color = Color.LightGray,
                    start = Offset(w * 0.5f, h * 0.15f),
                    end = Offset(w * 0.5f, h * 0.02f),
                    strokeWidth = w * 0.04f
                )
                drawCircle(
                    color = secondaryColor,
                    radius = w * 0.09f,
                    center = Offset(w * 0.5f, h * 0.02f)
                )
            }
            "zee" -> {
                // Sleepy ear flaps
                drawCircle(suitColor, radius = w * 0.11f, center = Offset(w * 0.2f, h * 0.2f))
                drawCircle(suitColor, radius = w * 0.11f, center = Offset(w * 0.8f, h * 0.2f))
            }
            "slick" -> {
                // Cool headset / antenna
                drawArc(
                    color = Color(0xFF37474F),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.2f, h * 0.06f),
                    size = Size(w * 0.6f, h * 0.3f),
                    style = Stroke(width = w * 0.06f)
                )
            }
            "jeff" -> {
                // Neat center spike
                val spikePath = Path().apply {
                    moveTo(w * 0.5f, h * 0.03f)
                    lineTo(w * 0.42f, h * 0.16f)
                    lineTo(w * 0.58f, h * 0.16f)
                    close()
                }
                drawPath(spikePath, color = secondaryColor)
            }
            "newt" -> {
                // Cute double loops / bow
                drawCircle(Color(0xFFFF80AB), radius = w * 0.1f, center = Offset(w * 0.3f, h * 0.12f))
                drawCircle(Color(0xFFFF80AB), radius = w * 0.1f, center = Offset(w * 0.7f, h * 0.12f))
            }
        }

        // Face aperture (peach oval opening)
        drawOval(
            color = Color(0xFFFFE0B2),
            topLeft = Offset(w * 0.26f, h * 0.28f),
            size = Size(w * 0.48f, h * 0.46f)
        )

        // Eyes
        val eyeRadius = w * 0.065f
        // Left eye
        drawCircle(Color.White, radius = eyeRadius, center = Offset(w * 0.4f, h * 0.44f))
        drawCircle(Color.Black, radius = eyeRadius * 0.55f, center = Offset(w * 0.41f, h * 0.44f))
        drawCircle(Color.White, radius = eyeRadius * 0.2f, center = Offset(w * 0.42f, h * 0.42f))

        // Right eye
        drawCircle(Color.White, radius = eyeRadius, center = Offset(w * 0.6f, h * 0.44f))
        drawCircle(Color.Black, radius = eyeRadius * 0.55f, center = Offset(w * 0.59f, h * 0.44f))
        drawCircle(Color.White, radius = eyeRadius * 0.2f, center = Offset(w * 0.6f, h * 0.42f))

        // Characteristic mouth
        when (character.id) {
            "zee" -> {
                // Sleepy snoring mouth "O"
                drawCircle(Color(0xFF8D6E63), radius = w * 0.045f, center = Offset(w * 0.5f, h * 0.61f))
            }
            "fuse" -> {
                // Determined smirk / grit
                drawLine(
                    color = Color.Black,
                    start = Offset(w * 0.42f, h * 0.6f),
                    end = Offset(w * 0.58f, h * 0.58f),
                    strokeWidth = w * 0.035f
                )
            }
            else -> {
                // Cheerful open smile
                drawArc(
                    color = Color(0xFFD81B60),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = true,
                    topLeft = Offset(w * 0.4f, h * 0.52f),
                    size = Size(w * 0.2f, h * 0.16f)
                )
            }
        }
    }
}
