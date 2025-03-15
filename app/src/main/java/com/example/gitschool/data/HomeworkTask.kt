package com.example.gitschool.data

data class HomeworkTask(
    val id: String = "", // додано значення за замовчуванням
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val userId: String = "",
    val status: String = ""
) {
    // Конструктор без параметрів, необхідний для Firebase
    constructor() : this("", "", "", "", "", "")
}
