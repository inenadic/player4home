package com.player4home.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    showGroupSubtitle: Boolean = true,
    channelNumber: Int? = null
) {
    val streamColor = when (channel.streamType) {
        StreamType.LIVE   -> LiveBadge
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

    // Pulse animation for the currently-playing indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Teal left accent bar when currently playing
        if (isCurrentlyPlaying) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(TealPrimary)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(if (isCurrentlyPlaying) TealGlow else Color.Transparent)
                .padding(
                    start = if (isCurrentlyPlaying) 19.dp else 16.dp,
                    end = 16.dp,
                    top = 11.dp,
                    bottom = 11.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated pulse dot — shown when currently playing
            if (isCurrentlyPlaying) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TealPrimary.copy(alpha = pulseAlpha))
                )
                Spacer(Modifier.width(8.dp))
            }

            // Channel number — shown when provided (Smarters style)
            if (channelNumber != null) {
                Text(
                    text = channelNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isCurrentlyPlaying) TealPrimary else OnNavyVariant,
                    modifier = Modifier.width(32.dp),
                    maxLines = 1
                )
                Spacer(Modifier.width(4.dp))
            }

            // Channel logo — rounded square with subtle border
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyCardElevated)
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrentlyPlaying) TealPrimary else OnNavy,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // LIVE broadcast indicator inline with name
                    if (channel.streamType == StreamType.LIVE) {
                        Spacer(Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ErrorColor.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(ErrorColor)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
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

            // Stream type badge — slightly more prominent
            Text(
                text = streamLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = streamColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(streamColor.copy(alpha = 0.15f))
                    .border(0.5.dp, streamColor.copy(alpha = 0.30f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            )
        }
    }
}
