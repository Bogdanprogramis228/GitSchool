package com.example.gitschool

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gitschool.adapters.TopAnimeAdapter
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TopAnimeActivity : AppCompatActivity() {

    private lateinit var topAnimeTitle: TextView
    private lateinit var topAnimeRecyclerView: RecyclerView
    private lateinit var adapter: TopAnimeAdapter
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_anime)

        topAnimeTitle = findViewById(R.id.topAnimeTitle)
        topAnimeRecyclerView = findViewById(R.id.topAnimeRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        // При кліку на заголовок – повернення назад
        topAnimeTitle.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Встановлюємо GridLayoutManager, 3 колонки (при потребі змініть кількість)
        topAnimeRecyclerView.layoutManager = GridLayoutManager(this, 3)

        loadTopAnimeFromApi()
        topAnimeRecyclerView.addItemDecoration(SpacesItemDecoration(16))
    }

    private fun loadTopAnimeFromApi() {
        lifecycleScope.launch {
            loadingProgressBar.visibility = View.VISIBLE // Показати ProgressBar на початку
            topAnimeRecyclerView.visibility = View.GONE // Приховати RecyclerView

            val allAnime = mutableListOf<Anime>()
            for (page in 1..4) {
                try {
                    val response = RetrofitClient.apiService.getTopAnime(page)
                    response.data?.let { allAnime.addAll(it) }
                    delay(1000) // Затримка 1 секунда між запитами
                    // ProgressBar буде автоматично "прокручуватися", якщо він видимий
                } catch (e: HttpException) {
                    if (e.code() == 429) {
                        showToast("Забагато запитів. Спробуй пізніше.")
                        break // Виходимо з циклу, якщо занадто багато запитів
                    } else {
                        showToast("Помилка HTTP: ${e.code()}")
                    }
                }
            }

            loadingProgressBar.visibility = View.GONE // Приховати ProgressBar після завантаження
            topAnimeRecyclerView.visibility = View.VISIBLE // Показати RecyclerView

            if (allAnime.isNotEmpty()) {
                val adapter = TopAnimeAdapter(allAnime) { clickedAnime ->
                    clickedAnime.malId?.let { openDetailActivity(it) }
                }
                topAnimeRecyclerView.adapter = adapter
            }

        }
    }

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.set(space, space, space, space)
        }
    }

    private fun openDetailActivity(animeId: Int) {
        val intent = Intent(this, DetailAnimeActivity::class.java)
        intent.putExtra("anime_id", animeId)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
