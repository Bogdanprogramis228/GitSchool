package com.example.gitschool.data

import com.google.gson.annotations.SerializedName

data class AnimeRelationsResponse(
    @SerializedName("data") val data: List<RelationGroup>?
)

data class RelationGroup(
    @SerializedName("relation") val relation: String?, // Тип зв'язку: "Prequel", "Sequel" і т.д.
    @SerializedName("entry") val entries: List<RelatedEntry>?
)

data class RelatedEntry(
    @SerializedName("mal_id") val malId: Int?,
    @SerializedName("type") val type: String?, // "anime", "manga"
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

data class ViewingOrderItem(
    val malId: Int,       // ID пов'язаного аніме
    val title: String,    // Назва пов'язаного аніме
    val relation: String  // Тип зв'язку (наприклад, "Приквел")
)