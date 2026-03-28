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

    // Word-boundary patterns to avoid false matches (e.g. "VODAFONE" matching "VOD")
    private val vodPattern    = Regex("""\bVOD\b""", RegexOption.IGNORE_CASE)
    private val moviePattern  = Regex("""\bMovies?\b""", RegexOption.IGNORE_CASE)
    private val seriesPattern = Regex("""\bSeries\b""", RegexOption.IGNORE_CASE)
    private val showPattern   = Regex("""\bShows?\b""", RegexOption.IGNORE_CASE)

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
                        seriesPattern.containsMatchIn(group) ||
                        showPattern.containsMatchIn(group)   -> StreamType.SERIES
                        vodPattern.containsMatchIn(group) ||
                        moviePattern.containsMatchIn(group)  -> StreamType.VOD
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
