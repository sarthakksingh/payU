package com.sarthak.payu.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarthak.payu.R
import com.sarthak.payu.data.model.PaymentMethod
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PayUTopBar(
    paymentMethods: List<PaymentMethod> = emptyList(),
    onDrawerClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {}
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val avatarContainer = if (isDarkTheme) Color.White else Color.Black
    val avatarContent = if (isDarkTheme) Color.Black else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(avatarContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("P", color = avatarContent, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, fontSize = 16.sp)
        }

        Spacer(Modifier.width(10.dp))
        Text("PayU", color = MaterialTheme.colorScheme.onBackground, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            DrawerHandlePill(
                paymentMethods = paymentMethods,
                onOpen = onDrawerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 220.dp, max = 250.dp)
            )
        }

        Spacer(Modifier.width(12.dp))
        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.calendar),
                contentDescription = "Calendar",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DrawerHandlePill(
    paymentMethods: List<PaymentMethod>,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val visibleMethods = remember(paymentMethods) {
        paymentMethods.filter { it.type.name != "CASH" }.take(3)
    }

    LaunchedEffect(dragOffset.value) {
        if (abs(dragOffset.value) < 1f) {
            dragOffset.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isDarkTheme) {
                        listOf(
                            Color(0xFF18162E),
                            Color(0xFF12162A),
                            Color(0xFF0E1322)
                        )
                    } else {
                        listOf(
                            Color(0xFFF2E7D8),
                            Color(0xFFE7D3C1),
                            Color(0xFFD9C2B0)
                        )
                    }
                )
            )
            .shadow(10.dp, RoundedCornerShape(999.dp), spotColor = Color.Black.copy(alpha = 0.25f))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onOpen() }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    scope.launch {
                        val next = (dragOffset.value + delta * 0.18f).coerceIn(-18f, 18f)
                        dragOffset.snapTo(next)
                    }
                },
                onDragStopped = { velocity ->
                    scope.launch {
                        if (abs(velocity) > 150f || abs(dragOffset.value) > 6f) {
                            onOpen()
                        }
                        dragOffset.animateTo(0f, animationSpec = tween(220))
                    }
                }
            )
            .graphicsLayer {
                val p = abs(dragOffset.value) / 18f
                translationY = dragOffset.value * 0.08f
                scaleX = 1f + (p * 0.02f)
                scaleY = 1f + (p * 0.02f)
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    "Payment modes",
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.72f) else Color(0xFF2A221F).copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }

            Spacer(Modifier.width(6.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (visibleMethods.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        visibleMethods.forEach { method ->
                            BankLogoBadge(
                                bankName = method.bankName,
                                size = 24.dp,
                                logoSize = 16.dp
                            )
                        }
                    }
                }
            }

        }
    }
}
