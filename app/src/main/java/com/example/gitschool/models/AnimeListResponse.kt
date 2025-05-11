package com.example.gitschool.models

import com.example.gitschool.Anime
import com.google.gson.annotations.SerializedName

data class AnimeListResponse(
    @SerializedName("data") val data: List<Anime>?
)