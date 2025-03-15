package com.example.gitschool

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class JournalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        // Отримуємо переданий клас
        val selectedClass = intent.getStringExtra("selectedClass")

        // Виводимо назву класу (можна замінити на логіку завантаження журналу)
        val classTextView: TextView = findViewById(R.id.classTextView)
        classTextView.text = "Журнал для класу: $selectedClass"

        // Тут можете додати логіку для завантаження журналу з Google Диску чи бази даних
    }
}
