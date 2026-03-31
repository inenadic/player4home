package com.player4home.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.player4home.R
import com.player4home.ui.navigation.Screen
import com.player4home.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

// ── Live clock helper ──────────────────────────────────────────

@Composable
private fun rememberLiveClock(): String {
    val formatter = remember {
        DateTimeFormatter.ofPattern("EEEE | HH:mm", Locale.ENGLISH)
    }
    var timeText by remember {
        mutableStateOf(LocalDateTime.now().format(formatter))
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            timeText = LocalDateTime.now().format(formatter)
        }
    }
    return timeText
}

// ── Dashboard ─────────────────────────────────────────────────

@Composable
private fun HomeDashboard(
    playlistId: Long,
    navController: NavController
) {
    val timeText = rememberLiveClock()

    val orbTransition = rememberInfiniteTransition(label = "orb_transition")
    val orbPhase by orbTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height

                // Teal orb — drifts from top-left toward center-left
                val tealCenterX = w * 0.05f + w * 0.25f * orbPhase
                val tealCenterY = h * 0.05f + h * 0.15f * orbPhase
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00BFA5).copy(alpha = 0.30f),
                            Color(0xFF00BFA5).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(tealCenterX, tealCenterY),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.55f,
                    center = Offset(tealCenterX, tealCenterY)
                )

                // Purple orb — drifts from bottom-right toward center-right
                val purpleCenterX = w * 0.95f - w * 0.20f * orbPhase
                val purpleCenterY = h * 0.95f - h * 0.15f * orbPhase
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7B61FF).copy(alpha = 0.28f),
                            Color(0xFF7B61FF).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(purpleCenterX, purpleCenterY),
                        radius = w * 0.50f
                    ),
                    radius = w * 0.50f,
                    center = Offset(purpleCenterX, purpleCenterY)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: logo + app name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnNavy
                    )
                }

                // Center: live date + time
                Text(
                    text = timeText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Right: add playlist + settings icon buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { navController.navigate(Screen.Upload.route) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add playlist",
                            tint = TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = OnNavyVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Main grid ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Left: Live TV — tall tile spanning full height
                TopCategoryTile(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    label = "Live TV",
                    icon = Icons.Filled.Tv,
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFF00BFA5), Color(0xFF006064))
                    ),
                    onClick = {
                        navController.navigate(
                            Screen.PlaylistDetail.createRoute(playlistId, "LIVE")
                        )
                    }
                )

                Spacer(Modifier.width(12.dp))

                // Right: 2-row column
                Column(modifier = Modifier.weight(2f)) {
                    // Top row: Movies + Series (equal height)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        TopCategoryTile(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            label = "Movies",
                            icon = Icons.Filled.Movie,
                            gradient = Brush.linearGradient(
                                listOf(Color(0xFFFF7043), Color(0xFFE53935))
                            ),
                            onClick = {
                                navController.navigate(
                                    Screen.PlaylistDetail.createRoute(playlistId, "VOD")
                                )
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        TopCategoryTile(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            label = "Series",
                            icon = Icons.Filled.VideoLibrary,
                            gradient = Brush.linearGradient(
                                listOf(Color(0xFF7B61FF), Color(0xFF4527A0))
                            ),
                            onClick = {
                                navController.navigate(
                                    Screen.PlaylistDetail.createRoute(playlistId, "SERIES")
                                )
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Bottom row: Settings + Playlists flat buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                    ) {
                        BottomCategoryTile(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            label = "Settings",
                            icon = Icons.Filled.Settings,
                            onClick = { navController.navigate(Screen.Settings.route) }
                        )
                        Spacer(Modifier.width(12.dp))
                        BottomCategoryTile(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            label = "Playlists",
                            icon = Icons.AutoMirrored.Filled.List,
                            onClick = { navController.navigate(Screen.Playlists.route) }
                        )
                    }
                }
            }
        }
    }
}

// ── Top (colorful) tile with shimmer + label overlay ──────────

@Composable
private fun TopCategoryTile(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(),
        label = "scale_$label"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Shimmer highlight at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = shimmerAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Icon — centered, fillMaxSize fraction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.38f)
            )
        }

        // Dark gradient overlay + label at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
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
    }
}

// ── Bottom flat button tile ────────────────────────────────────

@Composable
private fun BottomCategoryTile(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "scale_$label"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E5C47), RoundedCornerShape(16.dp))
            .background(Color(0xFF0D2B22))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                color = Color(0xFFB0BEC5),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
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
