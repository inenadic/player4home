package com.player4home.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.player4home.R
import com.player4home.ui.navigation.Screen
import com.player4home.ui.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Update dialog
    uiState.updateAvailable?.let { update ->
        UpdateDialog(
            version = update.latestVersion,
            downloadProgress = uiState.updateProgress,
            errorMessage = uiState.updateError,
            onUpdate = { viewModel.downloadAndInstall() },
            onDismiss = { viewModel.dismissUpdate() },
            onErrorDismiss = { viewModel.clearUpdateError() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TealPrimary
                )
            }

            uiState.recentPlaylists.isEmpty() -> {
                HomeEmptyState(
                    modifier = Modifier.align(Alignment.Center),
                    onAddPlaylist = { navController.navigate(Screen.Upload.route) }
                )
            }

            else -> {
                val playlistId = uiState.recentPlaylists.first().id
                HomeDashboard(
                    playlistId = playlistId,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun HomeDashboard(
    playlistId: Long,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnNavy
                )
            }
            TextButton(onClick = { navController.navigate(Screen.Upload.route) }) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
                Spacer(Modifier.width(4.dp))
                Text("Add Playlist", color = TealPrimary, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Main category tiles — top row (3 large)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.6f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Live TV",
                icon = Icons.Filled.Tv,
                gradient = Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF00838F))),
                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, "LIVE")) }
            )
            CategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Movies",
                icon = Icons.Filled.Movie,
                gradient = Brush.linearGradient(listOf(Color(0xFFFF6B35), Color(0xFFE53935))),
                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, "VOD")) }
            )
            CategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Series",
                icon = Icons.Filled.VideoLibrary,
                gradient = Brush.linearGradient(listOf(Color(0xFF7B61FF), Color(0xFF512DA8))),
                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, "SERIES")) }
            )
        }

        // Bottom row (2 smaller)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Settings",
                icon = Icons.Filled.Settings,
                gradient = Brush.linearGradient(listOf(Color(0xFF43A047), Color(0xFF1B5E20))),
                iconSize = 32.dp,
                onClick = { navController.navigate(Screen.Settings.route) }
            )
            CategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Playlists",
                icon = Icons.Filled.List,
                gradient = Brush.linearGradient(listOf(Color(0xFF00897B), Color(0xFF004D40))),
                iconSize = 32.dp,
                onClick = { navController.navigate(Screen.Playlists.route) }
            )
        }
    }
}

@Composable
private fun CategoryTile(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    gradient: Brush,
    iconSize: Dp = 48.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HomeEmptyState(
    modifier: Modifier = Modifier,
    onAddPlaylist: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TealContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
        Text(
            text = "No playlists yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = OnNavy
        )
        Text(
            text = stringResource(R.string.home_no_playlists),
            style = MaterialTheme.typography.bodyMedium,
            color = OnNavyVariant
        )
        Button(
            onClick = onAddPlaylist,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = OnTeal)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.home_add_playlist))
        }
    }
}

// ── Update dialog ──────────────────────────────────────────────

@Composable
private fun UpdateDialog(
    version: String,
    downloadProgress: Int?,
    errorMessage: String?,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onErrorDismiss,
            containerColor = NavyCard,
            title = { Text("Update failed", color = OnNavy) },
            text = { Text(errorMessage, color = OnNavyVariant) },
            confirmButton = {
                TextButton(onClick = onErrorDismiss) {
                    Text("OK", color = TealPrimary)
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = { if (downloadProgress == null) onDismiss() },
        containerColor = NavyCard,
        icon = {
            Icon(
                Icons.Filled.SystemUpdateAlt,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "New version available",
                color = OnNavy,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Version $version is ready to install.", color = OnNavyVariant)
                if (downloadProgress != null) {
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = TealPrimary,
                        trackColor = NavyCardElevated
                    )
                    Text(
                        "Downloading… $downloadProgress%",
                        color = OnNavyVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            if (downloadProgress == null) {
                Button(
                    onClick = onUpdate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = OnTeal
                    )
                ) {
                    Text("Update Now", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            if (downloadProgress == null) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = OnNavyVariant)
                }
            }
        }
    )
}
