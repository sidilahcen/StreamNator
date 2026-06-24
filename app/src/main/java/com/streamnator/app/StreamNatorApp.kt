package com.streamnator.app

import android.app.Application
import com.streamnator.app.data.db.AppDatabase
import com.streamnator.app.data.db.PlaylistEntity
import com.streamnator.app.data.prefs.PreferencesHelper
import com.streamnator.app.data.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamNatorApp : Application() {
    lateinit var database: AppDatabase
    lateinit var prefs: PreferencesHelper

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        prefs = PreferencesHelper(this)

        // Seed default playlist only once
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultPlaylist()
        }
    }

    private suspend fun seedDefaultPlaylist() {
        val dao = database.playlistDao()
        val count = dao.getCount()
        if (count > 0) return // Already seeded

        val defaultUrl = "https://iptv-org.github.io/iptv/languages/eng.m3u"
        val existing = dao.getByUrl(defaultUrl)
        if (existing != null) return // Already exists

        val repo = PlaylistRepository(database)
        val defaultPlaylist = PlaylistEntity(
            name = "IPTV.org (English)",
            url = defaultUrl,
            type = "m3u",
            userAgent = "StreamNator/1.0",
            autoRefresh = false
        )
        val id = repo.insertPlaylist(defaultPlaylist)
        prefs.setActivePlaylistId(id)

        try {
            repo.refreshPlaylist(defaultPlaylist.copy(id = id))
        } catch (_: Exception) { }
    }
}