package com.player4home.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.List
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
import com.player4home.data.model.Playlist
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
                    recentPlaylists = uiState.recentPlaylists,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun HomeDashboard(
    playlistId: Long,
    recentPlaylists: List<Playlist>,
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
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnNavy
                    )
                    Text(
                        text = "Your personal media hub",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnNavyVariant,
                        letterSpacing = 0.3.sp
                    )
                }
            }
            TextButton(onClick = { navController.navigate(Screen.Upload.route) }) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
                Spacer(Modifier.width(4.dp))
                Text("Add Playlist", color = TealPrimary, style = MaterialTheme.typography.labelLarge)
            }
        }

        // Recently Used section — shown only when playlists exist
        if (recentPlaylists.isNotEmpty()) {
            RecentlyUsedSection(playlists = recentPlaylists)
        }

        // Main category tiles — top row (3 large, colorful with glow)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.6f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopCategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Live TV",
                icon = Icons.Filled.Tv,
                gradient = Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF00838F))),
                glowColor = Color(0xFF00D4AA),
                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, "LIVE")) }
            )
            TopCategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Movies",
                icon = Icons.Filled.Movie,
                gradient = Brush.linearGradient(listOf(Color(0xFFFF6B35), Color(0xFFE53935))),
                glowColor = Color(0xFFFF6B35),
                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, "VOD")) }
            )
            TopCategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Series",
                icon = Icons.Filled.VideoLibrary,
                gradient = Brush.linearGradient(listOf(Color(0xFF7B61FF), Color(0xFF512DA8))),
                glowColor = Color(0xFF7B61FF),
                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlistId, "SERIES")) }
            )
        }

        // Bottom row (2 smaller, less prominent)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BottomCategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Settings",
                icon = Icons.Filled.Settings,
                gradient = Brush.linearGradient(listOf(Color(0xFF2A3A2A), Color(0xFF1B2A1B))),
                iconSize = 32.dp,
                onClick = { navController.navigate(Screen.Settings.route) }
            )
            BottomCategoryTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Playlists",
                icon = Icons.AutoMirrored.Filled.List,
                gradient = Brush.linearGradient(listOf(Color(0xFF1A2D2A), Color(0xFF0D1E1A))),
                iconSize = 32.dp,
                onClick = { navController.navigate(Screen.Playlists.route) }
            )
        }
    }
}

// ── Recently Used section ──────────────────────────────────────

@Composable
private fun RecentlyUsedSection(playlists: List<Playlist>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Recently Used",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = OnNavyVariant,
            letterSpacing = 0.8.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            playlists.take(3).forEach { playlist ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyCard)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = OnNavyVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ── Top (colorful) tile with shimmer glow + dark label overlay ─

@Composable
private fun TopCategoryTile(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    gradient: Brush,
    glowColor: Color,
    onClick: () -> Unit
) {
    // Shimmer animation — oscillates opacity on the top highlight stripe
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_$label")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha_$label"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .background(gradient)
            .clickable(onClick = onClick)
    ) {
        // Shimmer highlight strip at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            glowColor.copy(alpha = shimmerAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Icon — centred, weight-proportional via fillMaxSize fraction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.42f)
            )
        }

        // Dark overlay + label at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC000000))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // Subtle inner bottom shadow (purely visual depth layer)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x33000000))
                    )
                )
        )
    }
}

// ── Bottom (muted) tile ────────────────────────────────────────

@Composable
private fun BottomCategoryTile(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    gradient: Brush,
    iconSize: Dp = 32.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
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
                tint = OnNavyVariant,
                modifier = Modifier.size(iconSize)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                color = OnNavyVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // Subtle inner bottom shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x22000000))
                    )
                )
        )
    }
}

// ── Empty state ────────────────────────────────────────────────

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
