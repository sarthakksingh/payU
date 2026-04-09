package com.sarthak.payu.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BankLogoBadge(
    bankName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    background: Color = Color.White.copy(alpha = 0.09f),
    logoSize: Dp = 18.dp,
    forceTextFallback: String? = null
) {
    val context = LocalContext.current
    val resId = remember(bankName, context) { bankLogoDrawableRes(context, bankName) }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val fallbackColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        if (resId != null) {
            Image(
                painter = painterResource(resId),
                contentDescription = bankName,
                modifier = Modifier.size(logoSize),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = forceTextFallback ?: bankName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = fallbackColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun bankLogoDrawableRes(context: android.content.Context, bankName: String?): Int? {
    if (bankName.isNullOrBlank()) return null
    val normalized = normalizeBankLogoName(bankName)
    if (normalized != null) {
        val resId = context.resources.getIdentifier(normalized, "drawable", context.packageName)
        if (resId != 0) return resId
    }
    return null
}

private fun normalizeBankLogoName(bankName: String): String? {
    val value = bankName.lowercase()
    return when {
        "sbi" in value -> "sbi_bank"
        "state bank" in value -> "sbi_bank"
        "hdfc" in value -> "hdfc_bank"
        "icici" in value -> "icici_bank"
        "axis" in value -> "axis_bank"
        "kotak" in value -> "kotak_bank"
        "pnb" in value || "punjab national bank" in value -> "pnb_bank"
        "baroda" in value || "bank of baroda" in value -> "baroda_bank"
        "canara" in value -> "canara_bank"
        "idbi" in value -> "idbi_bank"
        "indian bank" in value -> "indian_bank"
        "uco" in value -> "uco_bank"
        "union" in value -> "union_bank"
        "yes" in value -> "yes_bank"
        "hsbc" in value -> "hsbc_bank"
        "indusind" in value -> null
        else -> null
    }
}

@Composable
fun BankLogoText(bankName: String?, modifier: Modifier = Modifier) {
    BankLogoBadge(
        bankName = bankName,
        modifier = modifier,
        size = 40.dp,
        shape = RoundedCornerShape(12.dp),
        background = Color.White.copy(alpha = 0.08f),
        logoSize = 24.dp
    )
}
