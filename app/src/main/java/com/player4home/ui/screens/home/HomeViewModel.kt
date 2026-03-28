package com.player4home.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player4home.data.model.Playlist
import com.player4home.data.repository.PlaylistRepository
import com.player4home.util.AppUpdater
import com.player4home.util.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentPlaylists: List<Playlist> = emptyList(),
    val isLoading: Boolean = true,
    val updateAvailable: UpdateInfo? = null,
    val updateProgress: Int? = null,   // null = not downloading; 0-100 = downloading
    val updateError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PlaylistRepository,
    private val updater: AppUpdater
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        repository.getAllPlaylists()
            .onEach { playlists ->
                _uiState.update { it.copy(recentPlaylists = playlists.take(5), isLoading = false) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val update = updater.checkForUpdate()
            if (update != null) _uiState.update { it.copy(updateAvailable = update) }
        }
    }

    fun downloadAndInstall() {
        val info = _uiState.value.updateAvailable ?: return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(updateProgress = 0) }
                val apk = updater.downloadApk(context, info.downloadUrl) { pct ->
                    _uiState.update { it.copy(updateProgress = pct) }
                }
                updater.installApk(context, apk)
                _uiState.update { it.copy(updateProgress = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(updateProgress = null, updateError = e.message) }
            }
        }
    }

    fun dismissUpdate() = _uiState.update { it.copy(updateAvailable = null) }
    fun clearUpdateError() = _uiState.update { it.copy(updateError = null) }
}
