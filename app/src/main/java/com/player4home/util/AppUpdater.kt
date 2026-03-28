package com.player4home.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.player4home.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String
)

@Singleton
class AppUpdater @Inject constructor(
    private val httpClient: OkHttpClient
) {
    companion object {
        private const val API_URL =
            "https://api.github.com/repos/inenadic/player4home/releases/latest"
    }

    /** Returns UpdateInfo if a newer release is available, null otherwise. */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(
                Request.Builder()
                    .url(API_URL)
                    .header("Accept", "application/vnd.github+json")
                    .build()
            ).execute()
            if (!response.isSuccessful) return@withContext null
            val json = JSONObject(response.body?.string() ?: return@withContext null)
            val tag = json.getString("tag_name").trimStart('v')
            val assets = json.getJSONArray("assets")
            val downloadUrl = (0 until assets.length())
                .map { assets.getJSONObject(it) }
                .firstOrNull { it.getString("name").endsWith(".apk") }
                ?.getString("browser_download_url")
                ?: return@withContext null
            if (isNewer(tag, BuildConfig.VERSION_NAME)) UpdateInfo(tag, downloadUrl) else null
        } catch (_: Exception) {
            null
        }
    }

    /** Downloads the APK to cache, reporting progress 0–100. */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val response = httpClient.newCall(Request.Builder().url(downloadUrl).build()).execute()
        if (!response.isSuccessful) throw Exception("Download failed: ${response.code}")
        val body = response.body ?: throw Exception("Empty response")
        val total = body.contentLength()
        val apkFile = File(context.cacheDir, "update.apk")
        var downloaded = 0L
        body.byteStream().use { input ->
            apkFile.outputStream().use { output ->
                val buf = ByteArray(8_192)
                var n: Int
                while (input.read(buf).also { n = it } != -1) {
                    output.write(buf, 0, n)
                    downloaded += n
                    if (total > 0) onProgress((downloaded * 100 / total).toInt())
                }
            }
        }
        apkFile
    }

    /** Launches the system installer for the given APK file. */
    fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apkFile
        )
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        )
    }

    private fun isNewer(latest: String, installed: String): Boolean {
        val l = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val i = installed.split(".").map { it.toIntOrNull() ?: 0 }
        for (idx in 0 until maxOf(l.size, i.size)) {
            val diff = l.getOrElse(idx) { 0 } - i.getOrElse(idx) { 0 }
            if (diff != 0) return diff > 0
        }
        return false
    }
}
