package com.vanim.ui.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.vanim.data.Anime
import com.vanim.data.AnimeRepository
import com.vanim.data.Episode
import com.vanim.data.StreamSource
import com.vanim.data.closestTo
import com.vanim.data.local.SettingsStore
import com.vanim.data.remote.AnimeApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

data class ActivePlayback(
    val animeId: String,
    val title: String,
    val episodeId: String,
    val episodeNumber: String,
    val thumbnail: String,
)

/**
 * One ExoPlayer for the process, so playback can outlive the player screen while the user browses.
 * The player screen binds to it; the mini player keeps it visible.
 */
@OptIn(UnstableApi::class)
class PlaybackSession(
    context: Context,
    client: OkHttpClient,
    private val repo: AnimeRepository,
    private val settingsStore: SettingsStore,
    private val scope: CoroutineScope,
) {
    val player: ExoPlayer = run {
        val http = OkHttpDataSource.Factory(client).setUserAgent(AnimeApiService.USER_AGENT)
        // DefaultDataSource routes file:// (offline downloads) locally and everything else through OkHttp.
        val dataSource = DefaultDataSource.Factory(context.applicationContext, http)
        ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
            // Request audio focus: music/podcast from another app pauses instead of overlapping.
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    private val _active = MutableStateFlow<ActivePlayback?>(null)
    val active: StateFlow<ActivePlayback?> = _active.asStateFlow()

    private val _playing = MutableStateFlow(false)
    val playing: StateFlow<Boolean> = _playing.asStateFlow()

    var minimized by mutableStateOf(false)
        private set

    /** Last player-screen state, used to rebuild the screen without reloading the stream. */
    var snapshot: PlayerUiState? = null

    private var preparedEpisodeId: String? = null
    private var preparedSourceUrl: String? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playing.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                // While the full player is gone, keep the episode chain going.
                if (playbackState == Player.STATE_ENDED && minimized) advance()
            }

            override fun onPlayerError(error: PlaybackException) {
                // A retry or fallback must prepare the source again.
                clearPrepared()
            }
        })
    }

    fun updateActive(anime: Anime?, episode: Episode) {
        _active.value = ActivePlayback(
            animeId = anime?.id.orEmpty(),
            title = anime?.title?.takeIf { it.isNotBlank() } ?: episode.title,
            episodeId = episode.id,
            episodeNumber = episode.number,
            thumbnail = episode.thumbnail.ifBlank { anime?.poster.orEmpty() },
        )
    }

    fun markPrepared(episodeId: String, sourceUrl: String) {
        preparedEpisodeId = episodeId
        preparedSourceUrl = sourceUrl
    }

    fun isPrepared(episodeId: String, sourceUrl: String): Boolean =
        preparedEpisodeId == episodeId && preparedSourceUrl == sourceUrl

    fun clearPrepared() {
        preparedEpisodeId = null
        preparedSourceUrl = null
    }

    fun minimize() {
        minimized = true
    }

    fun expand() {
        minimized = false
    }

    fun toggle() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
            player.play()
        }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        _active.value = null
        _playing.value = false
        snapshot = null
        preparedEpisodeId = null
        preparedSourceUrl = null
        minimized = false
    }

    private fun advance() {
        val snap = snapshot ?: return
        val next = snap.next ?: return
        val animeId = snap.anime?.id.orEmpty()
        if (animeId.isBlank()) return
        scope.launch {
            if (!settingsStore.settings.first().autoPlayNext) return@launch
            delay(3_000)
            if (!settingsStore.settings.first().autoPlayNext) return@launch
            val stillEnded = withContext(Dispatchers.Main) {
                minimized && player.playbackState == Player.STATE_ENDED
            }
            if (!stillEnded || snapshot?.episode?.id != snap.episode.id) return@launch
            // shortcut: auto-advance always streams; downloaded next episodes are handled by the full player.
            val stream = repo.stream(next.id).getOrNull() ?: return@launch
            val source = stream.sources.closestTo(snap.source?.height ?: 0) ?: stream.sources.firstOrNull() ?: return@launch
            val episode = stream.episode?.let {
                next.copy(
                    title = it.title.ifBlank { next.title },
                    thumbnail = it.thumbnail.ifBlank { next.thumbnail },
                    number = next.number.ifBlank { it.number },
                )
            } ?: next
            val previous = repo.adjacentEpisode(animeId, episode, newer = false)
            val following = repo.adjacentEpisode(animeId, episode, newer = true)
            val page = repo.pageOf(animeId, episode.id)
            val nearby = page?.let { repo.episodePage(animeId, it).getOrNull() }?.sortedBy { it.sortKey }.orEmpty()
            withContext(Dispatchers.Main) {
                // The user may have opened something else while the stream was loading.
                if (snapshot?.episode?.id != snap.episode.id) return@withContext
                val state = snap.copy(
                    episode = episode,
                    sources = stream.sources,
                    source = source,
                    startPositionMs = 0,
                    loading = false,
                    error = null,
                    next = following,
                    previous = previous,
                    nearby = nearby,
                )
                snapshot = state
                updateActive(state.anime, episode)
                markPrepared(episode.id, source.url)
                player.setMediaItem(MediaItem.fromUri(source.url))
                player.prepare()
                player.play()
            }
        }
    }
}
