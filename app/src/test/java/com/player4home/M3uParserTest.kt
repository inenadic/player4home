package com.player4home

import com.player4home.data.model.StreamType
import com.player4home.util.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {

    @Test
    fun `parse valid m3u returns correct channel count`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="bbc1" tvg-name="BBC One" tvg-logo="http://logo.com/bbc.png" group-title="UK",BBC One
            http://stream.example.com/bbc1
            #EXTINF:-1 tvg-id="cnn" group-title="News",CNN
            http://stream.example.com/cnn
            #EXTINF:-1 group-title="VOD",Some Movie
            http://stream.example.com/movie.mp4
        """.trimIndent()

        val channels = M3uParser.parse(m3u, playlistId = 1L)
        assertEquals(3, channels.size)
    }

    @Test
    fun `parse extracts channel names correctly`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1 group-title="News",BBC World News
            http://stream.example.com/bbc-world
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals("BBC World News", channels[0].name)
    }

    @Test
    fun `parse extracts stream urls correctly`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1,Channel 1
            http://stream.example.com/ch1
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals("http://stream.example.com/ch1", channels[0].streamUrl)
    }

    @Test
    fun `parse extracts tvg metadata`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="bbc1.uk" tvg-logo="http://logo/bbc.png" group-title="UK",BBC One
            http://stream.example.com/bbc1
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals("bbc1.uk", channels[0].tvgId)
        assertEquals("http://logo/bbc.png", channels[0].logoUrl)
        assertEquals("UK", channels[0].groupTitle)
    }

    @Test
    fun `parse assigns VOD stream type for VOD group`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1 group-title="VOD",The Matrix
            http://stream.example.com/matrix.mp4
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals(StreamType.VOD, channels[0].streamType)
    }

    @Test
    fun `parse assigns LIVE stream type by default`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1 group-title="Sports",ESPN
            http://stream.example.com/espn
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals(StreamType.LIVE, channels[0].streamType)
    }

    @Test
    fun `parse assigns SERIES stream type for Series group`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1 group-title="Series",Breaking Bad S01E01
            http://stream.example.com/bb101.mp4
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals(StreamType.SERIES, channels[0].streamType)
    }

    @Test
    fun `parse sets sort order correctly`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1,Channel A
            http://a.com
            #EXTINF:-1,Channel B
            http://b.com
            #EXTINF:-1,Channel C
            http://c.com
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals(0, channels[0].sortOrder)
        assertEquals(1, channels[1].sortOrder)
        assertEquals(2, channels[2].sortOrder)
    }

    @Test
    fun `parse ignores lines starting with hash except EXTINF`() {
        val m3u = """
            #EXTM3U
            #EXTVLCOPT:some-option
            #EXTINF:-1,My Channel
            http://stream.example.com/ch
        """.trimIndent()

        val channels = M3uParser.parse(m3u)
        assertEquals(1, channels.size)
    }

    @Test
    fun `parse returns empty list for empty input`() {
        val channels = M3uParser.parse("")
        assertTrue(channels.isEmpty())
    }

    @Test
    fun `parse returns empty list for m3u with no streams`() {
        val channels = M3uParser.parse("#EXTM3U")
        assertTrue(channels.isEmpty())
    }

    @Test
    fun `parse sets playlistId on all channels`() {
        val m3u = """
            #EXTM3U
            #EXTINF:-1,Ch1
            http://a.com
            #EXTINF:-1,Ch2
            http://b.com
        """.trimIndent()

        val channels = M3uParser.parse(m3u, playlistId = 42L)
        assertTrue(channels.all { it.playlistId == 42L })
    }
}
