package com.streamnator.app.data.network

import com.google.gson.GsonBuilder
import com.streamnator.app.domain.model.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface XtreamApi {
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String
    ): List<XtreamStream>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String
    ): List<XtreamStream>

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): List<XtreamSeries>

    @GET("player_api.php")
    suspend fun getSeriesEpisodes(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Int
    ): XtreamSeriesDetail
}

data class XtreamSeriesDetail(
    val seasons: List<XtreamSeason>? = null,
    val episodes: Map<String, List<XtreamSeriesEpisode>>? = null,
    val info: XtreamSeries? = null
)

data class XtreamSeason(
    val seasonNumber: Int = 0,
    val episodeCount: Int = 0
)

data class EpisodeResponse(
    val episodes: Map<String, List<XtreamSeriesEpisode>>? = null
)

object ApiClient {
    fun createXtreamApi(baseUrl: String): XtreamApi {
        val gson = GsonBuilder().setLenient().create()

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(XtreamApi::class.java)
    }

    fun createOkHttpClient(userAgent: String = "StreamNator/1.0"): OkHttpClient {
        val interceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
    }
}
