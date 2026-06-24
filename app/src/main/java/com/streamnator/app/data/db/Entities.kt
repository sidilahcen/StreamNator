package com.streamnator.app.data.db

import androidx.room.*

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String? = null,
    @ColumnInfo(name = "file_path") val filePath: String? = null,
    val type: String,
    @ColumnInfo(name = "xtream_host") val xtreamHost: String? = null,
    @ColumnInfo(name = "xtream_user") val xtreamUser: String? = null,
    @ColumnInfo(name = "xtream_pass") val xtreamPass: String? = null,
    @ColumnInfo(name = "stalker_url") val stalkerUrl: String? = null,
    @ColumnInfo(name = "stalker_mac") val stalkerMac: String? = null,
    @ColumnInfo(name = "user_agent") val userAgent: String? = null,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = 0,
    @ColumnInfo(name = "auto_refresh") val autoRefresh: Boolean = true
)

@Entity(
    tableName = "channels",
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = ["id"],
        childColumns = ["playlist_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("playlist_id"), Index("group_title"), Index("name")]
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    val name: String,
    val url: String,
    @ColumnInfo(name = "group_title") val groupTitle: String = "",
    @ColumnInfo(name = "tvg_id") val tvgId: String = "",
    @ColumnInfo(name = "tvg_name") val tvgName: String = "",
    @ColumnInfo(name = "tvg_logo") val tvgLogo: String = "",
    val num: Int = 0,
    val catchup: String = "",
    @ColumnInfo(name = "catchup_source") val catchupSource: String = "",
    @ColumnInfo(name = "catchup_days") val catchupDays: Int = 0,
    @ColumnInfo(name = "is_radio") val isRadio: Boolean = false
)

@Entity(
    tableName = "favorites",
    foreignKeys = [ForeignKey(
        entity = ChannelEntity::class,
        parentColumns = ["id"],
        childColumns = ["channel_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("channel_id"), Index("playlist_id")]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "channel_id") val channelId: Long,
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recently_watched",
    foreignKeys = [ForeignKey(
        entity = ChannelEntity::class,
        parentColumns = ["id"],
        childColumns = ["channel_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("channel_id"), Index("playlist_id")]
)
data class RecentlyWatchedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "channel_id") val channelId: Long,
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    @ColumnInfo(name = "watched_at") val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "epg_sources")
data class EpgSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    @ColumnInfo(name = "last_fetched") val lastFetched: Long = 0
)

@Entity(tableName = "settings", primaryKeys = ["key"])
data class SettingsEntity(
    val key: String,
    val value: String
)
