package com.vanim.ui.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.vanim.ui.theme.VanimBlue
import kotlinx.coroutines.delay

data class MiniPlayerItem(
    val animeId: String,
    val episodeId: String,
    val episodeNumber: String,
    val title: String,
    val thumbnail: String,
)

@OptIn(UnstableApi::class)
@Composable
fun MiniPlayer(
    item: MiniPlayerItem,
    player: ExoPlayer,
    onPlay: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sharedPlayer = player
    var progress by remember(player) { mutableFloatStateOf(0f) }
    var miniView by remember(player) { mutableStateOf<PlayerView?>(null) }

    LaunchedEffect(player) {
        while (true) {
            val duration = sharedPlayer.duration
            progress = if (duration > 0 && duration != C.TIME_UNSET) {
                (sharedPlayer.currentPosition.toFloat() / duration).coerceIn(0f, 1f)
            } else {
                0f
            }
            delay(300)
        }
    }
    DisposableEffect(player) {
        onDispose { miniView?.setPlayer(null) }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 9.dp, top = 8.dp, end = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(width = 84.dp, height = 50.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.Black),
                ) {
                    AsyncImage(
                        model = item.thumbnail.takeIf { it.isNotBlank() },
                        contentDescription = "Sampul ${item.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                setShutterBackgroundColor(android.graphics.Color.BLACK)
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                                setPlayer(sharedPlayer)
                                miniView = this
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            miniView = view
                            if (view.player !== sharedPlayer) view.setPlayer(sharedPlayer)
                        },
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                    Text(
                        "Episode ${item.episodeNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                IconButton(onClick = onPlay, modifier = Modifier.size(42.dp)) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Buka pemutar ${item.title}, episode ${item.episodeNumber}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(1.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Tutup mini-player",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(2.dp),
                color = VanimBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {},
            )
        }
    }
}
