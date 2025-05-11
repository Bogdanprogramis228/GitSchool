package com.example.gitschool.network

import AnimeDetailResponse
import AnimeRecommendationsResponse
import com.example.gitschool.data.AnimeRelationsResponse
import com.example.gitschool.models.AnimeListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("anime")
    suspend fun searchAnime(
        @Query("q")      q: String?  = null,
        @Query("type")   type: String? = null,
        @Query("genres") genres: String? = null,
        @Query("year")   year: Int?    = null
    ): AnimeListResponse

    @GET("seasons/upcoming")
    suspend fun getUpcomingAnime(): AnimeListResponse

    @GET("top/anime")
    suspend fun getTopAnime(@Query("page") page: Int): AnimeListResponse

    @GET("recommendations/anime")
    suspend fun getRecommendations(): AnimeListResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetails(@Path("id") id: Int): AnimeDetailResponse

    @GET("anime/{id}/relations")
    suspend fun getAnimeRelations(@Path("id") animeId: Int): AnimeRelationsResponse

    @GET("anime/{id}/recommendations")
    suspend fun getAnimeRecommendations(@Path("id") id: Int): AnimeRecommendationsResponse

    @GET("anime")
    suspend fun searchAnimeByGenres(
        @Query("genres") genres: String,
        @Query("limit") limit: Int = 20,
        @Query("sfw") sfw: Boolean = true,
        @Query("type") type: String = "tv",
    ): AnimeListResponse

    @GET("seasons/{year}/{season}")
    suspend fun getSeasonAnime(
        @Path("year") year: Int,
        @Path("season") season: String
    ): AnimeListResponse


}