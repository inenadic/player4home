package com.player4home.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_entries")
data class EpgEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelTvgId: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long
)
