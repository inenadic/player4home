package com.player4home.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlaylistType { URL, FILE, XTREAM }

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PlaylistType,
    val url: String = "",
    val filePath: String = "",
    val xtreamHost: String = "",
    val xtreamUsername: String = "",
    val xtreamPassword: String = "",
    val isPinProtected: Boolean = false,
    val pin: String = "",
    val channelCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
)
