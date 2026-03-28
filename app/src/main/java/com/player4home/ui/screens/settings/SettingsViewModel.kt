package com.player4home.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val backgroundPlayEnabled: Boolean = true,
    val showSetPinDialog: Boolean = false,
    val pinDialogError: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setBackgroundPlay(enabled: Boolean) = _uiState.update { it.copy(backgroundPlayEnabled = enabled) }
    fun openSetPinDialog() = _uiState.update { it.copy(showSetPinDialog = true, pinDialogError = "") }
    fun closeSetPinDialog() = _uiState.update { it.copy(showSetPinDialog = false) }
    fun setPinDialogError(error: String) = _uiState.update { it.copy(pinDialogError = error) }
}
