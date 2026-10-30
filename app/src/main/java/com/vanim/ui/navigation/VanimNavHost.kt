package com.vanim.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import com.vanim.ui.components.glass
import com.vanim.ui.motion.passTouchesThrough
import com.vanim.ui.motion.sharedEpisodeKey
import com.vanim.VanimApp
import com.vanim.data.Anime
import com.vanim.ui.motion.LocalNavScope
import com.vanim.ui.motion.LocalReduceMotion
import com.vanim.ui.motion.LocalSharedScope
import com.vanim.ui.motion.Motion
import com.vanim.ui.motion.rememberReduceMotion
import com.vanim.data.Episode
import com.vanim.ui.detail.DetailRoute
import com.vanim.ui.explore.ExploreRoute
import com.vanim.ui.home.HomeRoute
import com.vanim.ui.library.LibraryRoute
import com.vanim.ui.player.MiniPlayer
import com.vanim.ui.player.PlayerRoute
import com.vanim.ui.schedule.ScheduleRoute
import com.vanim.ui.settings.SettingsRoute

private object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore?genre={genre}&focus={focus}"
    const val SCHEDULE = "schedule"
    const val LIBRARY = "library"
    const val DETAIL = "detail/{animeId}?from={from}"
    const val PLAYER = "player/{animeId}/{episodeId}?number={number}&thumb={thumb}&shared={shared}"
    const val SETTINGS = "settings"

    fun explore(genre: String? = null, focus: Boolean = false) =
        "explore?genre=${Uri.encode(genre.orEmpty())}&focus=$focus"

    /** [from] names the row the poster was tapped in; it keys the shared poster transition. */
    fun detail(animeId: String, from: String? = null) =
        "detail/${Uri.encode(animeId)}?from=${Uri.encode(from.orEmpty())}"

    /** [thumb] is shown in the video area until the first frame; [shared] is the key of the image that flies into it. */
    fun player(animeId: String, episodeId: String, number: String, thumb: String = "", shared: String? = null) =
        "player/${Uri.encode(animeId)}/${Uri.encode(episodeId)}?number=${Uri.encode(number)}" +
            "&thumb=${Uri.encode(thumb)}&shared=${Uri.encode(shared.orEmpty())}"
}

private enum class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME(Routes.HOME, "Beranda", Icons.Outlined.Home, Icons.Rounded.Home),
    EXPLORE(Routes.EXPLORE, "Jelajah", Icons.Outlined.Explore, Icons.Rounded.Explore),
    SCHEDULE(Routes.SCHEDULE, "Jadwal", Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
    LIBRARY(Routes.LIBRARY, "Koleksi", Icons.Outlined.VideoLibrary, Icons.Rounded.VideoLibrary),
}

/** Detail and player sit on top of the tabs; moving to or from them is a fade under a shared element. */
private fun NavBackStackEntry.isOverlay() = destination.route == Routes.DETAIL || destination.route == Routes.PLAYER

private val FadeThroughIn = fadeIn(tween(Motion.Medium, 90, Motion.EmphasizedDecelerate)) +
    scaleIn(tween(Motion.Medium, 90, Motion.EmphasizedDecelerate), initialScale = 0.94f)
private val FadeThroughOut = fadeOut(tween(90, easing = Motion.EmphasizedAccelerate))
// The page underneath is always fully visible, so the flying poster never floats over an empty screen (the old
// fade-through left ~100 ms of blank). Opening: the new page slides in ~10% and covers it in 150 ms. Closing: the
// page beneath is there at once (slides back, no fade) while the top page slides off and fades in 150 ms.
private val PageIn = slideInHorizontally(tween(Motion.Page, easing = Motion.EmphasizedDecelerate)) { it / 10 } +
    fadeIn(tween(Motion.Cover, easing = LinearEasing))
