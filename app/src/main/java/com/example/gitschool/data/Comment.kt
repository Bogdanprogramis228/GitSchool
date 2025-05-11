package com.example.gitschool.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Comment(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("userName") @set:PropertyName("userName") var userName: String? = null,
    @get:PropertyName("userAvatarUrl") @set:PropertyName("userAvatarUrl") var userAvatarUrl: String? = null,
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Timestamp? = null,
    @get:PropertyName("animeId") @set:PropertyName("animeId") var animeId: Int = -1,
    @get:PropertyName("animeTitle") var animeTitle: String? = null,
    @get:PropertyName("likeCount") @set:PropertyName("likeCount") var likeCount: Long = 0,
    @get:PropertyName("dislikeCount") @set:PropertyName("dislikeCount") var dislikeCount: Long = 0,

    @get:Exclude var documentId: String? = null
)


data class Comments(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("userName") @set:PropertyName("userName") var userName: String? = null,
    @get:PropertyName("userAvatarUrl") @set:PropertyName("userAvatarUrl") var userAvatarUrl: String? = null,
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Timestamp? = null,
    @get:PropertyName("animeId") @set:PropertyName("animeId") var animeId: Int = -1,
    @get:PropertyName("animeTitle") var animeTitle: String? = null,
    @get:PropertyName("likeCount") @set:PropertyName("likeCount") var likeCount: Long = 0,
    @get:PropertyName("dislikeCount") @set:PropertyName("dislikeCount") var dislikeCount: Long = 0,

    @get:Exclude var documentId: String? = null
)
