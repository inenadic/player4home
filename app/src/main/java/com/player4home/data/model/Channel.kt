package com.player4home.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StreamType { LIVE, VOD, SERIES }

@Entity(
    tableName = "channels",
    foreignKeys = [ForeignKey(
        entity = Playlist::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("playlistId")]
)
data class Channel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val groupTitle: String = "",
    val streamType: StreamType = StreamType.LIVE,
    val tvgId: String = "",
    val tvgName: String = "",
    val sortOrder: Int = 0
)
