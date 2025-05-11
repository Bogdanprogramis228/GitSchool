package com.example.gitschool

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.icu.text.Transliterator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.TopAnimeAdapter
import com.example.gitschool.models.AnimeListResponse
import com.example.gitschool.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class SearchActivity : AppCompatActivity() {

    // 1) Типи: англ → укр
    private val typeMapEnToUa = mapOf(
        "tv"      to "Серіал",
        "movie"   to "Фільм",
        "ova"     to "OVA",
        "ona"     to "ONA",
        "special" to "Спеціальний",
        "music"   to "Музичний"
    )

    // 2) Жанри: укр → mal_id
    private val genresUaToId = listOf(
        "Екшен"            to 1,
        "Пригоди"          to 2,
        "Комедія"          to 4,
        "Демони"           to 6,
        "Детектив"         to 7,
        "Драма"            to 8,
        "Фентезі"          to 10,
        "Гра"              to 11,
        "Жахи"             to 14,
        "Музика"           to 19,
        "Романтика"        to 22,
        "Школа"            to 23,
        "Фантастика"       to 24,
        "Бойові мистецтва" to 17,
        "Військові"        to 38,
        "Гарем"            to 35,
        "Історія"          to 13,
        "Магія"            to 37,
        "Механіка"         to 18,
        "Містика"          to 27,
        "Повсякденність"   to 36,
        "Попаданці"        to 47,
        "Спорт"            to 30,
        "Трилер"           to 40,
        "Вампіри"          to 32
    )

    // UI
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var spinnerType: Spinner
    private lateinit var tvGenre: TextView
    private lateinit var spinnerYear: Spinner
    private lateinit var rvResults: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var rootLayout: ViewGroup

    // Data
    private val selectedGenreIds = mutableListOf<Int>()
    private val animeList = mutableListOf<Anime>()
    private lateinit var adapter: TopAnimeAdapter

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Зв’язуємо UI
        etSearch     = findViewById(R.id.etSearch)
        btnSearch    = findViewById(R.id.btnSearch)
        spinnerType  = findViewById(R.id.spinnerType)
        tvGenre      = findViewById(R.id.tvGenre)
        spinnerYear  = findViewById(R.id.spinnerYear)
        rvResults    = findViewById(R.id.rvResults)
        tvEmpty      = findViewById(R.id.tvEmpty)
        rootLayout   = findViewById(R.id.rootLayout)
        findViewById<TextView>(R.id.toolbar).setOnClickListener { finish() }

        // Spinner для Типів
        val typesUa = listOf("Усі типи") + typeMapEnToUa.values
        spinnerType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, typesUa
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Spinner для Років (2000 … поточний)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2000..currentYear).map(Int::toString).reversed()
        spinnerYear.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Всі роки") + years
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Мультивибір Жанрів через AlertDialog
        tvGenre.setOnClickListener {
            val namesUa = genresUaToId.map { it.first }.toTypedArray()
            val checked = BooleanArray(namesUa.size) { i ->
                selectedGenreIds.contains(genresUaToId[i].second)
            }

            AlertDialog.Builder(this)
                .setTitle("Оберіть жанри")
                .setMultiChoiceItems(namesUa, checked) { _, idx, isChecked ->
                    val id = genresUaToId[idx].second
                    if (isChecked) selectedGenreIds.add(id)
                    else selectedGenreIds.remove(id)
                }
                .setPositiveButton("Гаразд") { _, _ ->
                    tvGenre.text = if (selectedGenreIds.isEmpty()) "Жанр"
                    else genresUaToId
                        .filter { selectedGenreIds.contains(it.second) }
                        .joinToString { it.first }
                }
                .setNegativeButton("Скасувати", null)
                .show()
        }

        // RecyclerView + Adapter з обробником кліку
        adapter = TopAnimeAdapter(animeList) { anime ->
            openDetailActivity(anime.malId!!)
        }
        rvResults.layoutManager = GridLayoutManager(this, 3)
        rvResults.adapter = adapter

        // Пошук по кліку
        btnSearch.setOnClickListener {
            hideKeyboard()
            etSearch.clearFocus()
            doSearch()
        }

        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                etSearch.clearFocus()
            }
            false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun doSearch() {
        val rawQuery = etSearch.text.toString().trim()
        val isCyrillic = Regex("\\p{IsCyrillic}").containsMatchIn(rawQuery)

        // Визначаємо фінальний запит до API
        lifecycleScope.launch(Dispatchers.Main) {
            val queryToSearch = if (isCyrillic) {
                // спроба перекласти
                val translated = translateUaToEn(rawQuery)
                // якщо переклад повернув те ж саме або без латиниці — фолбек на транслитерацію
                if (translated == rawQuery || !Regex("[A-Za-z]").containsMatchIn(translated)) {
                    uaToLatin(rawQuery)
                } else {
                    translated
                }
            } else rawQuery

            try {
                val resp: AnimeListResponse = RetrofitClient.apiService.searchAnime(
                    q      = queryToSearch,
                    type   = (spinnerType.selectedItem as String).let { ua ->
                        typeMapEnToUa.entries.find { it.value == ua }?.key
                    },
                    genres = selectedGenreIds.takeIf { it.isNotEmpty() }?.joinToString(","),
                    year   = (spinnerYear.selectedItem as String)
                        .takeIf { it != "Всі роки" }
                        ?.toIntOrNull()
                )
                val data = resp.data ?: emptyList()

                // Не фільтруємо повторно — API вже повернув релевантні результати
                animeList.clear()
                animeList.addAll(data)
                adapter.notifyDataSetChanged()

                tvEmpty.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                tvEmpty.text = if (data.isEmpty()) "Нічого не знайдено за запитом" else ""

            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity,
                    "Помилка мережі або API", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun uaToLatin(text: String): String {
        val tr = Transliterator.getInstance("Cyrillic-Latin")
        return tr.transliterate(text)
    }

    private suspend fun translateUaToEn(text: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://libretranslate.com/translate")
                val postData = "q=${URLEncoder.encode(text, "UTF-8")}&source=uk&target=en"

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                conn.outputStream.use { it.write(postData.toByteArray()) }

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                json.getString("translatedText")
            } catch (e: Exception) {
                text // якщо помилка, повертаємо як є
            }
        }
    }


    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun openDetailActivity(animeId: Int) {
        val intent = Intent(this, DetailAnimeActivity::class.java)
        intent.putExtra("anime_id", animeId)
        startActivity(intent)
    }
}
