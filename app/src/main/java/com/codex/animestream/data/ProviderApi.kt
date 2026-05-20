package com.codex.animestream.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LicensedAnimeProviderApi {
    @GET("v1/home")
    suspend fun home(): HomeDto

    @GET("v1/anime/{id}")
    suspend fun details(@Path("id") id: String): AnimeDetailsDto

    @GET("v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("genre") genre: String?,
        @Query("year") year: Int?,
        @Query("status") status: String?,
        @Query("rating") minimumRating: Double?,
        @Query("language") language: String?,
        @Query("season") season: String?,
    ): List<AnimeDto>

    @GET("v1/episodes/{episodeId}/playback")
    suspend fun playback(@Path("episodeId") episodeId: String): PlaybackDto
}
