package com.vanim.ui.legal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vanim.R
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 5_300L

/** Plays the supplied, background-keyed Vanim mark before either the age gate or main navigation. */
@Composable
fun VideoSplashScreen(onFinished: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(animationStarted) {
        if (animationStarted) {
            delay(SPLASH_DURATION_MS)
            if (!finished) {
                finished = true
                onFinished()
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.vanim_splash)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.86f),
                onSuccess = { animationStarted = true },
                onError = {
                    if (!finished) {
                        finished = true
                        onFinished()
                    }
                },
            )
        }
    }
}
