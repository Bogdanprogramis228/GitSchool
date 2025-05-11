package com.example.gitschool

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.BookmarkAnimeAdapter
import com.example.gitschool.adapters.CustomSpinnerAdapter
import com.example.gitschool.data.AnimeStatus
import com.example.gitschool.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import toAnime


class BookmarksActivity : AppCompatActivity(), BookmarkAnimeAdapter.OnItemClickListener {

    private lateinit var bookmarksTitleTextView: TextView
    private lateinit var statusSpinner: Spinner
    private lateinit var bookmarksRecyclerView: RecyclerView
    private lateinit var adapter: BookmarkAnimeAdapter
    private var bookmarkedAnimeList: MutableList<Anime> = mutableListOf()
    private var currentStatus: String = AnimeStatus.WATCHING
    private lateinit var loadingProgressBar: ProgressBar

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_bookmarks)


        bookmarksTitleTextView = findViewById(R.id.bookmarksTitle)
        statusSpinner = findViewById(R.id.statusSpinner)
        bookmarksRecyclerView = findViewById(R.id.bookmarksRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        // Налаштування RecyclerView як сітка з 3 стовпцями
        bookmarksRecyclerView.layoutManager = GridLayoutManager(this, 3)

        // Натискання на заголовок – повернення назад
        bookmarksTitleTextView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val statusesArray = arrayOf(
            AnimeStatus.WATCHING,
            AnimeStatus.PLAN_TO_WATCH,
            AnimeStatus.COMPLETED,
            AnimeStatus.DROPPED,
            AnimeStatus.ON_HOLD,
            AnimeStatus.FAVORITE
        )
        currentStatus = AnimeStatus.WATCHING

        // Ініціалізація адаптера і прив’язування до RecyclerView
        adapter = BookmarkAnimeAdapter( bookmarkedAnimeList, this)
        bookmarksRecyclerView.adapter = adapter


        // Завантаження закладок з початковим статусом
        loadBookmarkedAnime(currentStatus)

        // Налаштування Spinner з масивом статусів
        val statusAdapter = CustomSpinnerAdapter(this, statusesArray)
        statusSpinner.adapter = statusAdapter

        // Встановлення початкового вибору Spinner
        val initialPosition = statusesArray.indexOf(AnimeStatus.WATCHING)
        if (initialPosition != -1) {
            statusSpinner.setSelection(initialPosition)
        }

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedStatus = statusesArray[position]
                if (currentStatus != selectedStatus) {
                    currentStatus = selectedStatus
                    loadBookmarkedAnime(currentStatus)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Нічого не робимо
            }
        }
    }

    // Функція завантаження закладок за статусом із Firestore
    @SuppressLint("NotifyDataSetChanged")
    private fun loadBookmarkedAnime(status: String) {
        loadingProgressBar.visibility = View.VISIBLE
        bookmarksRecyclerView.visibility = View.GONE

        // Очищуємо поточний список і оновлюємо UI, щоб прибрати старі дані
        bookmarkedAnimeList.clear()
        adapter.notifyDataSetChanged()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            loadingProgressBar.visibility = View.GONE
            bookmarksRecyclerView.visibility = View.VISIBLE
            Toast.makeText(this, "Користувач не авторизований", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Отримуємо дані з Firestore
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("animeStatuses")
                    .whereEqualTo("status", status)
                    .get()
                    .await()

                // Використовуємо тимчасовий список для зберігання завантажених елементів
                val tempList = mutableListOf<Anime>()

                        for (doc in snapshot.documents) {
                            val malIdFromFirestore = doc.getLong("malId")?.toInt()
                            Log.d("BookmarksActivity", "Документ: ${doc.id} має malId: $malIdFromFirestore")
                            malIdFromFirestore?.let { malId ->
                                try {
                                    delay(500L)
                                    val response = withContext(Dispatchers.IO) {
                                        Log.d("BookmarksActivity", "Запит до API для malId: $malId")
                                        RetrofitClient.apiService.getAnimeDetails(malId)
                                    }
                                    response.data?.let { animeDetail ->
                                        val anime = animeDetail.toAnime()
                                        Log.d("BookmarksActivity", "Успішно отримано дані з API для malId: ${anime.malId}")
                                        tempList.add(anime)
                                    }
                                } catch (e: Exception) {
                                    Log.e("BookmarksActivity", "Помилка при отриманні аніме з API: $malId", e)
                                }
                            } ?: Log.w("BookmarksActivity", "Пропущено документ ${doc.id} без malId")
                        }


                // Оновлюємо основний список за один раз
                bookmarkedAnimeList.clear()
                bookmarkedAnimeList.addAll(tempList)
                adapter.notifyDataSetChanged()
                Log.d("BookmarksActivity", "Завантажено ${tempList.size} аніме зі статусом $status")

            } catch (e: Exception) {
                Log.e("BookmarksActivity", "Помилка завантаження закладок", e)
                Toast.makeText(this@BookmarksActivity, "Помилка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                loadingProgressBar.visibility = View.GONE
                bookmarksRecyclerView.visibility = View.VISIBLE
            }
        }
    }


    override fun onItemClick(anime: Anime) {
        val intent = Intent(this, DetailAnimeActivity::class.java)
        intent.putExtra("anime_id", anime.malId)
        startActivity(intent)
    }
}