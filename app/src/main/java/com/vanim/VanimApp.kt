package com.vanim

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.vanim.data.AnimeRepository
import com.vanim.data.DownloadRepository
import com.vanim.data.LibraryRepository
import com.vanim.data.local.VanimDatabase
import com.vanim.data.local.SettingsStore
import com.vanim.data.remote.AnimeApiService
import com.vanim.data.UpdateRepository
import com.vanim.data.notify.EpisodeNotifyWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

/** Manual DI: one instance of each dependency for the whole process. */
class AppContainer(app: Application) {
    val httpClient: OkHttpClient = AnimeApiService.createHttpClient()
    val animeRepository = AnimeRepository(AnimeApiService.create(httpClient))
    private val database = VanimDatabase.create(app)
    val libraryRepository = LibraryRepository(database)
    val settingsStore = SettingsStore(app)

    /** For writes that must outlive the screen that started them (e.g. saving progress on exit). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val downloadRepository = DownloadRepository(app, database.downloadDao(), animeRepository, appScope)

    /** In-app update checker against GitHub Releases. */
    val updateRepository = UpdateRepository(settingsStore)

    /** Video requests get the same UA as the API but none of its JSON headers. */
    val videoHttpClient: OkHttpClient = httpClient.newBuilder().apply { interceptors().clear() }.build()
}

class VanimApp : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        EpisodeNotifyWorker.schedule(this)
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient { container.httpClient }
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(256L * 1024 * 1024)
                .build()
        }
        .crossfade(true)
        .respectCacheHeaders(false)
        .build()
}
