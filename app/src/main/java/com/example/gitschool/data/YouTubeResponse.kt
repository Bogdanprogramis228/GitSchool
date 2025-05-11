package com.example.gitschool.data

import com.google.gson.annotations.SerializedName

data class YouTubeResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: YouTubeVideoId,
    val snippet: YouTubeVideoSnippet
)

data class YouTubeVideoId(
    val kind: String,
    @SerializedName("videoId")
    val videoId: String
)

data class YouTubeVideoSnippet(
    val title: String,
    val description: String,
    @SerializedName("thumbnails")
    val thumbnails: YouTubeThumbnail
)

data class YouTubeThumbnail(
    val default: ThumbnailDetails?,
    val medium: ThumbnailDetails?,
    val high: ThumbnailDetails?
)

data class ThumbnailDetails(
    val url: String,
    val width: Int?,
    val height: Int?
)
