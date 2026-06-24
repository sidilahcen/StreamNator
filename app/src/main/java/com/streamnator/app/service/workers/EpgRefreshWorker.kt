package com.streamnator.app.service.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.repository.EpgRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class EpgRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = (applicationContext as StreamNatorApp).database
            val epgRepo = EpgRepository(db, applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context, intervalHours: Int = 24) {
            val request = PeriodicWorkRequestBuilder<EpgRefreshWorker>(intervalHours.toLong(), TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "epg_refresh", ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}
