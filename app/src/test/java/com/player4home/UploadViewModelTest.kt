package com.player4home

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.player4home.data.repository.PlaylistRepository
import com.player4home.ui.screens.upload.UploadMethod
import com.player4home.ui.screens.upload.UploadStatus
import com.player4home.ui.screens.upload.UploadViewModel
import com.player4home.util.XtreamApi
import io.mockk.mockk
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
class UploadViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var repository: PlaylistRepository
    private lateinit var xtreamApi: XtreamApi
    private lateinit var viewModel: UploadViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        xtreamApi = mockk(relaxed = true)
        viewModel = UploadViewModel(context, repository, xtreamApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has URL method selected`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(UploadMethod.URL, state.method)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMethod changes upload method`() = runTest {
        viewModel.setMethod(UploadMethod.FILE)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(UploadMethod.FILE, state.method)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setName trims to 30 characters`() = runTest {
        viewModel.setName("A".repeat(50))
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(30, state.playlistName.length)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save with empty name sets nameError`() = runTest {
        viewModel.setUrl("http://example.com/list.m3u")
        viewModel.save()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.nameError.isNotEmpty())
            assertEquals(UploadStatus.IDLE, state.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save with empty url sets urlError`() = runTest {
        viewModel.setName("My Playlist")
        viewModel.setMethod(UploadMethod.URL)
        viewModel.save()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.urlError.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save with invalid url sets urlError`() = runTest {
        viewModel.setName("My Playlist")
        viewModel.setUrl("not-a-valid-url")
        viewModel.save()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.urlError.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save with no file selected sets fileError`() = runTest {
        viewModel.setName("My Playlist")
        viewModel.setMethod(UploadMethod.FILE)
        viewModel.save()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.fileError.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save with empty xtream host sets xtreamError`() = runTest {
        viewModel.setName("My Playlist")
        viewModel.setMethod(UploadMethod.XTREAM)
        viewModel.save()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.xtreamError.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPinProtected updates state`() = runTest {
        viewModel.setPinProtected(true)
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.pinProtected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFile stores uri and filename`() = runTest {
        val uri = mockk<Uri>()
        viewModel.setFile(uri, "playlist.m3u")
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(uri, state.selectedFileUri)
            assertEquals("playlist.m3u", state.selectedFileName)
            assertTrue(state.fileError.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetStatus clears error state`() = runTest {
        viewModel.resetStatus()
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(UploadStatus.IDLE, state.status)
            assertTrue(state.errorMessage.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
