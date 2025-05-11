package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gitschool.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("DEPRECATION")
class RandomAnimeActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var animeImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var episodesText: TextView
    private lateinit var yearText: TextView
    private lateinit var ratingText: TextView
    private lateinit var genresText: TextView
    private lateinit var watchButton: Button
    private lateinit var randomizeButton: Button
    private lateinit var progressBar: ProgressBar

    private var currentAnime: Anime? = null
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_anime)

        backArrow = findViewById(R.id.backArrow)
        animeImage = findViewById(R.id.animeImage)
        titleText = findViewById(R.id.animeTitle)
        episodesText = findViewById(R.id.animeEpisodes)
        yearText = findViewById(R.id.animeYear)
        ratingText = findViewById(R.id.animeRating)
        genresText = findViewById(R.id.animeGenres)
        watchButton = findViewById(R.id.watchButton)
        randomizeButton = findViewById(R.id.randomizeButton)
        progressBar = findViewById(R.id.randomProgressBar)

        backArrow.setOnClickListener {
            onBackPressed()
        }

        watchButton.setOnClickListener {
            currentAnime?.malId?.let {
                openDetailActivity(it)
            }
        }

        randomizeButton.setOnClickListener {
            if (!isLoading) {
                loadRandomAnime()
            }
        }

        loadRandomAnime()
    }

    private fun loadRandomAnime() {
        isLoading = true
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            delay(500) // затримка перед запитом
            try {
                val randomPage = (1..100).random()
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getTopAnime(randomPage)
                }

                val animeList = response.data?.filter { it.score != null && it.images?.jpg?.imageUrl != null }
                val anime = animeList?.randomOrNull()

                if (anime != null) {
                    currentAnime = anime
                    showAnime(anime)
                } else {
                    Toast.makeText(this@RandomAnimeActivity, "Не вдалося знайти аніме", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@RandomAnimeActivity, "Помилка при завантаженні", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                isLoading = false
            }
        }
    }

    private fun showAnime(anime: Anime) {
        titleText.text = anime.title ?: "Без назви"
        episodesText.text = "Серій: ${anime.episodes ?: "Невідомо"}"

        val year = anime.aired?.from?.takeIf { it.isNotBlank() }?.let {
            try {
                LocalDate.parse(it, DateTimeFormatter.ISO_DATE_TIME).year.toString()
            } catch (_: Exception) {
                "Невідомо"
            }
        } ?: "Невідомо"
        yearText.text = "Рік: $year"

        ratingText.text = "Рейтинг: ${anime.score?.toString() ?: "—"} ⭐"

        // Підтягуємо жанри з детального запиту
        lifecycleScope.launch {
            try {
                val detailResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAnimeDetails(anime.malId ?: 0)
                }
                val genres = detailResponse.data?.genres?.joinToString(", ") { it.name ?: "" } ?: "—"
                genresText.text = "Жанри: $genres"
            } catch (e: Exception) {
                genresText.text = "Жанри: —"
            }
        }

        Glide.with(this)
            .load(anime.images?.jpg?.imageUrl)
            .into(animeImage)
    }



    private fun openDetailActivity(malId: Int) {
        val intent = Intent(this, DetailAnimeActivity::class.java)
        intent.putExtra("anime_id", malId)
        startActivity(intent)
    }
}
