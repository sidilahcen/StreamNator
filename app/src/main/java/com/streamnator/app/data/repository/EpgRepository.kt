package com.streamnator.app.data.repository

import android.content.Context
import com.streamnator.app.data.db.AppDatabase
import com.streamnator.app.data.db.EpgSourceEntity
import com.streamnator.app.domain.model.EpgProgram
import com.streamnator.app.util.XmltvParser
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class EpgRepository(
    private val db: AppDatabase,
    private val context: Context
) {
    private val epgSourceDao = db.epgSourceDao()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val epgCacheDir = File(context.filesDir, "epg")

    fun getEpgSources(): LiveData<List<EpgSourceEntity>> = epgSourceDao.getAll()

    suspend fun addEpgSource(url: String): Long {
        return epgSourceDao.insert(EpgSourceEntity(url = url))
    }

    suspend fun removeEpgSource(source: EpgSourceEntity) {
        epgSourceDao.delete(source)
    }

    suspend fun fetchEpg(source: EpgSourceEntity): List<EpgProgram> = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(source.url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("EPG fetch failed: ${response.code}")

            val body = response.body ?: throw Exception("Empty EPG response")
            val inputStream = body.byteStream()

            // Cache to file
            epgCacheDir.mkdirs()
            val cacheFile = File(epgCacheDir, "epg_${source.id}.xml")
            FileOutputStream(cacheFile).use { output ->
                inputStream.copyTo(output)
            }

            // Parse
            cacheFile.inputStream().use { stream ->
                val programs = XmltvParser.parse(stream)
                epgSourceDao.insert(source.copy(lastFetched = System.currentTimeMillis()))
                programs
            }
        }
    }

    fun getProgramsForChannel(programs: List<EpgProgram>, channelId: String, tvgName: String): List<EpgProgram> {
        return programs.filter {
            it.channelId == channelId || it.channelId == tvgName
        }.sortedBy { it.startTime }
    }

    fun getCurrentProgram(programs: List<EpgProgram>): EpgProgram? {
        val now = System.currentTimeMillis()
        return programs.find { it.startTime <= now && it.endTime >= now }
    }
}
