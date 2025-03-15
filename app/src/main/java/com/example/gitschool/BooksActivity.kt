package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.BooksAdapter
import com.example.gitschool.data.BookItem

@Suppress("DEPRECATION")
class BooksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)

        val className = intent.getStringExtra("class_name") ?: "Невідомий клас"

        // Замість supportActionBar, ми працюємо з TextView
        val toolbar: TextView = findViewById(R.id.toolbar)
        toolbar.text = "Книги для $className"

        // Додати обробник натискання на стрілку
        toolbar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.strilka, 0, 0, 0)
        toolbar.setOnClickListener {
            finish() // Закриває поточну активність та повертає на попередню сторінку
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Отримуємо список книг для класу
        val books = intent.getParcelableArrayListExtra<BookItem>("books_list") ?: emptyList()

        val adapter = BooksAdapter(books) { pdfUrl ->
            val intent = Intent(this, PdfViewerActivity::class.java)
            intent.putExtra("pdfUrl", pdfUrl)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }
}

