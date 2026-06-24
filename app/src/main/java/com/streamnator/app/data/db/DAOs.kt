package com.streamnator.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAll(): LiveData<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM playlists WHERE auto_refresh = 1")
    suspend fun getAutoRefreshPlaylists(): List<PlaylistEntity>

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getCount(): Int

    @Query("SELECT * FROM playlists WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): PlaylistEntity?
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlist_id = :playlistId ORDER BY group_title, name")
    fun getByPlaylist(playlistId: Long): LiveData<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlist_id = :playlistId AND group_title = :group ORDER BY name")
    fun getByGroup(playlistId: Long, group: String): LiveData<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlist_id = :playlistId AND name LIKE '%' || :query || '%' ORDER BY name")
    fun search(playlistId: Long, query: String): LiveData<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getById(id: Long): ChannelEntity?

    @Query("SELECT DISTINCT group_title FROM channels WHERE playlist_id = :playlistId AND group_title != '' ORDER BY group_title")
    fun getGroups(playlistId: Long): LiveData<List<String>>

    @Query("SELECT COUNT(*) FROM channels WHERE playlist_id = :playlistId")
    suspend fun getCount(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE playlist_id = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)

    @Query("SELECT * FROM channels WHERE playlist_id = :playlistId AND is_radio = 1")
    fun getRadioChannels(playlistId: Long): LiveData<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlist_id = :playlistId AND num = :num")
    suspend fun getByNumber(playlistId: Long, num: Int): ChannelEntity?
}

@Dao
interface FavoriteDao {
    @Query("""
        SELECT c.* FROM channels c
        INNER JOIN favorites f ON c.id = f.channel_id
        WHERE f.playlist_id = :playlistId
        ORDER BY f.added_at DESC
    """)
    fun getFavoritesWithChannels(playlistId: Long): LiveData<List<ChannelEntity>>

    @Query("SELECT * FROM favorites WHERE channel_id = :channelId AND playlist_id = :playlistId")
    suspend fun get(channelId: Long, playlistId: Long): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE channel_id = :channelId AND playlist_id = :playlistId")
    suspend fun delete(channelId: Long, playlistId: Long)
}

@Dao
interface RecentlyWatchedDao {
    @Query("""
        SELECT c.* FROM channels c
        INNER JOIN recently_watched r ON c.id = r.channel_id
        WHERE r.playlist_id = :playlistId
        ORDER BY r.watched_at DESC
        LIMIT :limit
    """)
    fun getRecentWithChannels(playlistId: Long, limit: Int = 20): LiveData<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recent: RecentlyWatchedEntity)
}

@Dao
interface EpgSourceDao {
    @Query("SELECT * FROM epg_sources")
    fun getAll(): LiveData<List<EpgSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: EpgSourceEntity): Long

    @Delete
    suspend fun delete(source: EpgSourceEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: SettingsEntity)
}