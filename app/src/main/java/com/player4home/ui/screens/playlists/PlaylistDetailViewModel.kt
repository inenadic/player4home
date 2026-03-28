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
    val groups: List<Pair<String, Int>> = emptyList(), // (name, channel count)
    val selectedGroup: String? = null,                 // null = "ALL"
    val channels: List<Channel> = emptyList(),         // filtered channels for right panel
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

    private val _allChannels  = MutableStateFlow<List<Channel>>(emptyList())
    private val _selectedTab  = MutableStateFlow(initialTab)
    private val _searchQuery  = MutableStateFlow("")
    private val _selectedGroup = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private data class ComputedState(
        val groups: List<Pair<String, Int>>,
        val channels: List<Channel>,
        val tab: ChannelTab,
        val query: String,
        val selectedGroup: String?
    )

    init {
        viewModelScope.launch {
            val playlist = repository.getPlaylistById(playlistId)
            _uiState.update { it.copy(playlistName = playlist?.name.orEmpty()) }
        }

        repository.getChannelsForPlaylist(playlistId)
            .onEach { channels ->
                _allChannels.value = channels
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)

        combine(_allChannels, _selectedTab, _searchQuery, _selectedGroup) { channels, tab, query, group ->
            val tabFiltered = when (tab) {
                ChannelTab.ALL    -> channels
                ChannelTab.LIVE   -> channels.filter { it.streamType == StreamType.LIVE }
                ChannelTab.VOD    -> channels.filter { it.streamType == StreamType.VOD }
                ChannelTab.SERIES -> channels.filter { it.streamType == StreamType.SERIES }
            }
            val groupedMap = tabFiltered.groupBy { it.groupTitle.ifEmpty { "—" } }
            val groups = listOf("ALL" to tabFiltered.size) +
                         groupedMap.entries.map { it.key to it.value.size }

            val groupFiltered = if (group == null) tabFiltered
                                else groupedMap[group] ?: tabFiltered

            val displayed = if (query.isBlank()) groupFiltered
                            else groupFiltered.filter { it.name.contains(query, ignoreCase = true) }

            ComputedState(groups, displayed, tab, query, group)
        }
            .onEach { (groups, channels, tab, query, group) ->
                _uiState.update {
                    it.copy(
                        groups = groups,
                        channels = channels,
                        selectedTab = tab,
                        searchQuery = query,
                        selectedGroup = group
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tab: ChannelTab) {
        _selectedTab.value = tab
        _selectedGroup.value = null
    }

    fun onGroupSelected(group: String?) {
        _selectedGroup.value = group
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
    }
}
