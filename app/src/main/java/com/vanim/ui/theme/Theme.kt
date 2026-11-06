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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vanim.R
import com.vanim.data.local.AppSettings
import com.vanim.data.local.ThemeMode

// Vanim's original midnight-cyan palette: crisp blue accents over ink/navy surfaces.
private val DarkScheme = darkColorScheme(
    primary = Color(0xFF55C8FF), onPrimary = Color(0xFF00263A),
    primaryContainer = Color(0xFF064C6A), onPrimaryContainer = Color(0xFFC5ECFF),
    inversePrimary = Color(0xFF00658E), secondary = Color(0xFFB6ED67),
    onSecondary = Color(0xFF213600), secondaryContainer = Color(0xFF203545),
    onSecondaryContainer = Color(0xFFD9F2FF), tertiary = Color(0xFFFFBE78),
    onTertiary = Color(0xFF4A2600), tertiaryContainer = Color(0xFF6B3B08),
    onTertiaryContainer = Color(0xFFFFDDB9), background = Color(0xFF080D15),
    onBackground = Color(0xFFEAF4FB), surface = Color(0xFF080D15),
    onSurface = Color(0xFFEAF4FB), surfaceVariant = Color(0xFF202D3A),
    onSurfaceVariant = Color(0xFFA8BAC8), surfaceTint = Color(0xFF55C8FF),
    inverseSurface = Color(0xFFEAF4FB), inverseOnSurface = Color(0xFF202A33),
    outline = Color(0xFF8398A8), outlineVariant = Color(0xFF263746),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    surfaceBright = Color(0xFF293846), surfaceDim = Color(0xFF080D15),
    surfaceContainerLowest = Color(0xFF050910), surfaceContainerLow = Color(0xFF101924),
    surfaceContainer = Color(0xFF141F2B), surfaceContainerHigh = Color(0xFF1A2734),
    surfaceContainerHighest = Color(0xFF243442),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF006B97), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC7EFFF), onPrimaryContainer = Color(0xFF001F2E),
    inversePrimary = Color(0xFF55C8FF), secondary = Color(0xFF4A6500),
    onSecondary = Color(0xFFFFFFFF), secondaryContainer = Color(0xFFD7F0A0),
    onSecondaryContainer = Color(0xFF182100), tertiary = Color(0xFF885000),
    onTertiary = Color(0xFFFFFFFF), tertiaryContainer = Color(0xFFFFDDB9),
    onTertiaryContainer = Color(0xFF2B1600), background = Color(0xFFF5FAFE),
    onBackground = Color(0xFF131C24), surface = Color(0xFFF5FAFE),
    onSurface = Color(0xFF131C24), surfaceVariant = Color(0xFFDFEAF1),
    onSurfaceVariant = Color(0xFF3D4D59), surfaceTint = Color(0xFF006B97),
    inverseSurface = Color(0xFF27333C), inverseOnSurface = Color(0xFFECF3F8),
    outline = Color(0xFF667986), outlineVariant = Color(0xFFC5D3DC),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    surfaceBright = Color(0xFFF5FAFE), surfaceDim = Color(0xFFD6E1E8),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFEEF5FA),
    surfaceContainer = Color(0xFFE8F0F5), surfaceContainerHigh = Color(0xFFE1EAF0),
    surfaceContainerHighest = Color(0xFFD9E4EB),
)

private fun ColorScheme.toAmoled() = copy(
    background = Color.Black, surface = Color.Black, surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black, surfaceContainerLow = Color(0xFF080D15),
    surfaceContainer = Color(0xFF0D141D), surfaceContainerHigh = Color(0xFF131D28),
    surfaceContainerHighest = Color(0xFF1C2935),
)

val PlusJakarta = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

private val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = PlusJakarta),
        displayMedium = displayMedium.copy(fontFamily = PlusJakarta),
        displaySmall = displaySmall.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.035).em),
        headlineLarge = headlineLarge.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.03).em),
        headlineMedium = headlineMedium.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.025).em),
        headlineSmall = headlineSmall.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
        titleLarge = titleLarge.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = PlusJakarta),
        bodyMedium = bodyMedium.copy(fontFamily = PlusJakarta),
        bodySmall = bodySmall.copy(fontFamily = PlusJakarta),
        labelLarge = labelLarge.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(fontFamily = PlusJakarta),
        labelSmall = TextStyle(fontFamily = PlusJakarta, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.45.sp),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
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
