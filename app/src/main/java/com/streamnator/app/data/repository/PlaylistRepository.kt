package com.streamnator.app.data.repository

import androidx.lifecycle.LiveData
import com.streamnator.app.data.db.*
import com.streamnator.app.data.network.ApiClient
import com.streamnator.app.util.M3uParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class PlaylistRepository(
    private val db: AppDatabase,
    private val playlistDao: PlaylistDao = db.playlistDao(),
    private val channelDao: ChannelDao = db.channelDao(),
    private val favoriteDao: FavoriteDao = db.favoriteDao(),
    private val recentlyWatchedDao: RecentlyWatchedDao = db.recentlyWatchedDao()
) {
    fun getAllPlaylists(): LiveData<List<PlaylistEntity>> = playlistDao.getAll()

    suspend fun getPlaylist(id: Long): PlaylistEntity? = playlistDao.getById(id)

    suspend fun insertPlaylist(playlist: PlaylistEntity): Long = playlistDao.insert(playlist)

    suspend fun updatePlaylist(playlist: PlaylistEntity) = playlistDao.update(playlist)

    suspend fun deletePlaylist(id: Long) {
        channelDao.deleteByPlaylist(id)
        playlistDao.deleteById(id)
    }

    suspend fun refreshPlaylist(playlist: PlaylistEntity): Int = withContext(Dispatchers.IO) {
        val userAgent = playlist.userAgent ?: "StreamNator/1.0"

        val entries = when (playlist.type) {
            "m3u" -> {
                val url = playlist.url ?: throw Exception("No URL for M3U playlist")
                M3uParser.parseFromUrl(url, userAgent)
            }
            "xtream" -> {
                refreshXtreamPlaylist(playlist)
                return@withContext channelDao.getCount(playlist.id)
            }
            else -> throw Exception("Unsupported playlist type")
        }

        channelDao.deleteByPlaylist(playlist.id)
        val entities = M3uParser.toChannelEntities(entries, playlist.id)
        entities.chunked(500).forEach { chunk ->
            channelDao.insertAll(chunk)
        }

        playlistDao.update(playlist.copy(lastUpdated = System.currentTimeMillis()))
        entries.size
    }

    suspend fun refreshXtreamPlaylist(playlist: PlaylistEntity) {
        val host = playlist.xtreamHost ?: throw Exception("No Xtream host")
        val user = playlist.xtreamUser ?: throw Exception("No Xtream username")
        val pass = playlist.xtreamPass ?: throw Exception("No Xtream password")

        val api = ApiClient.createXtreamApi(host)

        try {
            val categories = api.getLiveCategories(user, pass)
            channelDao.deleteByPlaylist(playlist.id)

            categories.forEach { category ->
                try {
                    val streams = api.getLiveStreams(user, pass, categoryId = category.categoryId)
                    val entities = streams.mapIndexed { index, stream ->
                        ChannelEntity(
                            playlistId = playlist.id,
                            name = stream.name,
                            url = "${host.trimEnd('/')}/${user}/${pass}/${stream.streamId}",
                            groupTitle = category.categoryName,
                            tvgLogo = stream.streamIcon,
                            num = index + 1
                        )
                    }
                    channelDao.insertAll(entities)
                } catch (_: Exception) { }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        playlistDao.update(playlist.copy(lastUpdated = System.currentTimeMillis()))
    }

    suspend fun fetchM3uUrl(url: String, userAgent: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            response.body?.string() ?: throw Exception("Empty response")
        }
    }
}