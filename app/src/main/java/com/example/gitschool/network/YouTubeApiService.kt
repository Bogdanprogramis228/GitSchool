package com.example.gitschool.network

import com.example.gitschool.data.YouTubeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("search")
    suspend fun searchTrailer(
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 1,
        @Query("q") query: String,
        @Query("key") key: String
    ): YouTubeResponse
}
