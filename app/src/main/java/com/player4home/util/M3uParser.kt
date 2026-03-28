package com.player4home.util

import com.player4home.data.model.Channel
import com.player4home.data.model.StreamType
import java.io.BufferedReader

object M3uParser {

    private val extinfRegex = Regex("""#EXTINF:-?\d+(.*)""")
    private val tvgIdRegex = Regex("""tvg-id="([^"]*)"""")
    private val tvgNameRegex = Regex("""tvg-name="([^"]*)"""")
    private val tvgLogoRegex = Regex("""tvg-logo="([^"]*)"""")
    private val groupTitleRegex = Regex("""group-title="([^"]*)"""")

    // Stream-based parse — reads line by line without loading entire file into memory
    fun parse(reader: BufferedReader, playlistId: Long = 0): List<Channel> {
        val channels = mutableListOf<Channel>()
        var currentMeta: String? = null
        var sortOrder = 0

        reader.forEachLine { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#EXTINF") -> {
                    currentMeta = trimmed
                }
                trimmed.isNotEmpty() && !trimmed.startsWith("#") && currentMeta != null -> {
                    val meta = currentMeta!!
                    val name = extinfRegex.find(meta)?.groupValues?.get(1)
                        ?.substringAfterLast(",")?.trim()
                        ?: trimmed

                    val tvgId    = tvgIdRegex.find(meta)?.groupValues?.get(1) ?: ""
                    val tvgName  = tvgNameRegex.find(meta)?.groupValues?.get(1) ?: ""
                    val logo     = tvgLogoRegex.find(meta)?.groupValues?.get(1) ?: ""
                    val group    = groupTitleRegex.find(meta)?.groupValues?.get(1) ?: ""
                    val streamType = when {
                        group.contains("VOD", ignoreCase = true) ||
                        group.contains("Movie", ignoreCase = true) -> StreamType.VOD
                        group.contains("Series", ignoreCase = true) ||
                        group.contains("Show", ignoreCase = true)  -> StreamType.SERIES
                        else -> StreamType.LIVE
                    }

                    channels.add(
                        Channel(
                            playlistId  = playlistId,
                            name        = name,
                            streamUrl   = trimmed,
                            logoUrl     = logo,
                            groupTitle  = group,
                            streamType  = streamType,
                            tvgId       = tvgId,
                            tvgName     = tvgName,
                            sortOrder   = sortOrder++
                        )
                    )
                    currentMeta = null
                }
            }
        }
        return channels
    }

    // String overload kept for tests
    fun parse(content: String, playlistId: Long = 0): List<Channel> =
        parse(content.reader().buffered(), playlistId)
}
