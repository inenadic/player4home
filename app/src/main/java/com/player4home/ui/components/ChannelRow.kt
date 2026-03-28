package com.player4home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.player4home.data.model.Channel
import com.player4home.data.model.StreamType
import com.player4home.ui.theme.*

@Composable
fun ChannelRow(
    channel: Channel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentlyPlaying: Boolean = false,
    showGroupSubtitle: Boolean = true
) {
    val streamColor = when (channel.streamType) {
        StreamType.LIVE   -> TealPrimary
        StreamType.VOD    -> VodBadge
        StreamType.SERIES -> SeriesBadge
    }
    val streamLabel = when (channel.streamType) {
        StreamType.LIVE   -> "LIVE"
        StreamType.VOD    -> "VOD"
        StreamType.SERIES -> "SERIES"
    }
    val fallbackIcon = when (channel.streamType) {
        StreamType.LIVE   -> Icons.Filled.Tv
        StreamType.VOD    -> Icons.Filled.Movie
        StreamType.SERIES -> Icons.Filled.PlayArrow
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isCurrentlyPlaying) TealGlow else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel logo — rounded square
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NavyCardElevated),
            contentAlignment = Alignment.Center
        ) {
            if (channel.logoUrl.isNotEmpty()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = streamColor.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentlyPlaying) TealPrimary else OnNavy
            )
            if (showGroupSubtitle && channel.groupTitle.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = channel.groupTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnNavyVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Stream type badge
        Text(
            text = streamLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = streamColor,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(streamColor.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
