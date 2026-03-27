package com.player4home.ui.screens.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player4home.data.model.Playlist
import com.player4home.data.model.PlaylistType
import com.player4home.data.repository.PlaylistRepository
import com.player4home.R
import com.player4home.util.M3uParser
import com.player4home.util.XtreamApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

enum class UploadMethod { URL, FILE, XTREAM }
enum class UploadStatus { IDLE, LOADING, SUCCESS, ERROR }

data class UploadUiState(
    val method: UploadMethod = UploadMethod.URL,
    val playlistName: String = "",
    val url: String = "",
    val selectedFileName: String = "",
    val selectedFileUri: Uri? = null,
    val xtreamHost: String = "",
    val xtreamUsername: String = "",
    val xtreamPassword: String = "",
    val pinProtected: Boolean = false,
    val pin: String = "",
    val status: UploadStatus = UploadStatus.IDLE,
    val errorMessage: String = "",
    val nameError: String = "",
    val urlError: String = "",
    val fileError: String = "",
    val xtreamError: String = ""
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PlaylistRepository,
    private val xtreamApi: XtreamApi,
    private val httpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun setMethod(method: UploadMethod) = _uiState.update { it.copy(method = method) }
    fun setName(name: String) = _uiState.update { it.copy(playlistName = name.take(30), nameError = "") }
    fun setUrl(url: String) = _uiState.update { it.copy(url = url, urlError = "") }
    fun setFile(uri: Uri, name: String) = _uiState.update { it.copy(selectedFileUri = uri, selectedFileName = name, fileError = "") }
    fun setXtreamHost(v: String) = _uiState.update { it.copy(xtreamHost = v, xtreamError = "") }
    fun setXtreamUsername(v: String) = _uiState.update { it.copy(xtreamUsername = v) }
    fun setXtreamPassword(v: String) = _uiState.update { it.copy(xtreamPassword = v) }
    fun setPinProtected(v: Boolean) = _uiState.update { it.copy(pinProtected = v) }
    fun setPin(v: String) = _uiState.update { it.copy(pin = v) }

    fun save() {
        val state = _uiState.value
        if (!validate(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(status = UploadStatus.LOADING) }
            try {
                when (state.method) {
                    UploadMethod.URL -> saveUrlPlaylist(state)
                    UploadMethod.FILE -> saveFilePlaylist(state)
                    UploadMethod.XTREAM -> saveXtreamPlaylist(state)
                }
                _uiState.update { it.copy(status = UploadStatus.SUCCESS) }
            } catch (e: Exception) {
                _uiState.update { it.copy(status = UploadStatus.ERROR, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    private suspend fun saveUrlPlaylist(state: UploadUiState) {
        val content = withContext(Dispatchers.IO) {
            httpClient.newCall(Request.Builder().url(state.url).build())
                .execute().body?.string() ?: throw Exception("Empty response")
        }
        val channels = M3uParser.parse(content)
        repository.addPlaylist(
            Playlist(name = state.playlistName, type = PlaylistType.URL, url = state.url,
                isPinProtected = state.pinProtected, pin = state.pin),
            channels
        )
    }

    private suspend fun saveFilePlaylist(state: UploadUiState) {
        val uri = state.selectedFileUri ?: throw Exception("No file selected")
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            ?: throw Exception("Cannot read file")
        val channels = M3uParser.parse(content)
        repository.addPlaylist(
            Playlist(name = state.playlistName, type = PlaylistType.FILE, filePath = uri.toString(),
                isPinProtected = state.pinProtected, pin = state.pin),
            channels
        )
    }

    private suspend fun saveXtreamPlaylist(state: UploadUiState) {
        val all = coroutineScope {
            val live = async { xtreamApi.fetchLiveChannels(state.xtreamHost, state.xtreamUsername, state.xtreamPassword, 0) }
            val vod = async { xtreamApi.fetchVodStreams(state.xtreamHost, state.xtreamUsername, state.xtreamPassword, 0) }
            live.await() + vod.await()
        }
        repository.addPlaylist(
            Playlist(name = state.playlistName, type = PlaylistType.XTREAM,
                xtreamHost = state.xtreamHost, xtreamUsername = state.xtreamUsername,
                xtreamPassword = state.xtreamPassword,
                isPinProtected = state.pinProtected, pin = state.pin),
            all
        )
    }

    private fun validate(state: UploadUiState): Boolean {
        var valid = true
        if (state.playlistName.isBlank()) {
            _uiState.update { it.copy(nameError = context.getString(R.string.upload_error_name_empty)) }
            valid = false
        }
        when (state.method) {
            UploadMethod.URL -> if (state.url.isBlank() || !android.util.Patterns.WEB_URL.matcher(state.url).matches()) {
                _uiState.update { it.copy(urlError = context.getString(R.string.upload_error_url_invalid)) }
                valid = false
            }
            UploadMethod.FILE -> if (state.selectedFileUri == null) {
                _uiState.update { it.copy(fileError = context.getString(R.string.upload_error_file_empty)) }
                valid = false
            }
            UploadMethod.XTREAM -> if (state.xtreamHost.isBlank() || state.xtreamUsername.isBlank()) {
                _uiState.update { it.copy(xtreamError = context.getString(R.string.upload_error_xtream_credentials)) }
                valid = false
            }
        }
        return valid
    }

    fun resetStatus() = _uiState.update { it.copy(status = UploadStatus.IDLE, errorMessage = "") }
}