private val PageOut = fadeOut(tween(Motion.Long), targetAlpha = 1f) // stays put and visible until the poster lands
private val PageBackIn = slideInHorizontally(tween(Motion.Page, easing = Motion.EmphasizedDecelerate)) { -it / 10 }
private val PageBackOut = slideOutHorizontally(tween(Motion.Page, easing = Motion.EmphasizedAccelerate)) { it / 10 } +
    fadeOut(tween(Motion.Cover, easing = LinearEasing))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.involvesOverlay() =
    initialState.isOverlay() || targetState.isOverlay()

/**
 * Exposes the destination's enter/exit scope to shared elements and entrance animations inside it, on an opaque
 * background so a page fading in covers the one beneath instead of mixing with it.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedContentScope.Scoped(content: @Composable () -> Unit) {
    // A leaving page stays composed (its shared element may still be flying) but takes no touches, so the page
    // beneath is usable at once. Read from this destination's own transition: LocalNavScope is provided below.
    val exiting = transition.targetState == EnterExitState.PostExit
    CompositionLocalProvider(LocalNavScope provides this) {
        Box(
            Modifier
                .passTouchesThrough(exiting)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) { content() }
    }
}

private fun NavBackStackEntry.from(): String? = arguments?.getString("from")?.takeIf { it.isNotBlank() }

private fun NavHostController.navigateTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun VanimNavHost(initialAnimeId: String? = null, onInitialAnimeConsumed: () -> Unit = {}) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val currentTab = Tab.entries.firstOrNull { tab -> destination?.hierarchy?.any { it.route == tab.route } == true }

    // Deep link from a "new episode" notification: jump straight to the detail screen.
    LaunchedEffect(initialAnimeId) {
        if (initialAnimeId != null) {
            nav.navigate(Routes.detail(initialAnimeId))
            onInitialAnimeConsumed()
        }
    }

    val app = LocalContext.current.applicationContext as VanimApp
    val animeRepository = app.container.animeRepository
    val session = app.container.playbackSession
    val mini by session.active.collectAsStateWithLifecycle()
    val miniPlaying by session.playing.collectAsStateWithLifecycle()
    val miniVisible = session.minimized && mini != null && destination?.route != Routes.PLAYER
    val openAnime: (Anime, String) -> Unit = { anime, from ->
        // The detail header needs the poster on its first frame to be the shared poster's landing spot.
        animeRepository.prime(anime)
        nav.navigate(Routes.detail(anime.id, from))
    }
    val play: (String, Episode, String?, String) -> Unit = { animeId, ep, shared, thumb ->
        nav.navigate(Routes.player(animeId, ep.id, ep.number, thumb, shared))
    }
    val reduceMotion = rememberReduceMotion()
    // Fresh Explore entry (no restoreState) so the new genre/focus arguments take effect.
    val openExplore: (String?, Boolean) -> Unit = { genre, focus ->
        nav.navigate(Routes.explore(genre, focus)) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = miniVisible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    val active = mini
                    if (active != null) {
                        Box(
                            Modifier.then(
                                if (currentTab == null) Modifier.navigationBarsPadding() else Modifier,
                            ),
                        ) {
                            MiniPlayer(
                                session = session,
                                active = active,
                                playing = miniPlaying,
                                onExpand = {
                                    session.expand()
                                    nav.navigate(
                                        Routes.player(
                                            active.animeId,
                                            active.episodeId,
                                            active.episodeNumber,
                                            active.thumbnail,
                                        ),
                                    ) { launchSingleTop = true }
                                },
                                onClose = session::stop,
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = currentTab != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .glass(RoundedCornerShape(24.dp), alpha = 0.82f),
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0),
                        ) {
                            Tab.entries.forEach { tab ->
                                val selected = tab == currentTab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (tab == Tab.EXPLORE) nav.navigateTab(Routes.explore()) else nav.navigateTab(tab.route)
                                    },
                                    icon = { Icon(if (selected) tab.selectedIcon else tab.icon, null) },
                                    label = { Text(tab.label) },
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedScope provides this, LocalReduceMotion provides reduceMotion) {
                    NavHost(
                        navController = nav,
                        startDestination = Routes.HOME,
                        // Tabs fade through; detail/player move along the X axis under their flying shared element.
                        enterTransition = { if (involvesOverlay()) PageIn else FadeThroughIn },
                        exitTransition = { if (involvesOverlay()) PageOut else FadeThroughOut },
                        popEnterTransition = { if (involvesOverlay()) PageBackIn else FadeThroughIn },
                        popExitTransition = { if (involvesOverlay()) PageBackOut else FadeThroughOut },
                    ) {
                        composable(Routes.HOME) {
                            Scoped {
                                HomeRoute(
                                    onAnimeClick = openAnime,
                                    onContinue = { e ->
                                        nav.navigate(
                                            Routes.player(
                                                e.animeId, e.episodeId, e.episodeNumber,
                                                e.episodeThumbnail.ifBlank { e.animeCover },
                                                shared = sharedEpisodeKey(e.episodeId, "home-cw"),
                                            ),
                                        )
                                    },
                                    onSearch = { openExplore(null, true) },
                                    onSettings = { nav.navigate(Routes.SETTINGS) },
                                    onSchedule = { nav.navigateTab(Routes.SCHEDULE) },
                                    onExplore = { g -> openExplore(g, false) },
                                )
                            }
                        }
                        composable(
                            Routes.EXPLORE,
                            arguments = listOf(
                                navArgument("genre") { type = NavType.StringType; defaultValue = "" },
                                navArgument("focus") { type = NavType.BoolType; defaultValue = false },
                            ),
                        ) { entry ->
                            Scoped {
                                ExploreRoute(focusSearch = entry.arguments?.getBoolean("focus") == true, onAnimeClick = openAnime)
                            }
                        }
                        composable(Routes.SCHEDULE) { Scoped { ScheduleRoute(onAnimeClick = openAnime) } }
                        composable(Routes.LIBRARY) {
                            Scoped {
                                LibraryRoute(
                                    onAnimeClick = openAnime,
                                    onResume = { e ->
                                        nav.navigate(
                                            Routes.player(
                                                e.animeId, e.episodeId, e.episodeNumber,
                                                e.episodeThumbnail.ifBlank { e.animeCover },
                                                shared = sharedEpisodeKey(e.episodeId, "library"),
                                            ),
                                        )
                                    },
                                    onPlayDownload = { d -> nav.navigate(Routes.player(d.animeId, d.episodeId, d.episodeNumber)) },
                                )
                            }
                        }
                        composable(
                            Routes.DETAIL,
                            arguments = listOf(
                                navArgument("animeId") { type = NavType.StringType },
                                navArgument("from") { type = NavType.StringType; defaultValue = "" },
                            ),
                        ) { entry ->
                            Scoped {
                                DetailRoute(
                                    origin = entry.from(),
                                    entryKey = entry.id,
                                    onBack = { nav.popBackStack() },
                                    onPlay = { anime, ep, shared, thumb -> play(anime.id, ep, shared, thumb) },
                                    onAnimeClick = openAnime,
                                    onGenre = { g -> openExplore(g, false) },
                                )
                            }
                        }
                        composable(
                            Routes.PLAYER,
                            arguments = listOf(
                                navArgument("animeId") { type = NavType.StringType },
                                navArgument("episodeId") { type = NavType.StringType },
                                navArgument("number") { type = NavType.StringType; defaultValue = "" },
                                navArgument("thumb") { type = NavType.StringType; defaultValue = "" },
                                navArgument("shared") { type = NavType.StringType; defaultValue = "" },
                            ),
                        ) { entry ->
                            Scoped {
                                PlayerRoute(
                                    sharedKey = entry.arguments?.getString("shared")?.takeIf { it.isNotBlank() },
                                    onBack = { nav.popBackStack() },
                                    onMinimize = {
                                        session.minimize()
                                        nav.popBackStack()
                                    },
                                    onOpenAnime = { anime ->
                                        session.minimize()
                                        nav.popBackStack()
                                        openAnime(anime, "similar-player")
                                    },
                                )
                            }
                        }
                        composable(Routes.SETTINGS) { Scoped { SettingsRoute(onBack = { nav.popBackStack() }) } }
                    }
                }
            }
        }
    }
}
