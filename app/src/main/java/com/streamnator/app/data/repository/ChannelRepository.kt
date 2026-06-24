package com.streamnator.app.data.repository

import androidx.lifecycle.LiveData
import com.streamnator.app.data.db.*

class ChannelRepository(
    private val channelDao: ChannelDao,
    private val favoriteDao: FavoriteDao,
    private val recentlyWatchedDao: RecentlyWatchedDao
) {
    fun getChannelsByPlaylist(playlistId: Long): LiveData<List<ChannelEntity>> =
        channelDao.getByPlaylist(playlistId)

    fun getChannelsByGroup(playlistId: Long, group: String): LiveData<List<ChannelEntity>> =
        channelDao.getByGroup(playlistId, group)

    fun searchChannels(playlistId: Long, query: String): LiveData<List<ChannelEntity>> =
        channelDao.search(playlistId, query)

    fun getGroups(playlistId: Long): LiveData<List<String>> =
        channelDao.getGroups(playlistId)

    suspend fun getChannel(id: Long): ChannelEntity? = channelDao.getById(id)

    fun getFavorites(playlistId: Long): LiveData<List<ChannelEntity>> =
        favoriteDao.getFavoritesWithChannels(playlistId)

    fun getRecentlyWatched(playlistId: Long, limit: Int = 20): LiveData<List<ChannelEntity>> =
        recentlyWatchedDao.getRecentWithChannels(playlistId, limit)

    suspend fun toggleFavorite(channelId: Long, playlistId: Long) {
        val existing = favoriteDao.get(channelId, playlistId)
        if (existing != null) {
            favoriteDao.delete(channelId, playlistId)
        } else {
            favoriteDao.insert(FavoriteEntity(channelId = channelId, playlistId = playlistId))
        }
    }

    suspend fun addToRecentlyWatched(channelId: Long, playlistId: Long) {
        recentlyWatchedDao.insert(
            RecentlyWatchedEntity(channelId = channelId, playlistId = playlistId)
        )
    }

    suspend fun isFavorite(channelId: Long, playlistId: Long): Boolean =
        favoriteDao.get(channelId, playlistId) != null

    suspend fun getChannelByNumber(playlistId: Long, num: Int): ChannelEntity? =
        channelDao.getByNumber(playlistId, num)
}