package com.player4home.ui.screens.player

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.player4home.data.model.Channel
import com.player4home.data.model.StreamType
import com.player4home.ui.theme.CardBorder
import com.player4home.ui.theme.Divider
import com.player4home.ui.theme.LiveBadge
import com.player4home.ui.theme.NavyBackground
import com.player4home.ui.theme.NavyCard
import com.player4home.ui.theme.NavyCardElevated
import com.player4home.ui.theme.NavySurface
import com.player4home.ui.theme.NavySurfaceVariant
import com.player4home.ui.theme.OnNavy
import com.player4home.ui.theme.OnNavySubtle
import com.player4home.ui.theme.OnNavyVariant
import com.player4home.ui.theme.SeriesBadge
import com.player4home.ui.theme.TealGlow
import com.player4home.ui.theme.TealPrimary
import com.player4home.ui.theme.TealPrimaryDark
import com.player4home.ui.theme.VodBadge

@Composable
fun PlayerScreen(
    playlistId: Long,
    channelId: Long,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(playlistId, channelId) {
        viewModel.load(playlistId, channelId)
    }

    LaunchedEffect(uiState.channel) {
        val streamUrl = uiState.channel?.streamUrl
        if (!streamUrl.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val filteredChannels = remember(uiState.channels, searchQuery) {
        if (searchQuery.isBlank()) uiState.channels
        else uiState.channels.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // ── LEFT: Channel Sidebar (30%) ──────────────────────────────────
        ChannelSidebar(
            modifier = Modifier
                .weight(0.30f)
                .fillMaxHeight(),
            channels = filteredChannels,
            allChannels = uiState.channels,
            selectedChannel = uiState.channel,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onChannelClick = { viewModel.selectChannel(it) },
            onBack = onBack
        )

        // ── RIGHT: Video Player (70%) ────────────────────────────────────
        VideoPlayerPanel(
            modifier = Modifier
                .weight(0.70f)
                .fillMaxHeight(),
            exoPlayer = exoPlayer,
            channel = uiState.channel,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onRetry = { viewModel.load(playlistId, channelId) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Channel Sidebar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChannelSidebar(
    modifier: Modifier = Modifier,
    channels: List<Channel>,
    allChannels: List<Channel>,
    selectedChannel: Channel?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()

    // Scroll to selected channel on first load
    LaunchedEffect(selectedChannel?.id, allChannels.size) {
        if (selectedChannel != null && allChannels.isNotEmpty()) {
            val index = channels.indexOfFirst { it.id == selectedChannel.id }
            if (index >= 0) listState.animateScrollToItem(index)
        }
    }

    Column(
        modifier = modifier
            .background(NavySurface)
    ) {
        // Header: back button + current channel name
        SidebarHeader(
            channelName = selectedChannel?.name ?: "Channels",
            onBack = onBack
        )

        // Thin divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CardBorder)
        )

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search channels…",
                    color = OnNavyVariant,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = OnNavyVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            textStyle = LocalTextStyle.current.copy(
                color = OnNavy,
                fontSize = 13.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary.copy(alpha = 0.6f),
                unfocusedBorderColor = CardBorder,
                cursorColor = TealPrimary,
                focusedContainerColor = NavyCardElevated,
                unfocusedContainerColor = NavyCard,
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )

        // Channel count label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (searchQuery.isBlank()) {
                    "${allChannels.size} channels"
                } else {
                    "${channels.size} of ${allChannels.size}"
                },
                color = OnNavyVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp
            )
        }

        // Thin divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Divider)
        )

        // Channel list
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = channels,
                key = { _, ch -> ch.id }
            ) { index, channel ->
                SidebarChannelItem(
                    index = index,
                    channel = channel,
                    isSelected = channel.id == selectedChannel?.id,
                    onClick = { onChannelClick(channel) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SidebarHeader(
    channelName: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = OnNavy,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = channelName,
            color = OnNavy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar Channel Item
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SidebarChannelItem(
    index: Int,
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) TealGlow else Color.Transparent
    val nameColor = if (isSelected) OnNavy else OnNavyVariant
    val numberColor = if (isSelected) TealPrimary.copy(alpha = 0.8f) else OnNavySubtle.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 0.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selected accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(52.dp)
                .background(
                    color = if (isSelected) TealPrimary else Color.Transparent,
                    shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Channel number
        Text(
            text = "${index + 1}",
            color = numberColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NavyCardElevated),
            contentAlignment = Alignment.Center
        ) {
            if (channel.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    onError = { /* fallback icon shown below via placeholder logic */ }
                )
            } else {
                Icon(
                    imageVector = when (channel.streamType) {
                        StreamType.LIVE -> Icons.Default.LiveTv
                        StreamType.VOD -> Icons.Default.Movie
                        StreamType.SERIES -> Icons.Default.VideoLibrary
                    },
                    contentDescription = null,
                    tint = OnNavySubtle,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Channel name
        Text(
            text = channel.name,
            color = if (isSelected) OnNavy else nameColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Stream type badge (show for VOD and SERIES; omit for LIVE to keep it clean)
        if (channel.streamType != StreamType.LIVE) {
            Spacer(modifier = Modifier.width(4.dp))
            StreamTypeBadge(streamType = channel.streamType)
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stream Type Badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StreamTypeBadge(streamType: StreamType) {
    val (label, color) = when (streamType) {
        StreamType.LIVE -> "LIVE" to LiveBadge
        StreamType.VOD -> "VOD" to VodBadge
        StreamType.SERIES -> "SER" to SeriesBadge
    }
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Video Player Panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VideoPlayerPanel(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    channel: Channel?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier.background(Color.Black)
    ) {
        // ExoPlayer view
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Bottom overlay gradient: channel name + LIVE badge
        if (channel != null && !isLoading && error == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (channel.streamType == StreamType.LIVE) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = LiveBadge.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = TealPrimary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Error overlay
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.80f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = OnNavyVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = error,
                        color = OnNavyVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Retry",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
