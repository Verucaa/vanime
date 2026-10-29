package com.vanim.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanim.data.local.AppSettings
import com.vanim.data.local.ThemeMode

// "Soft Glass": electric blue on a deep navy night, with translucent layered surfaces.
private val DarkScheme = darkColorScheme(
    primary = Color(0xFF23B9FD),
    onPrimary = Color(0xFF00131F),
    primaryContainer = Color(0xFF0B3C5E),
    onPrimaryContainer = Color(0xFFBFE9FF),
    inversePrimary = Color(0xFF0079B8),
    secondary = Color(0xFF7DE3FF),
    onSecondary = Color(0xFF00252F),
    secondaryContainer = Color(0xFF0B3C5E),
    onSecondaryContainer = Color(0xFFBFE9FF),
    tertiary = Color(0xFF8FA8FF),
    onTertiary = Color(0xFF00105A),
    tertiaryContainer = Color(0xFF1B2C6B),
    onTertiaryContainer = Color(0xFFDDE1FF),
    background = Color(0xFF070B14),
    onBackground = Color(0xFFE6F1FF),
    surface = Color(0xFF0C1424),
    onSurface = Color(0xFFE6F1FF),
    surfaceVariant = Color(0xFF15243A),
    onSurfaceVariant = Color(0xFFA9BDD6),
    surfaceTint = Color(0xFF23B9FD),
    inverseSurface = Color(0xFFE6F1FF),
    inverseOnSurface = Color(0xFF0A1A2A),
    outline = Color(0xFF5E7691),
    outlineVariant = Color(0xFF263B54),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surfaceBright = Color(0xFF1C2F4A),
    surfaceDim = Color(0xFF070B14),
    surfaceContainerLowest = Color(0xFF050810),
    surfaceContainerLow = Color(0xFF0A111E),
    surfaceContainer = Color(0xFF0F1A2C),
    surfaceContainerHigh = Color(0xFF15243A),
    surfaceContainerHighest = Color(0xFF1C2F4A),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0079B8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC7E7FF),
    onPrimaryContainer = Color(0xFF001E30),
    inversePrimary = Color(0xFF23B9FD),
    secondary = Color(0xFF00677E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC3F0FA),
    onSecondaryContainer = Color(0xFF00363F),
    tertiary = Color(0xFF4457B8),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDDE1FF),
    onTertiaryContainer = Color(0xFF00105A),
    background = Color(0xFFF5FAFF),
    onBackground = Color(0xFF0A1A2A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A1A2A),
    surfaceVariant = Color(0xFFDCE9F5),
    onSurfaceVariant = Color(0xFF3E5368),
    surfaceTint = Color(0xFF0079B8),
    inverseSurface = Color(0xFF0A1A2A),
    inverseOnSurface = Color(0xFFF5FAFF),
    outline = Color(0xFF6B8299),
    outlineVariant = Color(0xFFC3D4E4),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD9E6F2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF6FD),
    surfaceContainer = Color(0xFFE7F1FA),
    surfaceContainerHigh = Color(0xFFDFEBF6),
    surfaceContainerHighest = Color(0xFFD7E4F1),
)

private fun ColorScheme.toAmoled() = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF070B14),
    surfaceContainer = Color(0xFF0A111E),
    surfaceContainerHigh = Color(0xFF101B2C),
    surfaceContainerHighest = Color(0xFF16253A),
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.25).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun VanimTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val context = LocalContext.current
    val base = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkScheme
        else -> LightScheme
    }
    val scheme = if (dark && settings.amoledBlack) base.toAmoled() else base

    MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
}
