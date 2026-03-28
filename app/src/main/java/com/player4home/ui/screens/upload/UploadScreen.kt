package com.player4home.ui.screens.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.player4home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // SAF file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = resolveFileName(context, uri)
            viewModel.setFile(uri, fileName)
        }
    }

    // Handle status side-effects
    LaunchedEffect(uiState.status) {
        when (uiState.status) {
            UploadStatus.SUCCESS -> {
                snackbarHostState.showSnackbar(context.getString(R.string.upload_success))
                viewModel.resetStatus()
                navController.popBackStack()
            }
            UploadStatus.ERROR -> {
                snackbarHostState.showSnackbar(uiState.errorMessage.ifBlank { context.getString(R.string.error_generic) })
                viewModel.resetStatus()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.upload_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            val tabTitles = listOf("URL", "File", "Xtream")
            val selectedTabIndex = uiState.method.ordinal
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.setMethod(UploadMethod.entries[index]) },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.playlistName,
                    onValueChange = { viewModel.setName(it) },
                    label = { Text(stringResource(R.string.upload_playlist_name)) },
                    placeholder = { Text(stringResource(R.string.upload_playlist_name_hint)) },
                    singleLine = true,
                    isError = uiState.nameError.isNotEmpty(),
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.nameError.ifEmpty { "" },
                                color = if (uiState.nameError.isNotEmpty())
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${uiState.playlistName.length}/30",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                when (uiState.method) {
                    UploadMethod.URL -> UrlSection(
                        url = uiState.url,
                        urlError = uiState.urlError,
                        onUrlChange = { viewModel.setUrl(it) }
                    )

                    UploadMethod.FILE -> FileSection(
                        selectedFileName = uiState.selectedFileName,
                        fileError = uiState.fileError,
                        onBrowse = { filePickerLauncher.launch("*/*") }
                    )

                    UploadMethod.XTREAM -> XtreamSection(
                        host = uiState.xtreamHost,
                        username = uiState.xtreamUsername,
                        password = uiState.xtreamPassword,
                        xtreamError = uiState.xtreamError,
                        onHostChange = { viewModel.setXtreamHost(it) },
                        onUsernameChange = { viewModel.setXtreamUsername(it) },
                        onPasswordChange = { viewModel.setXtreamPassword(it) }
                    )
                }

                PinSection(
                    pinProtected = uiState.pinProtected,
                    pin = uiState.pin,
                    onPinProtectedChange = { viewModel.setPinProtected(it) },
                    onPinChange = { viewModel.setPin(it) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                val isLoading = uiState.status == UploadStatus.LOADING

                Button(
                    onClick = { viewModel.save() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(20.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(if (isLoading) "Saving…" else "Save Playlist")
                }
            }
        }
    }
}

@Composable
private fun UrlSection(
    url: String,
    urlError: String,
    onUrlChange: (String) -> Unit
) {
    OutlinedTextField(
        value = url,
        onValueChange = onUrlChange,
        label = { Text(stringResource(R.string.upload_url_label)) },
        placeholder = { Text(stringResource(R.string.upload_url_hint)) },
        singleLine = true,
        isError = urlError.isNotEmpty(),
        supportingText = if (urlError.isNotEmpty()) {
            { Text(text = urlError, color = MaterialTheme.colorScheme.error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FileSection(
    selectedFileName: String,
    fileError: String,
    onBrowse: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onBrowse,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Browse Files")
        }

        when {
            selectedFileName.isNotEmpty() -> {
                Text(
                    text = "Selected: $selectedFileName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            fileError.isNotEmpty() -> {
                Text(
                    text = fileError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Text(
            text = "Supported formats: M3U, M3U8",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun XtreamSection(
    host: String,
    username: String,
    password: String,
    xtreamError: String,
    onHostChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = host,
            onValueChange = onHostChange,
            label = { Text("Host URL") },
            placeholder = { Text("http://provider.com:8080") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (xtreamError.isNotEmpty()) {
            Text(
                text = xtreamError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PinSection(
    pinProtected: Boolean,
    pin: String,
    onPinProtectedChange: (Boolean) -> Unit,
    onPinChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Protect with PIN",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = pinProtected,
                onCheckedChange = onPinProtectedChange
            )
        }

        if (pinProtected) {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) onPinChange(it) },
                label = { Text("4-Digit PIN") },
                placeholder = { Text("0000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Resolves a human-readable file name from a SAF URI using the ContentResolver.
 */
private fun resolveFileName(context: android.content.Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            if (nameIndex >= 0) cursor.getString(nameIndex) else uri.lastPathSegment ?: "Unknown"
        } ?: uri.lastPathSegment ?: "Unknown"
    } catch (e: Exception) {
        uri.lastPathSegment ?: "Unknown"
    }
}
