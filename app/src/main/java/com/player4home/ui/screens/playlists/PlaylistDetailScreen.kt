package com.player4home.ui.screens.playlists

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.player4home.R
import com.player4home.data.model.Channel
import com.player4home.data.model.StreamType
import com.player4home.ui.components.ChannelRow
import com.player4home.ui.navigation.Screen
import com.player4home.ui.theme.*

private fun categoryIcon(name: String): ImageVector = when {
    name == "ALL"                                                       -> Icons.Filled.GridView
    name.contains("sport",    ignoreCase = true) ||
    name.contains("football", ignoreCase = true) ||
    name.contains("soccer",   ignoreCase = true) ||
    name.contains("tennis",   ignoreCase = true) ||
    name.contains("basket",   ignoreCase = true)                       -> Icons.Filled.SportsSoccer
    name.contains("news",     ignoreCase = true)                       -> Icons.Filled.Newspaper
    name.contains("movie",    ignoreCase = true) ||
    name.contains("film",     ignoreCase = true) ||
    name.contains("cinema",   ignoreCase = true)                       -> Icons.Filled.Movie
    name.contains("series",   ignoreCase = true) ||
    name.contains("show",     ignoreCase = true) ||
    name.contains("episode",  ignoreCase = true)                       -> Icons.Filled.VideoLibrary
    name.contains("music",    ignoreCase = true) ||
    name.contains("radio",    ignoreCase = true)                       -> Icons.Filled.MusicNote
    name.contains("kids",     ignoreCase = true) ||
    name.contains("child",    ignoreCase = true) ||
    name.contains("cartoon",  ignoreCase = true) ||
    name.contains("anime",    ignoreCase = true)                       -> Icons.Filled.ChildCare
    name.contains("docu",     ignoreCase = true)                       -> Icons.Filled.AutoStories
    name.contains("entertain",ignoreCase = true)                       -> Icons.Filled.Theaters
    else                                                               -> Icons.Filled.Folder
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    navController: NavController,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // ── Top bar ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = OnNavy
                )
            }
            Text(
                text = uiState.playlistName.ifEmpty { stringResource(R.string.screen_playlist_detail) },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = OnNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (uiState.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp).padding(end = 8.dp),
                    color = TealPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh playlist",
                        tint = OnNavyVariant
                    )
                }
            }
        }

        // ── Type filter chips ─────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChannelTab.entries.forEach { tab ->
                TypeChip(
                    label = when (tab) {
                        ChannelTab.ALL    -> "ALL"
                        ChannelTab.LIVE   -> "LIVE"
                        ChannelTab.VOD    -> "MOVIES"
                        ChannelTab.SERIES -> "SERIES"
                    },
                    selected = uiState.selectedTab == tab,
                    accentColor = when (tab) {
                        ChannelTab.ALL    -> TealPrimary
                        ChannelTab.LIVE   -> LiveBadge
                        ChannelTab.VOD    -> VodBadge
                        ChannelTab.SERIES -> SeriesBadge
                    },
                    onClick = { viewModel.onTabSelected(tab) }
                )
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // ── Content ───────────────────────────────────────────
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                // Left panel — categories
                CategoriesPanel(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight(),
                    groups = uiState.groups,
                    selectedGroup = uiState.selectedGroup,
                    onGroupSelected = { viewModel.onGroupSelected(it) },
                    pinnedGroups = uiState.pinnedGroups,
                    onTogglePin = { viewModel.onTogglePin(it) }
                )

                // Panel divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Divider)
                )

                // Right panel — channels
                ChannelsPanel(
                    modifier = Modifier
                        .weight(0.72f)
                        .fillMaxHeight(),
                    channels = uiState.channels,
                    searchQuery = uiState.searchQuery,
                    selectedTab = uiState.selectedTab,
                    onSearch = { viewModel.onSearch(it) },
                    onChannelClick = { channel ->
                        navController.navigate(
                            Screen.Player.createRoute(channel.playlistId, channel.id)
                        )
                    }
                )
            }
        }
    }
}

// ── Type filter chip ──────────────────────────────────────────

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (selected) accentColor.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (selected) accentColor else OnNavyVariant
    val borderColor = if (selected) accentColor.copy(alpha = 0.4f) else Color.Transparent

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

// ── Left panel — categories ───────────────────────────────────

