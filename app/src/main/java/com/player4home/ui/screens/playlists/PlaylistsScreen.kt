package com.player4home.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.player4home.R
import com.player4home.ui.components.DeleteConfirmDialog
import com.player4home.ui.components.PlaylistCard
import com.player4home.ui.navigation.Screen
import com.player4home.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    navController: NavController,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.screen_playlists),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnNavy
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Upload.route) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add playlist", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
            )
        },
        containerColor = NavyBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TealPrimary
                    )
                }

                uiState.playlists.isEmpty() -> {
                    PlaylistsEmptyState(
                        modifier = Modifier.align(Alignment.Center),
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
                            Text(
                                text = "${uiState.playlists.size} playlist${if (uiState.playlists.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnNavyVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(
                            items = uiState.playlists,
                            key = { it.id }
                        ) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) },
                                onEdit = { navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) },
                                onDelete = { viewModel.requestDelete(playlist) }
                            )
                        }
                    }
                }
            }

            uiState.deleteTarget?.let { target ->
                DeleteConfirmDialog(
                    playlistName = target.name,
                    onConfirm = { viewModel.confirmDelete(target) },
                    onDismiss = { viewModel.cancelDelete() }
                )
            }
        }
    }
}

@Composable
private fun PlaylistsEmptyState(
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
                .background(NavyCardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.List,
                contentDescription = null,
                tint = OnNavyVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = stringResource(R.string.home_no_playlists),
            style = MaterialTheme.typography.bodyLarge,
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
