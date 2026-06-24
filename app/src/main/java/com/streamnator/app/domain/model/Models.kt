package com.streamnator.app.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val url: String? = null,
    val filePath: String? = null,
    val type: PlaylistType,
    val xtreamHost: String? = null,
    val xtreamUser: String? = null,
    val xtreamPass: String? = null,
    val stalkerUrl: String? = null,
    val stalkerMac: String? = null,
    val userAgent: String? = null,
    val lastUpdated: Long = 0,
    val autoRefresh: Boolean = true,
    val channelCount: Int = 0
)

enum class PlaylistType { M3U, XTREAM, STALKER }

data class Channel(
    val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val url: String,
    val groupTitle: String = "",
    val tvgId: String = "",
    val tvgName: String = "",
    val tvgLogo: String = "",
    val num: Int = 0,
    val catchup: String = "",
    val catchupSource: String = "",
    val catchupDays: Int = 0,
    val isRadio: Boolean = false,
    val isFavorite: Boolean = false,
    val epgNowTitle: String = "",
    val epgNowStart: Long = 0,
    val epgNowEnd: Long = 0,
    val epgNowProgress: Float = 0f
)

data class Favorite(
    val id: Long = 0,
    val channelId: Long,
    val playlistId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

data class RecentlyWatched(
    val id: Long = 0,
    val channelId: Long,
    val playlistId: Long,
    val watchedAt: Long = System.currentTimeMillis()
)

data class EpgSource(
    val id: Long = 0,
    val url: String,
    val lastFetched: Long = 0
)

data class EpgProgram(
    val channelId: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val icon: String = "",
    val categories: List<String> = emptyList()
)

data class XtreamCategory(
    val categoryId: String,
    val categoryName: String,
    val parentId: Int = 0
)

data class XtreamStream(
    val streamId: Int,
    val name: String,
    val streamType: String,
    val streamIcon: String = "",
    val categoryId: String,
    val added: String = "",
    val rating: String = "",
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    val releaseDate: String = "",
    val youtubeTrailer: String = "",
    val episodeRunTime: String = "",
    val containerExtension: String = ""
)

data class XtreamSeries(
    val seriesId: Int,
    val name: String,
    val cover: String = "",
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    val releaseDate: String = "",
    val rating: String = "",
    val categoryId: String
)

data class XtreamSeriesEpisode(
    val id: Int,
    val episodeNum: Int,
    val title: String,
    val containerExtension: String = "",
    val info: XtreamEpisodeInfo? = null
)

data class XtreamEpisodeInfo(
    val plot: String = "",
    val durationSecs: Int = 0,
    val movieImage: String = ""
)

data class AppSettings(
    val theme: String = "dark",
    val language: String = "en",
    val autoRefreshOnStartup: Boolean = true,
    val defaultPlayer: String = "exoplayer",
    val epgMatchMethod: String = "tvg-id",
    val epgAutoRefreshInterval: String = "24h",
    val preferredQuality: String = "auto",
    val defaultAspectRatio: String = "16:9",
    val bufferSize: String = "medium",
    val skipInterval: String = "10s"
)
