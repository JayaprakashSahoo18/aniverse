package com.codex.animestream.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AnimeStreamAppRoot(onEnterPictureInPicture: () -> Unit) {
    AnimeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val nav = rememberNavController()
            NavHost(
                navController = nav,
                startDestination = "home",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(260)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(260)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(260)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(260)) },
            ) {
                composable("home") {
                    HomeScreen(
                        openAnime = { nav.navigate("anime/$it") },
                        openSearch = { nav.navigate("search") },
                    )
                }
                composable("search") {
                    SearchScreen(openAnime = { nav.navigate("anime/$it") }, onBack = { nav.popBackStack() })
                }
                composable(
                    "anime/{animeId}",
                    arguments = listOf(navArgument("animeId") { type = NavType.StringType }),
                ) {
                    DetailsScreen(
                        playEpisode = { nav.navigate("player/$it") },
                        onBack = { nav.popBackStack() },
                    )
                }
                composable(
                    "player/{episodeId}",
                    arguments = listOf(navArgument("episodeId") { type = NavType.StringType }),
                ) {
                    PlayerScreen(
                        onBack = { nav.popBackStack() },
                        onEnterPictureInPicture = onEnterPictureInPicture,
                        playNext = { nav.navigate("player/$it") { popUpTo("home") } },
                    )
                }
            }
        }
    }
}
