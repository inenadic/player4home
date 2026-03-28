package com.player4home.ui.screens.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player4home.data.model.Channel
import com.player4home.data.model.StreamType
import com.player4home.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChannelTab { ALL, LIVE, VOD, SERIES }

data class PlaylistDetailUiState(
    val playlistName: String = "",
    val channels: List<Channel> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: ChannelTab = ChannelTab.ALL,
    val searchQuery: String = ""
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PlaylistRepository
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])
    private val initialTab: ChannelTab = when (savedStateHandle.get<String>("tab")) {
        "LIVE"   -> ChannelTab.LIVE
        "VOD"    -> ChannelTab.VOD
        "SERIES" -> ChannelTab.SERIES
        else     -> ChannelTab.ALL
    }

    private val _allChannels = MutableStateFlow<List<Channel>>(emptyList())
    private val _selectedTab = MutableStateFlow(initialTab)
    private val _searchQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        // Load playlist name
        viewModelScope.launch {
            val playlist = repository.getPlaylistById(playlistId)
            _uiState.update { it.copy(playlistName = playlist?.name.orEmpty()) }
        }

        // Collect all channels for this playlist
        repository.getChannelsForPlaylist(playlistId)
            .onEach { channels ->
                _allChannels.value = channels
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)

        // Combine channels, tab, and search query into filtered result
        combine(_allChannels, _selectedTab, _searchQuery) { channels, tab, query ->
            val tabFiltered = when (tab) {
                ChannelTab.ALL -> channels
                ChannelTab.LIVE -> channels.filter { it.streamType == StreamType.LIVE }
                ChannelTab.VOD -> channels.filter { it.streamType == StreamType.VOD }
                ChannelTab.SERIES -> channels.filter { it.streamType == StreamType.SERIES }
            }
            val queryFiltered = if (query.isBlank()) {
                tabFiltered
            } else {
                tabFiltered.filter { it.name.contains(query, ignoreCase = true) }
            }
            Triple(queryFiltered, tab, query)
        }
            .onEach { (filtered, tab, query) ->
                _uiState.update { it.copy(channels = filtered, selectedTab = tab, searchQuery = query) }
            }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tab: ChannelTab) {
        _selectedTab.value = tab
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
    }
}
