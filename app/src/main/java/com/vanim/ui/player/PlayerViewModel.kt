package com.vanim.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.vanim.data.Anime
import com.vanim.data.DownloadItem
import com.vanim.data.DownloadRepository
import com.vanim.data.closestTo
import com.vanim.data.AnimeRepository
import com.vanim.data.Episode
import com.vanim.data.LibraryRepository
import com.vanim.data.OpMark
import com.vanim.data.nextLowerThan
import com.vanim.data.rankSimilar
import com.vanim.data.StreamSource
import com.vanim.data.local.AppSettings
import com.vanim.data.local.SettingsStore
import com.vanim.util.friendlyMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class PlayerUiState(
    val anime: Anime? = null,
    val episode: Episode,
    val sources: List<StreamSource> = emptyList(),
    val source: StreamSource? = null,
    /** Position to seek to once the current source is prepared. */
    val startPositionMs: Long = 0,
    val loading: Boolean = true,
    val error: String? = null,
    val next: Episode? = null,
    val previous: Episode? = null,
    /** Episodes of the current page, for the portrait episode strip. */
    val nearby: List<Episode> = emptyList(),
    /** Other titles with similar genres, shown below the episode strip. */
    val similar: List<Anime> = emptyList(),
    /** Playing a downloaded file; no network needed. */
    val offline: Boolean = false,
    /** One-shot message for the snackbar (download started/failed). */
    val message: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(
    private val repo: AnimeRepository,
    private val library: LibraryRepository,
    private val downloads: DownloadRepository,
    private val settingsStore: SettingsStore,
    private val appScope: CoroutineScope,
    private val session: PlaybackSession,
    /** Whether the active network is metered (mobile data), for the mobile-data quality setting. */
    private val isMetered: () -> Boolean,
    saved: SavedStateHandle,
) : ViewModel() {

    private val animeId: String = checkNotNull(saved["animeId"])

    private val _state = MutableStateFlow(
        PlayerUiState(
            anime = repo.cachedAnime(animeId),
            episode = Episode(
                id = checkNotNull(saved["episodeId"]),
                number = saved.get<String>("number").orEmpty(),
                title = "Episode ${saved.get<String>("number").orEmpty()}",
                // Shown in the video area until the first frame, so the tapped thumbnail has somewhere to land.
                thumbnail = saved.get<String>("thumb").orEmpty(),
            ),
        ),
    )
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    val settings: StateFlow<AppSettings> =
        settingsStore.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    /** Download state of whatever episode is on screen. */
    val download: StateFlow<DownloadItem?> = _state.map { it.episode.id }.distinctUntilChanged()
        .flatMapLatest { downloads.forEpisode(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The user's opening mark for this anime, shared by all its episodes. */
    val opMark: StateFlow<OpMark?> =
        settingsStore.opMark(animeId).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var loadJob: Job? = null
    private var similarJob: Job? = null

    /** Height the user picked by hand in this player session; kept for the following episodes. */
    private var sessionHeight: Int? = null

    /** Sources of the current episode that already failed, so fallback never loops. */
    private val failedUrls = mutableSetOf<String>()

    init {
        val restored = session.snapshot?.takeIf { it.episode.id == _state.value.episode.id }
        if (restored != null) {
            _state.value = restored.copy(
                startPositionMs = session.player.currentPosition.coerceAtLeast(0),
                message = null,
            )
            restored.anime?.let(::loadSimilar)
        } else {
            val cached = _state.value.anime
            if (cached == null) {
                viewModelScope.launch {
                    repo.detail(animeId).onSuccess { a ->
                        _state.update { it.copy(anime = a) }
                        loadSimilar(a)
                    }
                }
            } else {
                loadSimilar(cached)
            }
            play(_state.value.episode)
        }
    }

    fun play(episode: Episode) {
        loadJob?.cancel()
        failedUrls.clear()
        _state.update {
            it.copy(
                episode = episode, sources = emptyList(), source = null, loading = true, error = null,
                next = null, previous = null, offline = false,
            )
        }
        loadJob = viewModelScope.launch {
            val resumeAt = library.savedPosition(episode.id)
            if (playOffline(episode, resumeAt)) return@launch
            val stream = repo.stream(episode.id).getOrElse { e ->
                _state.update { it.copy(loading = false, error = e.friendlyMessage()) }
                return@launch
            }
            if (stream.sources.isEmpty()) {
                _state.update { it.copy(loading = false, error = "Belum ada sumber video untuk episode ini.") }
                return@launch
            }
            val merged = stream.episode?.let { info ->
                episode.copy(
                    title = info.title.ifBlank { episode.title },
                    thumbnail = info.thumbnail.ifBlank { episode.thumbnail },
                    number = episode.number.ifBlank { info.number },
                )
            } ?: episode
            val preferred = preferredHeight(settings.first(), isMetered(), sessionHeight)
            _state.update {
                it.copy(
                    episode = merged,
                    sources = stream.sources,
                    source = pickSource(stream.sources, preferred),
                    startPositionMs = resumeAt,
                    loading = false,
                )
            }
            loadNeighbours(merged)
        }
    }

    private fun pickSource(sources: List<StreamSource>, preferredHeight: Int): StreamSource =
        sources.closestTo(preferredHeight) ?: sources.first()

    /** Plays the downloaded file when there is one. Returns false to fall back to streaming. */
    private suspend fun playOffline(episode: Episode, resumeAt: Long): Boolean {
        val saved = downloads.completed(episode.id) ?: return false
        val e = saved.entity
        val local = StreamSource(Uri.fromFile(File(e.filePath)).toString(), "${e.quality} • Offline", "Perangkat")
        _state.update {
            it.copy(
                // Offline there may be no detail response, so rebuild the essentials from the download row.
                anime = it.anime ?: Anime(e.animeId, e.animeTitle, poster = e.animePoster),
                episode = episode.copy(
                    number = episode.number.ifBlank { e.episodeNumber },
                    title = e.episodeTitle.ifBlank { episode.title },
                    thumbnail = episode.thumbnail.ifBlank { e.episodeThumbnail },
                ),
                sources = listOf(local),
                source = local,
                startPositionMs = resumeAt,
                loading = false,
                offline = true,
            )
        }
        loadNeighbours(_state.value.episode)
        return true
    }

    fun downloadCurrent(source: StreamSource) {
        val s = _state.value
        val anime = s.anime ?: return
        viewModelScope.launch {
            val wifiOnly = settings.value.downloadWifiOnly
            downloads.enqueue(anime, s.episode, source, wifiOnly)
                .onSuccess { postMessage(if (wifiOnly) "Download dimulai (menunggu Wi-Fi)" else "Download ${source.quality} dimulai") }
                .onFailure { postMessage("Gagal download: ${it.friendlyMessage()}") }
        }
    }

    fun deleteDownload() {
        val id = _state.value.episode.id
        viewModelScope.launch {
            downloads.delete(id)
            postMessage("Download dihapus")
        }
    }

    private fun postMessage(text: String) = _state.update { it.copy(message = text) }
    fun messageShown() = _state.update { it.copy(message = null) }

    private suspend fun loadNeighbours(episode: Episode) {
        val next = repo.adjacentEpisode(animeId, episode, newer = true)
        val prev = repo.adjacentEpisode(animeId, episode, newer = false)
        val page = repo.pageOf(animeId, episode.id)
        val nearby = page?.let { repo.episodePage(animeId, it).getOrNull() }?.sortedBy { it.sortKey }.orEmpty()
        if (_state.value.episode.id == episode.id) {
            _state.update { it.copy(next = next, previous = prev, nearby = nearby) }
        }
    }

    private fun loadSimilar(anime: Anime) {
        if (anime.genres.isEmpty()) return
        similarJob?.cancel()
        similarJob = viewModelScope.launch {
            val candidates = repo.similarCandidates(anime).getOrNull().orEmpty()
            val similar = rankSimilar(anime, candidates, exclude = emptySet())
            _state.update { if (it.anime?.id == anime.id) it.copy(similar = similar) else it }
        }
    }

    fun selectSource(source: StreamSource, currentPositionMs: Long) {
        if (source.height > 0) sessionHeight = source.height
        _state.update { it.copy(source = source, startPositionMs = currentPositionMs) }
    }

    /**
     * Switches to the next lower quality after a playback error. Returns false when playing offline
     * or when nothing lower is left, so the caller shows the error instead.
     */
    fun fallBack(positionMs: Long): Boolean {
        val s = _state.value
        val current = s.source ?: return false
        if (s.offline) return false
        failedUrls += current.url
        val lower = s.sources.nextLowerThan(current, failedUrls) ?: return false
        _state.update {
            it.copy(
                source = lower,
                startPositionMs = positionMs,
                message = "Gagal memutar ${current.quality}, beralih ke ${lower.quality}",
            )
        }
        return true
    }

    fun markOpStart(positionMs: Long) = saveOpMark(markOp(opMark.value, positionMs, isStart = true))
    fun markOpEnd(positionMs: Long) = saveOpMark(markOp(opMark.value, positionMs, isStart = false))
    fun clearOpMark() {
        viewModelScope.launch { settingsStore.clearOpMark(animeId) }
    }

    private fun saveOpMark(mark: OpMark) {
        viewModelScope.launch { settingsStore.setOpMark(animeId, mark) }
    }

    fun playNext() {
        _state.value.next?.let(::play)
    }

    fun playPrevious() {
        _state.value.previous?.let(::play)
    }

    fun retry() = play(_state.value.episode)

    fun saveProgress(positionMs: Long, durationMs: Long) {
        val s = _state.value
        val anime = s.anime ?: return
        // Ignore the first few seconds so merely opening an episode doesn't clutter history.
        if (durationMs <= 0 || positionMs < 5_000) return
        appScope.launch { library.saveProgress(anime, s.episode, positionMs, durationMs) }
    }
}
