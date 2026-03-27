package com.player4home.data.db

import androidx.room.*
import com.player4home.data.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY lastUsedAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("UPDATE playlists SET lastUsedAt = :time WHERE id = :id")
    suspend fun updateLastUsed(id: Long, time: Long = System.currentTimeMillis())

    @Query("UPDATE playlists SET channelCount = :count WHERE id = :id")
    suspend fun updateChannelCount(id: Long, count: Int)
}
