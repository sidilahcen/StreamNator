package com.streamnator.app.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.streamnator.app.domain.model.AppSettings

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("streamnator_prefs", Context.MODE_PRIVATE)

    fun getTheme(): String = prefs.getString("theme", "dark") ?: "dark"
    fun setTheme(value: String) = prefs.edit().putString("theme", value).apply()

    fun getLanguage(): String = prefs.getString("language", "en") ?: "en"
    fun setLanguage(value: String) = prefs.edit().putString("language", value).apply()

    fun getAutoRefresh(): Boolean = prefs.getBoolean("auto_refresh_startup", true)
    fun setAutoRefresh(value: Boolean) = prefs.edit().putBoolean("auto_refresh_startup", value).apply()

    fun getDefaultPlayer(): String = prefs.getString("default_player", "exoplayer") ?: "exoplayer"
    fun setDefaultPlayer(value: String) = prefs.edit().putString("default_player", value).apply()

    fun getEpgMatchMethod(): String = prefs.getString("epg_match_method", "tvg-id") ?: "tvg-id"
    fun setEpgMatchMethod(value: String) = prefs.edit().putString("epg_match_method", value).apply()

    fun getEpgRefreshInterval(): String = prefs.getString("epg_refresh_interval", "24h") ?: "24h"
    fun setEpgRefreshInterval(value: String) = prefs.edit().putString("epg_refresh_interval", value).apply()

    fun getPreferredQuality(): String = prefs.getString("preferred_quality", "auto") ?: "auto"
    fun setPreferredQuality(value: String) = prefs.edit().putString("preferred_quality", value).apply()

    fun getDefaultAspectRatio(): String = prefs.getString("default_aspect_ratio", "16:9") ?: "16:9"
    fun setDefaultAspectRatio(value: String) = prefs.edit().putString("default_aspect_ratio", value).apply()

    fun getBufferSize(): String = prefs.getString("buffer_size", "medium") ?: "medium"
    fun setBufferSize(value: String) = prefs.edit().putString("buffer_size", value).apply()

    fun getSkipInterval(): String = prefs.getString("skip_interval", "10s") ?: "10s"
    fun setSkipInterval(value: String) = prefs.edit().putString("skip_interval", value).apply()

    fun getActivePlaylistId(): Long = prefs.getLong("active_playlist_id", -1)
    fun setActivePlaylistId(id: Long) = prefs.edit().putLong("active_playlist_id", id).apply()

    fun getAllSettings(): AppSettings = AppSettings(
        theme = getTheme(),
        language = getLanguage(),
        autoRefreshOnStartup = getAutoRefresh(),
        defaultPlayer = getDefaultPlayer(),
        epgMatchMethod = getEpgMatchMethod(),
        epgAutoRefreshInterval = getEpgRefreshInterval(),
        preferredQuality = getPreferredQuality(),
        defaultAspectRatio = getDefaultAspectRatio(),
        bufferSize = getBufferSize(),
        skipInterval = getSkipInterval()
    )
}
