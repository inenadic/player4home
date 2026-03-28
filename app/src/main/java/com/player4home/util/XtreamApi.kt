package com.player4home.util

import com.player4home.data.model.Channel
import com.player4home.data.model.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamApi @Inject constructor(private val client: OkHttpClient) {

    suspend fun fetchLiveChannels(host: String, username: String, password: String, playlistId: Long): List<Channel> {
        val url = "$host/player_api.php?username=$username&password=$password&action=get_live_streams"
        return fetchChannels(url, host, username, password, StreamType.LIVE, playlistId)
    }

    suspend fun fetchVodStreams(host: String, username: String, password: String, playlistId: Long): List<Channel> {
        val url = "$host/player_api.php?username=$username&password=$password&action=get_vod_streams"
        return fetchChannels(url, host, username, password, StreamType.VOD, playlistId)
    }

    private suspend fun fetchChannels(
        url: String,
        host: String,
        username: String,
        password: String,
        type: StreamType,
        playlistId: Long
    ): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            parseJsonStreams(body, host, username, password, type, playlistId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseJsonStreams(
        json: String,
        host: String,
        username: String,
        password: String,
        type: StreamType,
        playlistId: Long
    ): List<Channel> {
        return try {
            val array = org.json.JSONArray(json)
            val ext = if (type == StreamType.VOD) "mp4" else "ts"
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                val streamId = obj.optString("stream_id", obj.optString("vod_id", ""))
                Channel(
                    playlistId = playlistId,
                    name = obj.optString("name", "Unknown"),
                    streamUrl = "$host/$username/$password/$streamId.$ext",
                    logoUrl = obj.optString("stream_icon", ""),
                    groupTitle = obj.optString("category_name", ""),
                    streamType = type,
                    tvgId = obj.optString("epg_channel_id", ""),
                    sortOrder = i
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
