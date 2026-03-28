package com.player4home.data.db

import androidx.room.*
import com.player4home.data.model.EpgEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {
    @Query("SELECT * FROM epg_entries WHERE channelTvgId = :tvgId AND endTime > :now ORDER BY startTime ASC LIMIT 10")
    fun getUpcomingForChannel(tvgId: String, now: Long = System.currentTimeMillis()): Flow<List<EpgEntry>>

    @Query("SELECT * FROM epg_entries WHERE channelTvgId = :tvgId AND startTime <= :now AND endTime > :now LIMIT 1")
    suspend fun getCurrentProgram(tvgId: String, now: Long = System.currentTimeMillis()): EpgEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<EpgEntry>)

    @Query("DELETE FROM epg_entries WHERE endTime < :before")
    suspend fun deleteOldEntries(before: Long = System.currentTimeMillis())
}
