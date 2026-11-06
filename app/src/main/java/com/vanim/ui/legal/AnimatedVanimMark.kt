package com.vanim.ui.legal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.vanim.ui.motion.rememberReduceMotion
import com.vanim.ui.theme.VanimBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Original Compose-drawn animation of the app's V mark; it is not a video splash. */
@Composable
fun AnimatedVanimMark(modifier: Modifier = Modifier.size(112.dp)) {
    val reduceMotion = rememberReduceMotion()
    val leftWing = remember { Animatable(-0.9f) }
    val rightWing = remember { Animatable(0.9f) }
    val center = remember { Animatable(0f) }
    val shimmer = remember { Animatable(0f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            leftWing.snapTo(0f)
            rightWing.snapTo(0f)
            center.snapTo(1f)
            shimmer.snapTo(0f)
        } else {
            leftWing.snapTo(-0.9f)
            rightWing.snapTo(0.9f)
            center.snapTo(0f)
            shimmer.snapTo(0f)
            launch { leftWing.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 290f)) }
            delay(90)
            launch { rightWing.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 290f)) }
            delay(520)
            center.animateTo(1f, tween(durationMillis = 280))
            shimmer.animateTo(1f, tween(durationMillis = 120))
            delay(75)
            shimmer.animateTo(0f, tween(durationMillis = 520))
        }
    }

    Canvas(modifier) {
        val sx = size.width / 100f
        val sy = size.height / 100f
        fun path(points: List<Pair<Float, Float>>) = Path().apply {
            moveTo(points.first().first * sx, points.first().second * sy)
            points.drop(1).forEach { (x, y) -> lineTo(x * sx, y * sy) }
            close()
        }
        val left = path(listOf(18f to 23f, 35f to 31f, 49f to 77f, 43f to 71f, 30f to 38f))
        val right = path(listOf(82f to 23f, 65f to 31f, 51f to 77f, 57f to 71f, 70f to 38f))
        val core = path(listOf(42f to 48f, 50f to 52f, 58f to 48f, 51f to 66f))
        val wings = Brush.verticalGradient(
            colors = listOf(Color(0xFF48DCFF), Color(0xFF1675E8), Color(0xFF183D9A)),
            startY = 20f * sy,
            endY = 80f * sy,
        )
        val coreBrush = Brush.verticalGradient(
            colors = listOf(Color.White, VanimBlue, Color(0xFF75AFFF)),
            startY = 48f * sy,
            endY = 67f * sy,
        )

        translate(left = leftWing.value * size.width * 0.72f, top = -abs(leftWing.value) * size.height * 0.10f) {
            drawPath(left, wings)
        }
        translate(left = rightWing.value * size.width * 0.72f, top = abs(rightWing.value) * size.height * 0.10f) {
            drawPath(right, wings)
        }

        if (center.value > 0f) {
            val glowAlpha = (0.14f + shimmer.value * 0.38f) * center.value
            drawCircle(
                color = VanimBlue.copy(alpha = glowAlpha),
                radius = size.width * (0.12f + shimmer.value * 0.08f),
                center = Offset(size.width * 0.5f, size.height * 0.56f),
            )
            drawPath(core, color = Color.White.copy(alpha = shimmer.value * 0.7f), style = Stroke(width = size.width * 0.055f))
            drawPath(core, coreBrush, alpha = center.value)
        }
    }
}
