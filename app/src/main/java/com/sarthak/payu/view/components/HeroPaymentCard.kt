package com.sarthak.payu.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.PaymentMethodType
import com.sarthak.payu.data.model.paymentMethodBackNumber
import com.sarthak.payu.data.model.paymentMethodDisplayName
import com.sarthak.payu.data.model.paymentMethodExpiryDisplay
import com.sarthak.payu.data.model.paymentMethodFrontNumber

private data class HeroPalette(
    val topDark: List<Color>,
    val bottomLight: List<Color>,
    val accent: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlippableHeroPaymentCard(
    method: PaymentMethod,
    userName: String,
    selectedMethodId: String?,
    centered: Boolean,
    dullness: Float,
    monthlySpent: Double,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val density = LocalDensity.current
    val isSelected = selectedMethodId == method.id
    var isFlipped by rememberSaveable(method.id) { mutableStateOf(false) }
    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 240f),
        label = "heroCardRotation"
    )
    val normalizedAngle = ((flipRotation % 360f) + 360f) % 360f
    val isBackVisible = normalizedAngle in 90f..270f
    val cardScale by animateFloatAsState(
        targetValue = if (centered) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 120f),
        label = "heroCardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (centered) 1f else 0.78f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 110f),
        label = "heroCardAlpha"
    )

    LaunchedEffect(selectedMethodId) {
        if (!isSelected) isFlipped = false
    }

    val shimmerTransition = rememberInfiniteTransition(label = "heroCardShimmer")
    val shimmerPhase by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heroCardShimmerPhase"
    )

    val palette = remember(method.bankName) { heroPaletteFor(method.bankName) }
    val title = method.bankName.ifBlank { "Payment Method" }

    Box(
        modifier = modifier
        .graphicsLayer {
                alpha = cardAlpha * (1f - (dullness * 0.16f))
                scaleX = cardScale
                scaleY = cardScale
                rotationY = flipRotation
                cameraDistance = 16f * density.density
            }
            .combinedClickable(
                onClick = { isFlipped = !isFlipped },
                onDoubleClick = { onTap() }
            )
    ) {
        if (isBackVisible) {
            HeroCardBack(
                method = method,
                userName = userName,
                palette = palette,
                rotationY = 180f,
                shimmerPhase = shimmerPhase,
                title = title,
                monthlySpent = monthlySpent
            )
        } else {
            HeroCardFront(
                method = method,
                userName = userName,
                palette = palette,
                shimmerPhase = shimmerPhase,
                title = title,
                monthlySpent = monthlySpent
            )
        }
    }
}

