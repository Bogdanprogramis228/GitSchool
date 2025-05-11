package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.HistoryAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class ViewHistoryItem(
    val malId: Int = 0,
    val title: String = "",
    val imageUrl: String = "",
    val episodes: Int? = null,
    val year: Int? = null,
    val rating: Double? = null,
    val genres: List<String> = emptyList(),
    val timestamp: Timestamp? = null
)

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    // Використовуємо той самий TextView для заголовка як “Back Button”
    private lateinit var titleTextView: TextView

    private val historyList: MutableList<ViewHistoryItem> = mutableListOf()
    private lateinit var adapter: HistoryAdapter

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Використовуємо макет activity_history.xml (зразок, який ви навели)
        setContentView(R.layout.activity_history)

        // Ініціалізація елементів UI
        titleTextView = findViewById(R.id.genreTitleTextView)
        historyRecyclerView = findViewById(R.id.animeByGenreRecyclerView)
        progressBar = findViewById(R.id.loadingProgressBarGenre)

        // Встановлюємо заголовок сторінки
        titleTextView.text = "Історія переглядів"

        // Додаємо обробку кліку на заголовок: при натисканні повертаємось
        titleTextView.setOnClickListener {
            finish()
        }

        // Налаштовуємо RecyclerView як вертикальний список (1 елемент на рядок)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(historyList) { item ->
            openDetailActivity(item.malId)
        }
        historyRecyclerView.adapter = adapter

        // Завантажуємо історію переглядів
        loadViewHistory()
    }

    private fun loadViewHistory() {
        progressBar.visibility = View.VISIBLE
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .collection("viewHistory")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                historyList.clear()
                snapshot.documents.forEach { doc ->
                    doc.toObject(ViewHistoryItem::class.java)?.let { historyList.add(it) }
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("HistoryActivity", "Error loading view history", e)
                Toast.makeText(this, "Помилка завантаження історії", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }


    private fun openDetailActivity(malId: Int) {
        val intent = Intent(this, DetailAnimeActivity::class.java)
        intent.putExtra("anime_id", malId)
        startActivity(intent)
    }
}
