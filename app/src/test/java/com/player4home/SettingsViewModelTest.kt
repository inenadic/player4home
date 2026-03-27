package com.player4home

import app.cash.turbine.test
import com.player4home.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has background play enabled`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.backgroundPlayEnabled)
            assertFalse(state.showSetPinDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setBackgroundPlay updates state`() = runTest {
        viewModel.setBackgroundPlay(false)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.backgroundPlayEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openSetPinDialog shows dialog`() = runTest {
        viewModel.openSetPinDialog()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.showSetPinDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `closeSetPinDialog hides dialog`() = runTest {
        viewModel.openSetPinDialog()
        viewModel.closeSetPinDialog()
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.showSetPinDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPinDialogError updates error message`() = runTest {
        viewModel.setPinDialogError("PINs do not match")
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("PINs do not match", state.pinDialogError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openSetPinDialog clears previous error`() = runTest {
        viewModel.setPinDialogError("Some error")
        viewModel.openSetPinDialog()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.pinDialogError.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
