package com.player4home.data.repository

import androidx.room.withTransaction
import com.player4home.data.db.AppDatabase
import com.player4home.data.db.ChannelDao
import com.player4home.data.db.PlaylistDao
import com.player4home.data.model.Channel
import com.player4home.data.model.Playlist
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val db: AppDatabase,
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao
) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): Playlist? = playlistDao.getPlaylistById(id)

    suspend fun addPlaylist(playlist: Playlist, channels: List<Channel>): Long =
        db.withTransaction {
            val id = playlistDao.insertPlaylist(playlist)
            if (channels.isNotEmpty()) {
                val mapped = channels.mapIndexed { i, ch -> ch.copy(playlistId = id, sortOrder = i) }
                channelDao.insertChannels(mapped)
                playlistDao.updateChannelCount(id, channels.size)
            }
            id
        }

    suspend fun updatePlaylist(playlist: Playlist) = playlistDao.updatePlaylist(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)

    suspend fun markUsed(id: Long) = playlistDao.updateLastUsed(id)

    fun getChannelsForPlaylist(playlistId: Long) = channelDao.getChannelsByPlaylist(playlistId)

    fun getGroupsForPlaylist(playlistId: Long) = channelDao.getGroupsForPlaylist(playlistId)

    fun searchChannels(playlistId: Long, query: String) = channelDao.searchChannels(playlistId, query)
}
