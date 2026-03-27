package com.player4home.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player4home.data.model.Channel
import com.player4home.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val channel: Channel? = null,
    val channels: List<Channel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun load(playlistId: Long, channelId: Long) {
        viewModelScope.launch {
            repository.getChannelsForPlaylist(playlistId).collect { channels ->
                val current = channels.find { it.id == channelId } ?: channels.firstOrNull()
                _uiState.update { it.copy(channel = current, channels = channels, isLoading = false) }
            }
        }
    }

    fun selectChannel(channel: Channel) {
        _uiState.update { it.copy(channel = channel) }
    }

    fun setError(msg: String?) {
        _uiState.update { it.copy(error = msg) }
    }
}
