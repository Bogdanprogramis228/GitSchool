package com.example.gitschool.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Notification(
    // ID самого документа сповіщення
    @get:Exclude var documentId: String? = null,

    @get:PropertyName("type") @set:PropertyName("type") var type: String = "system", // "system" або "anime_update"
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("message") @set:PropertyName("message") var message: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Timestamp? = null,
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,

    // Поля для типу "anime_update"
    @get:PropertyName("animeId") @set:PropertyName("animeId") var animeId: Int? = null,
    @get:PropertyName("animeTitle") @set:PropertyName("animeTitle") var animeTitle: String? = null,
    @get:PropertyName("animeImageUrl") @set:PropertyName("animeImageUrl") var animeImageUrl: String? = null
) {
    constructor() : this(null,"system", "", "", null, false, null, null, null) // Порожній конструктор для Firestore
}