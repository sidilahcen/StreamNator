package com.streamnator.app.util

import com.streamnator.app.data.db.ChannelEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object M3uParser {

    data class M3uEntry(
        val name: String = "",
        val url: String = "",
        val tvgId: String = "",
        val tvgName: String = "",
        val tvgLogo: String = "",
        val groupTitle: String = "",
        val num: Int = 0,
        val catchup: String = "",
        val catchupSource: String = "",
        val catchupDays: Int = 0,
        val isRadio: Boolean = false
    )

    suspend fun parseFromUrl(
        playlistUrl: String,
        userAgent: String = "StreamNator/1.0",
        onProgress: ((Int) -> Unit)? = null
    ): List<M3uEntry> {
        val entries = mutableListOf<M3uEntry>()
        val connection = URL(playlistUrl).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", userAgent)
        connection.connectTimeout = 15000
        connection.readTimeout = 30000

        connection.inputStream.use { stream ->
            val reader = BufferedReader(InputStreamReader(stream, detectEncoding(stream)))
            val lines = reader.readLines()
            val total = lines.size
            var i = 0

            while (i < lines.size) {
                val line = lines[i].trim()
                if (line.startsWith("#EXTINF:")) {
                    val attrs = parseExtinfAttributes(line)
                    i++
                    if (i < lines.size) {
                        val url = lines[i].trim()
                        if (url.isNotEmpty() && !url.startsWith("#")) {
                            attrs["url"] = url
                            val entry = buildEntry(attrs)
                            entries.add(entry)
                        }
                    }
                }
                i++
                if (i % 500 == 0) onProgress?.invoke(i * 100 / total)
            }
        }
        onProgress?.invoke(100)
        return entries
    }

    suspend fun parseFromFile(
        content: String,
        onProgress: ((Int) -> Unit)? = null
    ): List<M3uEntry> {
        val entries = mutableListOf<M3uEntry>()
        val lines = content.lines()
        val total = lines.size
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val attrs = parseExtinfAttributes(line)
                i++
                if (i < lines.size) {
                    val url = lines[i].trim()
                    if (url.isNotEmpty() && !url.startsWith("#")) {
                        attrs["url"] = url
                        val entry = buildEntry(attrs)
                        entries.add(entry)
                    }
                }
            }
            i++
            if (i % 500 == 0) onProgress?.invoke(i * 100 / total)
        }
        onProgress?.invoke(100)
        return entries
    }

    fun toChannelEntities(entries: List<M3uEntry>, playlistId: Long): List<ChannelEntity> {
        return entries.mapIndexed { index, entry ->
            ChannelEntity(
                playlistId = playlistId,
                name = entry.name,
                url = entry.url,
                groupTitle = entry.groupTitle,
                tvgId = entry.tvgId,
                tvgName = entry.tvgName,
                tvgLogo = entry.tvgLogo,
                num = if (entry.num > 0) entry.num else index + 1,
                catchup = entry.catchup,
                catchupSource = entry.catchupSource,
                catchupDays = entry.catchupDays,
                isRadio = entry.isRadio
            )
        }
    }

    private fun parseExtinfAttributes(line: String): MutableMap<String, String> {
        val attrs = mutableMapOf<String, String>()

        // Extract duration and name
        val infPart = line.substringAfter("#EXTINF:").trim()
        val commaIdx = infPart.indexOf(',')

        val attrPart = if (commaIdx > 0) infPart.substring(0, commaIdx).trim() else infPart
        val namePart = if (commaIdx > 0) infPart.substring(commaIdx + 1).trim() else ""

        attrs["name"] = namePart

        // Parse quoted attributes: key="value"
        val quotedRegex = Regex("""([a-zA-Z-]+)="([^"]*)"""")
        quotedRegex.findAll(attrPart).forEach { match ->
            attrs[match.groupValues[1]] = match.groupValues[2]
        }

        // Parse unquoted attributes: key=value
        val unquotedRegex = Regex("""([a-zA-Z-]+)=([^\s"]+)""")
        unquotedRegex.findAll(attrPart).forEach { match ->
            val key = match.groupValues[1]
            if (key !in attrs) {
                attrs[key] = match.groupValues[2]
            }
        }

        return attrs
    }

    private fun buildEntry(attrs: Map<String, String>): M3uEntry {
        val name = attrs["tvg-name"] ?: attrs["name"] ?: "Unknown"
        val isRadio = name.startsWith("[R]", ignoreCase = true) ||
                attrs["radio"]?.equals("true", ignoreCase = true) == true

        return M3uEntry(
            name = name,
            url = attrs["url"] ?: "",
            tvgId = attrs["tvg-id"] ?: "",
            tvgName = attrs["tvg-name"] ?: name,
            tvgLogo = attrs["tvg-logo"] ?: "",
            groupTitle = attrs["group-title"] ?: "",
            num = attrs["channel-number"]?.toIntOrNull()
                ?: attrs["tvg-chno"]?.toIntOrNull() ?: 0,
            catchup = attrs["catchup"] ?: "",
            catchupSource = attrs["catchup-source"] ?: "",
            catchupDays = attrs["catchup-days"]?.toIntOrNull() ?: 0,
            isRadio = isRadio
        )
    }

    private fun detectEncoding(stream: java.io.InputStream): String {
        val header = ByteArray(3)
        val marked = stream.markSupported()
        if (marked) stream.mark(3)
        stream.read(header)
        if (marked) stream.reset()

        return when {
            header[0] == 0xEF.toByte() && header[1] == 0xBB.toByte() -> "UTF-8"
            header[0] == 0xFE.toByte() && header[1] == 0xFF.toByte() -> "UTF-16BE"
            header[0] == 0xFF.toByte() && header[1] == 0xFE.toByte() -> "UTF-16LE"
            else -> "UTF-8"
        }
    }
}