@Composable
private fun CategoriesPanel(
    modifier: Modifier,
    groups: List<Pair<String, Int>>,
    selectedGroup: String?,
    onGroupSelected: (String?) -> Unit,
    pinnedGroups: Set<String>,
    onTogglePin: (String) -> Unit
) {
    var filterText by remember { mutableStateOf("") }
    val visible = if (filterText.isBlank()) groups
                  else groups.filter { (name, _) ->
                      name == "ALL" || name.contains(filterText, ignoreCase = true)
                  }

    Column(modifier = modifier.background(NavySurface)) {
        // Section header
        Text(
            text = "CATEGORIES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = OnNavySubtle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, top = 12.dp, bottom = 6.dp)
        )

        // Search groups
        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    "Filter…",
                    color = OnNavySubtle,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, null, tint = OnNavyVariant, modifier = Modifier.size(16.dp))
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = OnNavy,
                unfocusedTextColor = OnNavy,
                cursorColor = TealPrimary,
                focusedContainerColor = NavyCard,
                unfocusedContainerColor = NavyCard
            ),
            textStyle = MaterialTheme.typography.bodySmall
        )

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(visible, key = { it.first }) { (name, count) ->
                val isSelected = (name == "ALL" && selectedGroup == null) ||
                                 name == selectedGroup
                CategoryItem(
                    name = name,
                    count = count,
                    isSelected = isSelected,
                    onClick = { onGroupSelected(if (name == "ALL") null else name) },
                    isPinned = name in pinnedGroups,
                    onTogglePin = if (name == "ALL") null else ({ onTogglePin(name) })
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    name: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) TealContainer else Color.Transparent)
            .padding(end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(
                    if (isSelected) TealPrimary else Color.Transparent,
                    RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = name,
            color = if (isSelected) TealPrimary else OnNavyVariant,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(4.dp))
        // Count badge — pill shape
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (isSelected) TealPrimary.copy(alpha = 0.18f)
                    else NavyCardElevated
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = if (isSelected) TealPrimary else OnNavyVariant,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                fontSize = 10.sp
            )
        }
        if (onTogglePin != null) {
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Unpin category" else "Pin category",
                    tint = if (isPinned) AmberSecondary else OnNavySubtle,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 15.dp),
        color = Divider,
        thickness = 0.5.dp
    )
}

// ── Right panel — channels ────────────────────────────────────

@Composable
private fun ChannelsPanel(
    modifier: Modifier,
    channels: List<Channel>,
    searchQuery: String,
    selectedTab: ChannelTab,
    onSearch: (String) -> Unit,
    onChannelClick: (Channel) -> Unit
) {
    val isGrid = selectedTab == ChannelTab.VOD || selectedTab == ChannelTab.SERIES

    Column(modifier = modifier.background(NavyBackground)) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            placeholder = {
                Text(
                    stringResource(R.string.search_channels_hint),
                    color = OnNavySubtle,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, null, tint = OnNavyVariant, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = OnNavy,
                unfocusedTextColor = OnNavy,
                cursorColor = TealPrimary,
                focusedContainerColor = NavyCard,
                unfocusedContainerColor = NavyCard
            )
        )

        if (channels.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isNotBlank()) stringResource(R.string.search_no_results)
                           else stringResource(R.string.detail_no_channels),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnNavyVariant
                )
            }
        } else {
            // Channel count line
            Text(
                text = "${channels.size} ${if (isGrid) "titles" else "channels"}",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = OnNavySubtle,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
            )

            if (isGrid) {
                VodGrid(channels = channels, onChannelClick = onChannelClick)
            } else {
                LiveList(channels = channels, onChannelClick = onChannelClick)
            }
        }
    }
}

// ── VOD / Series poster grid ──────────────────────────────────

@Composable
private fun VodGrid(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            VodPosterCard(channel = channel, onClick = { onChannelClick(channel) })
        }
    }
}

@Composable
private fun VodPosterCard(channel: Channel, onClick: () -> Unit) {
    val badgeColor = when (channel.streamType) {
        StreamType.VOD    -> VodBadge
        StreamType.SERIES -> SeriesBadge
        StreamType.LIVE   -> LiveBadge
    }
    val badgeLabel = when (channel.streamType) {
        StreamType.VOD    -> "VOD"
        StreamType.SERIES -> "SERIES"
        StreamType.LIVE   -> "LIVE"
    }
    val fallbackIcon = when (channel.streamType) {
        StreamType.VOD    -> Icons.Filled.Movie
        StreamType.SERIES -> Icons.Filled.PlayArrow
        StreamType.LIVE   -> Icons.Filled.PlayArrow
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Thumbnail / poster area (2:3 portrait aspect)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            ) {
                if (channel.logoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Gradient background with icon when no logo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(NavyCardElevated, NavySurface)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = badgeColor.copy(alpha = 0.5f)
                        )
                    }
                }

                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.55f to Color.Transparent,
                                    1f to Color(0xCC000000)
                                )
                            )
                        )
                )

                // Stream type badge — top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Title below the image
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
                Text(
                    text = channel.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    ),
                    color = OnNavy
                )
            }
        }
    }
}

// ── Live / ALL channel list ───────────────────────────────────

@Composable
private fun LiveList(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(channels, key = { _, ch -> ch.id }) { index, channel ->
            ChannelRow(
                channel = channel,
                showGroupSubtitle = false,
                channelNumber = index + 1,
                onClick = { onChannelClick(channel) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = Divider,
                thickness = 0.5.dp
            )
        }
    }
}
