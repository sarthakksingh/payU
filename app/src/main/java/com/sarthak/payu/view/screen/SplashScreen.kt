package com.sarthak.payu.view.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarthak.payu.ui.theme.DarkBg
import com.sarthak.payu.ui.theme.LightBg
import com.sarthak.payu.ui.theme.TealGreen
import com.sarthak.payu.ui.theme.TextPrimary
import com.sarthak.payu.ui.theme.TextPrimaryLight
import com.sarthak.payu.ui.theme.TextSecondary
import com.sarthak.payu.ui.theme.TextSecondaryLight
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SplashScreen(
    isDarkTheme: Boolean,
    onFinished: () -> Unit
) {
    val bg = if (isDarkTheme) DarkBg else LightBg
    val primaryText = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val accent = if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF11857A)
    val orbit = rememberInfiniteTransition(label = "splash_orbit")
    val orbitAngle by orbit.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )
    val pulse by orbit.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotate by orbit.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            OrbitGlow(
                color = accent.copy(alpha = if (isDarkTheme) 0.18f else 0.14f),
                size = 180.dp,
                offsetX = (-96).dp,
                offsetY = (-132).dp
            )
            OrbitGlow(
                color = accent.copy(alpha = if (isDarkTheme) 0.12f else 0.10f),
                size = 220.dp,
                offsetX = 88.dp,
                offsetY = 118.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Pay",
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 0.4.sp
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = 44.dp)
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = if (isDarkTheme) 0.18f else 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "U",
                            color = primaryText,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            modifier = Modifier.graphicsLayer { rotationZ = rotate }
                        )

                        OrbitDot(
                            color = TealGreen,
                            orbitAngle = orbitAngle,
                            radius = 24.dp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Track expenses. Own your finances.",
                    color = secondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun OrbitGlow(
    color: Color,
    size: Dp,
    offsetX: Dp,
    offsetY: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun OrbitDot(
    color: Color,
    orbitAngle: Float,
    radius: Dp
) {
    val radians = Math.toRadians(orbitAngle.toDouble())
    val dx = (cos(radians) * radius.value).roundToInt()
    val dy = (sin(radians) * radius.value).roundToInt()
    Box(
        modifier = Modifier
            .offset(x = dx.dp, y = dy.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}
