package com.example.gitschool.data

import com.example.gitschool.models.NewsItem
import org.jsoup.Jsoup

object NewsParser {
    fun getNews(callback: (List<NewsItem>) -> Unit) {
        Thread {
            try {
                val doc = Jsoup.connect("https://school-yablounic.e-schools.info/news").get()

                // Знаходимо всі блоки новин
                val newsElements = doc.select("div.sch_news_item")
                val newsList = mutableListOf<NewsItem>()

                for (element in newsElements) {
                    // Заголовок новини
                    val title = element.select("div.title a").text()

                    // Посилання на повну новину
                    val link = element.select("div.title a").attr("href")

                    // Опис/текст (всередині div.text, якщо є)
                    val description = element.select("div.text").text()

                    // Додаємо в список
                    newsList.add(
                        NewsItem(
                            title = title,
                            description = description,
                            link = link
                        )
                    )
                }

                callback(newsList)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList()) // у разі помилки повертаємо пустий список
            }
        }.start()
    }
    fun getNewsDetails(fullLink: String, callback: (String) -> Unit) {
        Thread {
            try {
                // Наприклад, якщо link="/news/418354", ми маємо дописати "https://school-yablounic.e-schools.info" + link
                val doc = Jsoup.connect(fullLink).get()

                // Знаходимо HTML-елемент, де зберігається детальний опис
                val detailText = doc.select("div.news_box_text").text()

                callback(detailText)
            } catch (e: Exception) {
                e.printStackTrace()
                callback("Не вдалося завантажити опис новини.")
            }
        }.start()
    }

}
