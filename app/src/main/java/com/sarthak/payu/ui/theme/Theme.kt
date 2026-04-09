package com.sarthak.payu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand Colors ──────────────────────────────────────────────
val TealGreen = Color(0xFF2DD4BF)
val TealGreenDark = Color(0xFF0D9488)
val PinkAccent = Color(0xFFEC4899)
val PurpleAccent = Color(0xFF8B5CF6)
val GoldAccent = Color(0xFFF59E0B)
val BlueAccent = Color(0xFF3B82F6)

// Gradient pair used on balance card
val CardGradientStart = Color(0xFFD4A76A)  // warm gold
val CardGradientEnd = Color(0xFF2DD4BF)    // teal

// Dark theme surfaces
val DarkBg = Color(0xFF0A0A0A)
val DarkSurface = Color(0xFF1A1A1A)
val DarkCard = Color(0xFF222222)
val DarkBorder = Color(0xFF2A2A2A)

// Light theme surfaces
val LightBg = Color(0xFFF2F0EA)
val LightSurface = Color(0xFFFCFBF8)
val LightCard = Color(0xFFEDE8E1)
val LightBorder = Color(0xFFDCD7CF)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9CA3AF)
val TextPrimaryLight = Color(0xFF111827)
val TextSecondaryLight = Color(0xFF6B7280)
val TextTertiaryLight = Color(0xFF9AA0A6)

// Light card gradient used for prominent account cards
val CardGradientLightStart = Color(0xFFB49AF8)
val CardGradientLightMid = Color(0xFF5B6EEB)
val CardGradientLightEnd = Color(0xFF35A6E8)

// ── Color Schemes ─────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = TealGreen,
    secondary = PinkAccent,
    tertiary = PurpleAccent,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color.Black,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF11857A),
    secondary = Color(0xFFCA2E78),
    tertiary = Color(0xFF6D4ACF),
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onPrimary = Color.White,
    outline = LightBorder
)

@Composable
fun PayUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
