package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.CustomSpinnerAdapter
import com.example.gitschool.adapters.SeasonsAnimeAdapter
import com.example.gitschool.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Suppress("DEPRECATION")
class SeasonsActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var yearSpinner: Spinner
    private lateinit var seasonSpinner: Spinner
    private lateinit var seasonsRecyclerView: RecyclerView
    private lateinit var progressBar: View

    private var animeList: MutableList<Anime> = mutableListOf()
    private lateinit var adapter: SeasonsAnimeAdapter

    private val baseYearsArray = arrayOf("2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025")
    private val seasonsArray = arrayOf("winter", "spring", "summer", "fall")
    private lateinit var yearsArray: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seasons)

        backArrow = findViewById(R.id.backArrow)
        yearSpinner = findViewById(R.id.yearSpinner)
        seasonSpinner = findViewById(R.id.seasonSpinner)
        seasonsRecyclerView = findViewById(R.id.seasonsRecyclerView)
        progressBar = findViewById(R.id.seasonsProgressBar)

        seasonsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SeasonsAnimeAdapter(animeList) { anime ->
            anime.malId?.let { openDetailActivity(it) }
        }
        seasonsRecyclerView.adapter = adapter

        // Динамічне оновлення масиву років (у випадку, якщо поточний рік ще не доданий)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearsArray = baseYearsArray.toMutableSet().apply { add(currentYear.toString()) }
            .toMutableList().sorted().toTypedArray()

        val customYearAdapter = CustomSpinnerAdapter(this, yearsArray)
        yearSpinner.adapter = customYearAdapter

        val customSeasonAdapter = CustomSpinnerAdapter(this, seasonsArray)
        seasonSpinner.adapter = customSeasonAdapter


        setInitialSpinnerSelections()

        val spinnerListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadSeasonAnime(getSelectedYear(), getSelectedSeason())
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        yearSpinner.onItemSelectedListener = spinnerListener
        seasonSpinner.onItemSelectedListener = spinnerListener

        backArrow.setOnClickListener { onBackPressed() }

        loadSeasonAnime(getSelectedYear(), getSelectedSeason())
    }

    private fun setInitialSpinnerSelections() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val currentSeason = when (currentMonth) {
            in 0..2 -> "winter"
            in 3..5 -> "spring"
            in 6..8 -> "summer"
            else -> "fall"
        }

        val yearIndex = yearsArray.indexOf(currentYear.toString())
        val seasonIndex = seasonsArray.indexOf(currentSeason)

        if (yearIndex >= 0) yearSpinner.setSelection(yearIndex)
        if (seasonIndex >= 0) seasonSpinner.setSelection(seasonIndex)
    }

    private fun getSelectedYear(): Int {
        return (yearSpinner.selectedItem as? String)?.toIntOrNull() ?: 2023
    }

    private fun getSelectedSeason(): String {
        return seasonSpinner.selectedItem as? String ?: "spring"
    }

    private fun loadSeasonAnime(year: Int, season: String) {
        progressBar.visibility = View.VISIBLE
        seasonsRecyclerView.visibility = View.GONE
        animeList.clear()
        adapter.updateData(emptyList())

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getSeasonAnime(year, season)
                }
                val seasonAnime = response.data ?: emptyList()
                animeList.addAll(seasonAnime)
                adapter.updateData(animeList)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SeasonsActivity, "Помилка завантаження аніме", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                seasonsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun openDetailActivity(malId: Int) {
        val intent = Intent(this, DetailAnimeActivity::class.java)
        intent.putExtra("anime_id", malId)
        startActivity(intent)
    }
}
