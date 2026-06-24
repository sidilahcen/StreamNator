package com.streamnator.app.service.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PlaylistRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = (applicationContext as StreamNatorApp).database
            val repo = PlaylistRepository(db)
            val playlists = db.playlistDao().getAutoRefreshPlaylists()
            var totalChannels = 0
            playlists.forEach { playlist ->
                try { totalChannels += repo.refreshPlaylist(playlist) }
                catch (e: Exception) { Log.e("Worker", "Refresh failed: ${e.message}") }
            }
            Log.d("Worker", "Refreshed $totalChannels channels")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PlaylistRefreshWorker>(24, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "playlist_refresh", ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}
