package com.example.gitschool

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PdfViewerActivity : AppCompatActivity() {
    private var pdfUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ми не використовуємо layout, адже відкриваємо посилання через браузер

        // Отримання URL книги з інтенду
        pdfUrl = intent.getStringExtra("PDF_URL")

        if (pdfUrl != null) {
            // Створюємо інтенд для відкриття URL в браузері
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            startActivity(browserIntent)
        } else {
            Toast.makeText(this, "Помилка: не знайдено URL книги", Toast.LENGTH_SHORT).show()
        }
        // Завершуємо активність після запуску браузера
        finish()
    }
}
