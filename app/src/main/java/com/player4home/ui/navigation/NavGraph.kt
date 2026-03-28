package com.player4home.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.player4home.ui.screens.home.HomeScreen
import com.player4home.ui.screens.player.PlayerScreen
import com.player4home.ui.screens.playlists.PlaylistDetailScreen
import com.player4home.ui.screens.playlists.PlaylistsScreen
import com.player4home.ui.screens.settings.SettingsScreen
import com.player4home.ui.screens.upload.UploadScreen

sealed class Screen(val route: String) {
    data object Home       : Screen("home")
    data object Playlists  : Screen("playlists")
    data object Upload     : Screen("upload")
    data object Settings   : Screen("settings")
    data object Player     : Screen("player/{playlistId}/{channelId}") {
        fun createRoute(playlistId: Long, channelId: Long) = "player/$playlistId/$channelId"
    }
    data object PlaylistDetail : Screen("playlist/{playlistId}?tab={tab}") {
        fun createRoute(playlistId: Long, tab: String = "ALL") = "playlist/$playlistId?tab=$tab"
    }
}

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(navController = navController)
        }
        composable(Screen.Upload.route) {
            UploadScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType },
                navArgument("tab") { type = NavType.StringType; defaultValue = "ALL" }
            )
        ) {
            PlaylistDetailScreen(navController = navController)
        }
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType },
                navArgument("channelId")  { type = NavType.LongType }
            )
        ) { backStack ->
            val playlistId = backStack.arguments?.getLong("playlistId") ?: 0L
            val channelId  = backStack.arguments?.getLong("channelId")  ?: 0L
            PlayerScreen(
                playlistId = playlistId,
                channelId  = channelId,
                onBack     = { navController.popBackStack() }
            )
        }
    }
}
