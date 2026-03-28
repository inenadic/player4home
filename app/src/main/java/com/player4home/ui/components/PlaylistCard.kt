package com.player4home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.player4home.R
import com.player4home.data.model.Playlist
import com.player4home.data.model.PlaylistType
import com.player4home.ui.theme.*

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (accentColor, typeIcon) = when (playlist.type) {
        PlaylistType.URL    -> TealPrimary to Icons.Filled.Link
        PlaylistType.FILE   -> AmberSecondary to Icons.Filled.Storage
        PlaylistType.XTREAM -> VodBadge to Icons.Filled.Tv
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent left bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        brush = Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.3f))),
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            // Type icon circle
            Box(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 14.dp, bottom = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (playlist.isPinProtected) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AmberSecondary
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type badge
                    Text(
                        text = when (playlist.type) {
                            PlaylistType.URL    -> "URL"
                            PlaylistType.FILE   -> "FILE"
                            PlaylistType.XTREAM -> "XTREAM"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    if (playlist.channelCount > 0) {
                        Text(
                            text = "${playlist.channelCount} channels",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnNavyVariant
                        )
                    }
                }
            }

            // Actions
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.playlist_edit),
                    tint = OnNavyVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.padding(end = 4.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.playlist_delete),
                    tint = ErrorColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
