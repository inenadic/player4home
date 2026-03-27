package com.player4home.ui.screens.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player4home.data.model.Playlist
import com.player4home.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = true,
    val deleteTarget: Playlist? = null
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    init {
        repository.getAllPlaylists()
            .onEach { playlists ->
                _uiState.update { it.copy(playlists = playlists, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun requestDelete(playlist: Playlist) {
        _uiState.update { it.copy(deleteTarget = playlist) }
    }

    fun confirmDelete(playlist: Playlist) {
        _uiState.update { it.copy(deleteTarget = null) }
        viewModelScope.launch { repository.deletePlaylist(playlist) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deleteTarget = null) }
    }
}
