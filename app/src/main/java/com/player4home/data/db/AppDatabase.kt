package com.player4home.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.player4home.data.model.Channel
import com.player4home.data.model.EpgEntry
import com.player4home.data.model.Playlist
import com.player4home.data.model.PlaylistType
import com.player4home.data.model.StreamType

class Converters {
    @TypeConverter
    fun fromPlaylistType(value: PlaylistType): String = value.name

    @TypeConverter
    fun toPlaylistType(value: String): PlaylistType = PlaylistType.valueOf(value)

    @TypeConverter
    fun fromStreamType(value: StreamType): String = value.name

    @TypeConverter
    fun toStreamType(value: String): StreamType = StreamType.valueOf(value)
}

@Database(
    entities = [Playlist::class, Channel::class, EpgEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun epgDao(): EpgDao
}
