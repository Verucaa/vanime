package com.vanim.ui.theme

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** The original Vanim wordmark: only V uses the blue accent; the rest stays white on dark surfaces. */
@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 21.sp,
    restColor: Color = Color.White,
) {
    val style = TextStyle(
        fontFamily = Polea,
        fontWeight = FontWeight.ExtraBold,
        fontSize = fontSize,
        letterSpacing = (-0.035).em,
    )
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("V", style = style, color = VanimBlue)
        Text("anim", style = style, color = restColor)
    }
}

/** Styles any visible Vanim name with Polea and the blue-V brand split. */
@Composable
fun brandStyledText(text: String, restColor: Color = MaterialTheme.colorScheme.onSurface): AnnotatedString {
    val pattern = Regex("(?i)Vanim")
    return buildAnnotatedString {
        var cursor = 0
        pattern.findAll(text).forEach { match ->
            append(text.substring(cursor, match.range.first))
            val word = match.value
            withStyle(SpanStyle(fontFamily = Polea, fontWeight = FontWeight.ExtraBold, color = VanimBlue)) {
                append(word.substring(0, 1))
            }
            withStyle(SpanStyle(fontFamily = Polea, fontWeight = FontWeight.ExtraBold, color = restColor)) {
                append(word.substring(1))
            }
            cursor = match.range.last + 1
        }
        append(text.substring(cursor))
    }
}