@Composable
private fun HeroCardFront(
    method: PaymentMethod,
    userName: String,
    palette: HeroPalette,
    shimmerPhase: Float,
    title: String,
    monthlySpent: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(204.dp)
            .shadowLikeCard()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = palette.bottomLight,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .drawWithContent {
                drawContent()
                val width = size.width
                val height = size.height
                val sheenStart = -width + (width * 3f * shimmerPhase)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        start = Offset(sheenStart, 0f),
                        end = Offset(sheenStart + width * 0.5f, height)
                    ),
                    blendMode = BlendMode.Screen
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val wave = heroWavePath(width, height)
            drawPath(wave, brush = Brush.verticalGradient(palette.topDark))
            clipPath(wave) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(width, height)
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        BankLogoBadge(
                            bankName = method.bankName,
                            size = 18.dp,
                            logoSize = 12.dp,
                            background = Color.Transparent
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Text(
                        title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
                Text(
                    "VIRTUAL CARD",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "ACCOUNT HOLDER",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        userName.ifBlank { "User" }.uppercase(),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "TOTAL BALANCE",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "₹${"%,.2f".format(method.balance)}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "ACCOUNT NO.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        paymentMethodFrontNumber(method),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Spent",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        "₹${"%,.0f".format(monthlySpent)}",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCardBack(
    method: PaymentMethod,
    userName: String,
    palette: HeroPalette,
    rotationY: Float,
    shimmerPhase: Float,
    title: String,
    monthlySpent: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(204.dp)
            .graphicsLayer { this.rotationY = rotationY }
            .shadowLikeCard()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE8EDF6), Color(0xFFD1D8E6), Color(0xFFCBD5E1)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .drawWithContent {
                drawContent()
                val width = size.width
                val height = size.height
                val sheenStart = -width + (width * 3f * shimmerPhase)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        start = Offset(sheenStart, 0f),
                        end = Offset(sheenStart + width * 0.45f, height)
                    ),
                    blendMode = BlendMode.Screen
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040404),
                        Color(0xFF000000),
                        Color(0xFF000000),
                        Color(0xFF2E2E2E)
                    ),
                    startY = size.height * 0.08f,
                    endY = size.height * 0.22f
                ),
                topLeft = Offset(0f, size.height * 0.08f),
                size = Size(size.width, size.height * 0.13f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "VIRTUAL CARD",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        when (method.type) {
                            PaymentMethodType.CARD -> "CARD NUMBER"
                            else -> "ACCOUNT NUMBER"
                        },
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        paymentMethodBackNumber(method),
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "CVV",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        method.cvv.ifBlank { "xxx" },
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "EXPIRY",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        paymentMethodExpiryDisplay(method),
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "TOTAL SPENT",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "₹${"%,.0f".format(monthlySpent)}",
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

private fun heroPaletteFor(bankName: String): HeroPalette {
    val variants = listOf(
        HeroPalette(
            topDark = listOf(Color(0xFF0F172A), Color(0xFF1D204A), Color(0xFF4338CA)),
            bottomLight = listOf(Color(0xFF7DD3FC), Color(0xFF60A5FA), Color(0xFF7C3AED)),
            accent = Color(0xFF7C3AED)
        ),
        HeroPalette(
            topDark = listOf(Color(0xFF10172F), Color(0xFF23304E), Color(0xFF5B21B6)),
            bottomLight = listOf(Color(0xFF5EEAD4), Color(0xFF3B82F6), Color(0xFF8B5CF6)),
            accent = Color(0xFF2563EB)
        ),
        HeroPalette(
            topDark = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF312E81)),
            bottomLight = listOf(Color(0xFFA78BFA), Color(0xFF60A5FA), Color(0xFF38BDF8)),
            accent = Color(0xFF38BDF8)
        )
    )
    val index = kotlin.math.abs(bankName.lowercase().hashCode()) % variants.size
    return variants[index]
}

private fun heroWavePath(width: Float, height: Float): Path {
    return Path().apply {
        moveTo(0f, 0f)
        lineTo(width, 0f)
        lineTo(width, height * 0.13f)
        cubicTo(
            width * 0.96f,
            height * 0.14f,
            width * 0.95f,
            height * 0.20f,
            width * 0.91f,
            height * 0.46f
        )
        cubicTo(
            width * 0.87f,
            height * 0.68f,
            width * 0.78f,
            height * 0.66f,
            width * 0.75f,
            height * 0.42f
        )
        cubicTo(
            width * 0.70f,
            height * 0.18f,
            width * 0.61f,
            height * 0.20f,
            width * 0.56f,
            height * 0.53f
        )
        cubicTo(
            width * 0.52f,
            height * 0.80f,
            width * 0.43f,
            height * 0.80f,
            width * 0.39f,
            height * 0.50f
        )
        cubicTo(
            width * 0.35f,
            height * 0.21f,
            width * 0.27f,
            height * 0.20f,
            width * 0.22f,
            height * 0.47f
        )
        cubicTo(
            width * 0.17f,
            height * 0.72f,
            width * 0.08f,
            height * 0.68f,
            width * 0.05f,
            height * 0.40f
        )
        cubicTo(0f, height * 0.18f, 0f, height * 0.18f, 0f, height * 0.13f)
        close()
    }
}

private fun Modifier.shadowLikeCard(): Modifier = this
