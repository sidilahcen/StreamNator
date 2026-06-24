package com.streamnator.app.util

import com.streamnator.app.domain.model.EpgProgram
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object XmltvParser {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

    fun parse(inputStream: InputStream): List<EpgProgram> {
        val programs = mutableListOf<EpgProgram>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var currentChannelId = ""
        var currentTitle = ""
        var currentDesc = ""
        var currentStart = 0L
        var currentEnd = 0L
        var currentIcon = ""
        var currentCategories = mutableListOf<String>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "channel" -> {
                        currentChannelId = parser.getAttributeValue(null, "id") ?: ""
                    }
                    "programme" -> {
                        currentChannelId = parser.getAttributeValue(null, "channel") ?: ""
                        currentStart = parseTime(parser.getAttributeValue(null, "start"))
                        currentEnd = parseTime(parser.getAttributeValue(null, "stop"))
                        currentTitle = ""
                        currentDesc = ""
                        currentIcon = ""
                        currentCategories.clear()
                    }
                    "title" -> {
                        currentTitle = parser.nextText()
                    }
                    "desc" -> {
                        currentDesc = parser.nextText()
                    }
                    "icon" -> {
                        currentIcon = parser.getAttributeValue(null, "src") ?: ""
                    }
                    "category" -> {
                        currentCategories.add(parser.nextText())
                    }
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "programme" -> {
                        if (currentTitle.isNotEmpty()) {
                            programs.add(
                                EpgProgram(
                                    channelId = currentChannelId,
                                    title = currentTitle,
                                    description = currentDesc,
                                    startTime = currentStart,
                                    endTime = currentEnd,
                                    icon = currentIcon,
                                    categories = currentCategories.toList()
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return programs
    }

    private fun parseTime(timeStr: String?): Long {
        if (timeStr == null) return 0
        return try {
            val clean = timeStr.replace(" +0", "").trim() + " +0000"
            dateFormat.parse(clean)?.time ?: 0
        } catch (e: Exception) {
            try {
                // Try without timezone
                SimpleDateFormat("yyyyMMddHHmmss", Locale.US).parse(timeStr.trim())?.time ?: 0
            } catch (e2: Exception) {
                0
            }
        }
    }
}
