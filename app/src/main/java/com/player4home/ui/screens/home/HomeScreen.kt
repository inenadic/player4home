package com.player4home.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.player4home.R
import com.player4home.ui.components.PlaylistCard
import com.player4home.ui.navigation.Screen
import com.player4home.ui.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Gradient header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(NavyCardElevated, NavyBackground)
                    )
                )
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = OnNavy
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your IPTV playlists",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnNavyVariant
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }

            uiState.recentPlaylists.isEmpty() -> {
                HomeEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    onAddPlaylist = { navController.navigate(Screen.Upload.route) }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.home_recent_playlists),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = OnNavyVariant
                            )
                            TextButton(
                                onClick = { navController.navigate(Screen.Playlists.route) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "See all",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TealPrimary
                                )
                            }
                        }
                    }
                    items(
                        items = uiState.recentPlaylists,
                        key = { it.id }
                    ) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) },
                            onEdit = { navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) },
                            onDelete = {}
                        )
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.Upload.route) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.home_add_playlist))
                        }
                    }
                }
            }
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
