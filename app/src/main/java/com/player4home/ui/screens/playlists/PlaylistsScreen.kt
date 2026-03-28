package com.player4home.ui.screens.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.player4home.R
import com.player4home.ui.components.DeleteConfirmDialog
import com.player4home.ui.components.PlaylistCard
import com.player4home.ui.navigation.Screen

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
                title = { Text(stringResource(R.string.screen_playlists)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
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
                        color = MaterialTheme.colorScheme.primary
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.playlists,
                            key = { it.id }
                        ) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = {
                                    navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                                },
                                onEdit = {
                                    navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                                },
                                onDelete = {
                                    viewModel.requestDelete(playlist)
                                }
                            )
                        }
                    }
                }
            }

            // Delete confirmation dialog
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
        Text(
            text = stringResource(R.string.home_no_playlists),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onAddPlaylist) {
            Text(text = stringResource(R.string.home_add_playlist))
        }
    }
}
