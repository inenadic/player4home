package com.player4home.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.player4home.R
import com.player4home.data.model.Channel
import com.player4home.ui.components.ChannelRow
import com.player4home.ui.navigation.Screen
import com.player4home.ui.theme.*

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
                    onGroupSelected = { viewModel.onGroupSelected(it) }
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
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) TealContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) TealPrimary else OnNavyVariant,
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
    onGroupSelected: (String?) -> Unit
) {
    var filterText by remember { mutableStateOf("") }
    val visible = if (filterText.isBlank()) groups
                  else groups.filter { (name, _) ->
                      name == "ALL" || name.contains(filterText, ignoreCase = true)
                  }

    Column(modifier = modifier.background(NavySurface)) {
        // Search groups
        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            placeholder = {
                Text(
                    "Search categories",
                    color = OnNavySubtle,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, null, tint = OnNavyVariant, modifier = Modifier.size(18.dp))
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
                    onClick = { onGroupSelected(if (name == "ALL") null else name) }
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) TealContainer else Color.Transparent)
            .padding(end = 12.dp, top = 11.dp, bottom = 11.dp),
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
        Spacer(Modifier.width(12.dp))
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
        Text(
            text = count.toString(),
            color = if (isSelected) TealPrimary.copy(alpha = 0.7f) else OnNavySubtle,
            style = MaterialTheme.typography.labelMedium
        )
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
    onSearch: (String) -> Unit,
    onChannelClick: (Channel) -> Unit
) {
    Column(modifier = modifier.background(NavyBackground)) {
        // Search channels
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            placeholder = {
                Text(
                    stringResource(R.string.search_channels_hint),
                    color = OnNavySubtle
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
            // Channel count
            Text(
                text = "${channels.size} channels",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = OnNavySubtle,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
            LazyColumn(Modifier.fillMaxSize()) {
                items(channels, key = { it.id }) { channel ->
                    ChannelRow(
                        channel = channel,
                        showGroupSubtitle = false,
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
    }
}
