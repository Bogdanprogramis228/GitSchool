package com.example.gitschool.data

import org.jsoup.Jsoup

object PhotoParser {
    private const val BASE_URL = "https://school-yablounic.e-schools.info"

    fun getPhotoGalleryTitle(): String {
        val doc = Jsoup.connect(BASE_URL).get()
        return doc.select(".grid_ttl_mr2 h2 a").text()
    }

    fun getPhotoList(): List<String> {
        val doc = Jsoup.connect(BASE_URL).get()
        return doc.select(".index_ph_box .index_ph_list_wrap #index_ph_list .ph a img")
            .map { it.absUrl("src") }
    }
}