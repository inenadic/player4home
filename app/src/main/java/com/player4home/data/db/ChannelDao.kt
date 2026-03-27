package com.player4home.data.db

import androidx.room.*
import com.player4home.data.model.Channel
import com.player4home.data.model.StreamType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND streamType = :type ORDER BY sortOrder ASC")
    fun getChannelsByType(playlistId: Long, type: StreamType): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND groupTitle = :group ORDER BY sortOrder ASC")
    fun getChannelsByGroup(playlistId: Long, group: String): Flow<List<Channel>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE playlistId = :playlistId ORDER BY groupTitle ASC")
    fun getGroupsForPlaylist(playlistId: Long): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND (name LIKE '%' || :query || '%') ORDER BY sortOrder ASC")
    fun searchChannels(playlistId: Long, query: String): Flow<List<Channel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsForPlaylist(playlistId: Long)

    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun countChannels(playlistId: Long): Int
}
