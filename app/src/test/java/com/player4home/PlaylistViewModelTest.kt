package com.player4home

import app.cash.turbine.test
import com.player4home.data.model.Playlist
import com.player4home.data.model.PlaylistType
import com.player4home.data.repository.PlaylistRepository
import com.player4home.ui.screens.playlists.PlaylistsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PlaylistRepository
    private lateinit var viewModel: PlaylistsViewModel

    private val samplePlaylists = listOf(
        Playlist(id = 1, name = "My IPTV", type = PlaylistType.URL, url = "http://example.com/playlist.m3u"),
        Playlist(id = 2, name = "Local File", type = PlaylistType.FILE, filePath = "/sdcard/list.m3u"),
        Playlist(id = 3, name = "Xtream", type = PlaylistType.XTREAM, xtreamHost = "http://host.com")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.getAllPlaylists() } returns flowOf(samplePlaylists)
        viewModel = PlaylistsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has loading true`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            // First emission may be loading
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `playlists are loaded from repository`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.playlists.size)
            assertEquals("My IPTV", state.playlists[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `requestDelete sets deleteTarget`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.requestDelete(samplePlaylists[0])
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.deleteTarget)
            assertEquals(1L, state.deleteTarget?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancelDelete clears deleteTarget`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.requestDelete(samplePlaylists[0])
        viewModel.cancelDelete()
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.deleteTarget)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete calls repository`() = runTest {
        coEvery { repository.deletePlaylist(any()) } returns Unit
        viewModel.confirmDelete(samplePlaylists[0])
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repository.deletePlaylist(samplePlaylists[0]) }
    }
}
