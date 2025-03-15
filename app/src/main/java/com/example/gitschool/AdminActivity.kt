package com.example.gitschool

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Налаштування кнопок
        val buttonAddContent = findViewById<Button>(R.id.buttonAddContent)
        val buttonViewStats = findViewById<Button>(R.id.buttonViewStats)

        buttonAddContent.setOnClickListener {
            Toast.makeText(this, "Add Content clicked!", Toast.LENGTH_SHORT).show()
        }

        buttonViewStats.setOnClickListener {
            Toast.makeText(this, "View Statistics clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
