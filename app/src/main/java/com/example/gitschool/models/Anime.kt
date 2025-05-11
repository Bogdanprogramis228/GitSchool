package com.example.gitschool


import com.google.gson.annotations.SerializedName

data class Anime(
    @SerializedName("mal_id") val malId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("images") val images: Images?,
    @SerializedName("score") val score: Double?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("aired") val aired: Aired?,
    @SerializedName("synopsis") val synopsis: String?
)

data class Aired(
    @SerializedName("from") val from: String?,
    @SerializedName("to") val to: String?
)

data class Images(
    @SerializedName("jpg") val jpg: Jpg?
)

data class Jpg(
    @SerializedName("image_url") val imageUrl: String?
)

data class Producer(
    val name: String?
)



