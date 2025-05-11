package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.AnimeListByGenreAdapter
import com.example.gitschool.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AnimeListByGenreActivity : AppCompatActivity() {

    private lateinit var genreTitleTextView: TextView
    private lateinit var animeByGenreRecyclerView: RecyclerView
    private lateinit var adapter: AnimeListByGenreAdapter
    private lateinit var loadingProgressBarGenre: ProgressBar
    private var genreId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_list_by_genre)

        genreTitleTextView = findViewById(R.id.genreTitleTextView)
        animeByGenreRecyclerView = findViewById(R.id.animeByGenreRecyclerView)
        loadingProgressBarGenre = findViewById(R.id.loadingProgressBarGenre)

        animeByGenreRecyclerView.layoutManager = LinearLayoutManager(this)

        val genreName = intent.getStringExtra("genreName")
        genreTitleTextView.text = genreName

        genreTitleTextView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        genreId = getGenreIdByName(genreName)
        genreId?.let { loadAnimeByGenreFromApi(it) } // Викликаємо завантаження після отримання ID
    }

    private fun getGenreIdByName(genreName: String?): Int? {
        return when (genreName) {
            "Бойові мистецтва" -> 17
            "Вампіри" -> 32
            "Військові" -> 38
            "Гарем" -> 35
            "Детектив" -> 7
            "Драма" -> 8
            "Гра" -> 11
            "Історія" -> 13
            "Комедія" -> 4
            "Магія" -> 37
            "Механіка" -> 18
            "Містика" -> 27
            "Музика" -> 19
            "Повсякденність" -> 36
            "Попаданці" -> 47
            "Пригоди" -> 2
            "Демони" -> 6
            "Романтика" -> 22
            "Спорт" -> 30
            "Трилер" -> 40
            "Жахи" -> 14
            "Фантастика" -> 24
            "Фентезі" -> 10
            "Школа" -> 23
            "Екшен" -> 1
            else -> null
        }
    }

    private fun loadAnimeByGenreFromApi(genreId: Int) {
        lifecycleScope.launch {
            loadingProgressBarGenre.visibility = View.VISIBLE
            animeByGenreRecyclerView.visibility = View.GONE

            try {
                Log.d("AnimeByGenre", "Завантаження аніме за жанром з ID: $genreId")
                val response = RetrofitClient.apiService.searchAnimeByGenres(genres = genreId.toString()) // Використовуємо searchAnimeByGenres
                if (response.data != null && response.data.isNotEmpty()) {
                    adapter = AnimeListByGenreAdapter(response.data) { clickedAnime ->
                        clickedAnime.malId?.let { openDetailActivity(it) }
                    }
                    animeByGenreRecyclerView.adapter = adapter
                } else {
                    showToast("Аніме цього жанру не знайдено")
                }
            } catch (e: HttpException) {
                showToast("Помилка HTTP: ${e.code()}")
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Помилка при завантаженні аніме за жанром")
            } finally {
                loadingProgressBarGenre.visibility = View.GONE
                animeByGenreRecyclerView.visibility = View.VISIBLE
            }
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