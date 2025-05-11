package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.GenreAdapter

class CatalogActivity : AppCompatActivity() {

    private lateinit var toolbar: TextView
    private lateinit var genreRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        genreRecyclerView = findViewById(R.id.genreRecyclerView)
        genreRecyclerView.layoutManager = GridLayoutManager(this, 3)

        val genreList = listOf(
            "Бойові мистецтва" to R.drawable.martial_arts_placeholder,
            "Вампіри" to R.drawable.vampires_placeholder,
            "Військові" to R.drawable.military_placeholder,
            "Гарем" to R.drawable.harem_placeholder,
            "Детектив" to R.drawable.detective_placeholder,
            "Драма" to R.drawable.drama_placeholder,
            "Гра" to R.drawable.game_placeholder,
            "Історія" to R.drawable.history_placeholder,
            "Комедія" to R.drawable.komedy_placeholder,
            "Магія" to R.drawable.magic_placeholder,
            "Механіка" to R.drawable.mechanics_placeholder,
            "Містика" to R.drawable.mysticism_placeholder,
            "Музика" to R.drawable.music_placeholder,
            "Буденність" to R.drawable.everyday_life_placeholder,
            "Попаданці" to R.drawable.travellers_placeholder,
            "Пригоди" to R.drawable.adventures_placeholder,
            "Демони" to R.drawable.demons_placeholder,
            "Романтика" to R.drawable.romance_placeholder,
            "Спорт" to R.drawable.sport_placeholder,
            "Трилер" to R.drawable.thriller_placeholder,
            "Жахи" to R.drawable.horrors_placeholder,
            "Фантастика" to R.drawable.science_fiction_placeholder,
            "Фентезі" to R.drawable.fantasy_placeholder,
            "Школа" to R.drawable.school_placeholder,
            "Екшен" to R.drawable.action_placeholder
        )

        val adapter = GenreAdapter(genreList) { selectedGenre ->
            val intent = Intent(this, AnimeListByGenreActivity::class.java)
            intent.putExtra("genreName", selectedGenre)
            startActivity(intent)
        }

        genreRecyclerView.adapter = adapter

        toolbar = findViewById(R.id.catalogTitle)
        toolbar.setOnClickListener {
            finish()
        }
    }
}