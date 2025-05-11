package com.example.gitschool

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.adapters.AnimeAdapter
import com.example.gitschool.data.AnimeStatus
import com.example.gitschool.data.Notification
import com.example.gitschool.network.RetrofitClient
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@Suppress("DEPRECATION")
class SecondActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // RecyclerView для трьох списків аніме
    private lateinit var horizontalRecyclerView1: RecyclerView  // "Анонси"
    private lateinit var horizontalRecyclerView2: RecyclerView  // "Популярне"
    private lateinit var horizontalRecyclerView3: RecyclerView  // "Рекомендації"

    private lateinit var btnProfile: TextView
    private lateinit var userNameTextView: TextView

    // Заголовки секцій
    private lateinit var titleAnnouncements: TextView  // "Анонси"
    private lateinit var titleEvents: TextView         // "Популярне"
    private lateinit var titleCatalog: TextView        // "Рекомендації"

    private lateinit var db: FirebaseFirestore

    private lateinit var navAvatarImage: ImageView
    private lateinit var navBackgroundImage: ImageView

    private val database = FirebaseDatabase.getInstance("https://gitschool-9eede-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Ініціалізація Firestore та основних елементів
        db = FirebaseFirestore.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        horizontalRecyclerView1 = findViewById(R.id.recyclerView)
        horizontalRecyclerView2 = findViewById(R.id.recyclerView2)
        horizontalRecyclerView3 = findViewById(R.id.recyclerView3)

        btnProfile = findViewById(R.id.btn_profile)
        titleAnnouncements = findViewById(R.id.title_announcements)
        titleEvents = findViewById(R.id.title_events)
        titleCatalog = findViewById(R.id.title_catalog)

        // Отримання header NavigationView та його елементів
        val headerView = navigationView.getHeaderView(0)
        userNameTextView = headerView.findViewById(R.id.user_name)
        navAvatarImage = headerView.findViewById(R.id.user_avatar)       // Повинно відповідати вашому XML header (ex: nav_header.xml)
        navBackgroundImage = headerView.findViewById(R.id.user_background) // Повинно відповідати вашому XML header

        loadUserName()
        loadHeaderImages()  // Завантажуємо зображення хедера при створенні

        // Налаштування Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.toolbar_menu)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Клік на кнопку "Профіль"
        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Пошук (btn_schedule)
        val btnSchedule = findViewById<TextView>(R.id.btn_schedule)
        btnSchedule.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Закладки (btn_bookmarks)
        val btnBookmarks = findViewById<TextView>(R.id.btn_bookmarks)
        btnBookmarks.setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }

        // Сезони (btn_seasons) – перехід до SeasonsActivity
        val btnSeasons: TextView = findViewById(R.id.btn_seasons)
        btnSeasons.setOnClickListener {
            startActivity(Intent(this, SeasonsActivity::class.java))
        }

        // Іконка налаштувань (Toolbar)
        val settingsIcon: ImageButton = findViewById(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Налаштування горизонтальних RecyclerView
        horizontalRecyclerView1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalRecyclerView3.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Встановлення заголовків секцій
        titleAnnouncements.text = "Анонси"
        titleEvents.text = "Популярне"
        titleCatalog.text = "Рекомендації"

        // Завантаження даних із API
        loadAnnouncements()
        loadPopular()
        loadRecommendations()

        // Обробка кліків у NavigationView
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> showToast("Головна")
                R.id.nav_events -> startActivity(Intent(this, TopAnimeActivity::class.java))
                R.id.nav_news -> startActivity(Intent(this, CatalogActivity::class.java))
                R.id.nav_library -> startActivity(Intent(this, RandomAnimeActivity::class.java))
                R.id.nav_cafeteria -> startActivity(Intent(this, HistoryActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_discord -> {
                    val url = "https://discord.gg/prAfgxBYHZ"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                R.id.nav_contact -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserName()
        loadHeaderImages()           // Оновлюємо фото аватара та фону при поверненні
        loadRecommendations()        // Оновлюємо рекомендації
    }

    private fun loadUserName() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: user.displayName ?: "Користувач"
                    userNameTextView.text = name
                }
                .addOnFailureListener {
                    userNameTextView.text = user.displayName ?: "Користувач"
                }
        }
    }

    // Метод для завантаження зображень хедера (аватара та фону)
    private fun loadHeaderImages() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val dbRef = FirebaseDatabase.getInstance("https://gitschool-9eede-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Завантаження аватара
        dbRef.child("users").child(uid).child("avatarBase64").get()
            .addOnSuccessListener { snapshot ->
                val avatarBase64 = snapshot.getValue(String::class.java)
                if (!avatarBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(this)
                            .load(bitmap)
                            .placeholder(R.drawable.ava_01)
                            .circleCrop()
                            .into(navAvatarImage)
                    } catch (e: Exception) {
                        Log.e("LoadHeaderImages", "Error decoding avatarBase64", e)
                        navAvatarImage.setImageResource(R.drawable.ava_01)
                    }
                } else {
                    // Якщо Base64 відсутній, намагаємося завантажити за resourceId
                    dbRef.child("users").child(uid).child("avatarResourceId").get()
                        .addOnSuccessListener { resSnapshot ->
                            val resId = resSnapshot.getValue(Int::class.java)
                            if (resId != null) {
                                Glide.with(this)
                                    .load(resId)
                                    .placeholder(R.drawable.ava_01)
                                    .circleCrop()
                                    .into(navAvatarImage)
                            } else {
                                navAvatarImage.setImageResource(R.drawable.ava_01)
                            }
                        }
                        .addOnFailureListener {
                            navAvatarImage.setImageResource(R.drawable.ava_01)
                        }
                }
            }
            .addOnFailureListener {
                navAvatarImage.setImageResource(R.drawable.ava_01)
            }

        // Завантаження фону
        dbRef.child("users").child(uid).child("backgroundBase64").get()
            .addOnSuccessListener { snapshot ->
                val backgroundBase64 = snapshot.getValue(String::class.java)
                if (!backgroundBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(backgroundBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(this)
                            .load(bitmap)
                            .placeholder(R.drawable.back_01)
                            .centerCrop()
                            .into(navBackgroundImage)
                    } catch (e: Exception) {
                        Log.e("LoadHeaderImages", "Error decoding backgroundBase64", e)
                        navBackgroundImage.setImageResource(R.drawable.back_01)
                    }
                } else {
                    dbRef.child("users").child(uid).child("backgroundResourceId").get()
                        .addOnSuccessListener { resSnapshot ->
                            val resId = resSnapshot.getValue(Int::class.java)
                            if (resId != null) {
                                Glide.with(this)
                                    .load(resId)
                                    .placeholder(R.drawable.back_01)
                                    .centerCrop()
                                    .into(navBackgroundImage)
                            } else {
                                navBackgroundImage.setImageResource(R.drawable.back_01)
                            }
                        }
                        .addOnFailureListener {
                            navBackgroundImage.setImageResource(R.drawable.back_01)
                        }
                }
            }
            .addOnFailureListener {
                navBackgroundImage.setImageResource(R.drawable.back_01)
            }
    }

    private fun loadAnnouncements() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUpcomingAnime()
                val animeList = response.data
                if (!animeList.isNullOrEmpty()) {
                    val adapter = AnimeAdapter(animeList) { clickedAnime ->
                        clickedAnime.malId?.let { openDetailActivity(it) }
                    }
                    horizontalRecyclerView1.adapter = adapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Помилка завантаження анонсів")
            }
        }
    }

    private fun loadPopular() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getTopAnime(page = 1)
                val animeList = response.data
                if (!animeList.isNullOrEmpty()) {
                    val adapter = AnimeAdapter(animeList) { clickedAnime ->
                        clickedAnime.malId?.let { openDetailActivity(it) }
                    }
                    horizontalRecyclerView2.adapter = adapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Помилка завантаження популярного аніме")
            }
        }
    }

    private fun loadRecommendations() {
        val recommendationsTitle = findViewById<TextView>(R.id.title_catalog)
        val recommendationsRecycler = findViewById<RecyclerView>(R.id.recyclerView3)

        // Ховаємо секцію під час завантаження
        recommendationsTitle.visibility = View.GONE
        recommendationsRecycler.visibility = View.GONE

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.d("LoadRecs", "User not logged in. Cannot load personalized recommendations.")
            recommendationsTitle.visibility = View.GONE
            recommendationsRecycler.visibility = View.GONE
            return
        }

        Log.d("LoadRecs", "Loading recommendations for user: $userId based on FAVORITES")

        db.collection("users").document(userId).collection("animeStatuses")
            .whereEqualTo("status", AnimeStatus.FAVORITE)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val favoriteAnimeGenres = mutableMapOf<Int, Int>()
                val favoriteAnimeIds = mutableSetOf<Int>()

                if (querySnapshot.isEmpty) {
                    Log.d("LoadRecs", "User has no 'Favorite' anime. Hiding recommendations.")
                    return@addOnSuccessListener
                }

                querySnapshot.documents.forEach { doc ->
                    val animeId = doc.getLong("malId")?.toInt()
                    if (animeId != null) {
                        favoriteAnimeIds.add(animeId)
                        val genresListMap = doc.get("genres") as? List<Map<String, Any>>
                        genresListMap?.forEach { genreMap ->
                            val genreId = (genreMap["malId"] as? Number)?.toInt()
                            if (genreId != null) {
                                favoriteAnimeGenres[genreId] = favoriteAnimeGenres.getOrDefault(genreId, 0) + 1
                            }
                        }
                    }
                }

                if (favoriteAnimeGenres.isEmpty()) {
                    Log.d("LoadRecs", "No genres found in favorite anime. Hiding recommendations.")
                    return@addOnSuccessListener
                }

                val topGenreIds = favoriteAnimeGenres.entries
                    .sortedByDescending { it.value }
                    .take(3)
                    .map { it.key }

                if (topGenreIds.isEmpty()) {
                    Log.d("LoadRecs", "Could not determine top genres from favorites.")
                    return@addOnSuccessListener
                }

                Log.d("LoadRecs", "Top favorite genre IDs: $topGenreIds")

                lifecycleScope.launch {
                    try {
                        val genreQueryParam = topGenreIds.joinToString(",")
                        Log.d("LoadRecs", "Searching API with favorite genres: $genreQueryParam")

                        val searchResponse = RetrofitClient.apiService.searchAnimeByGenres(
                            genres = genreQueryParam,
                            limit = 25
                        )

                        var recommendedAnime = searchResponse.data ?: emptyList()
                        recommendedAnime = recommendedAnime.filterNot { anime ->
                            anime.malId != null && favoriteAnimeIds.contains(anime.malId)
                        }

                        Log.d("LoadRecs", "Filtered recommendations count: ${recommendedAnime.size}")

                        if (recommendedAnime.isNotEmpty()) {
                            val adapter = AnimeAdapter(recommendedAnime) { clickedAnime ->
                                clickedAnime.malId?.let { openDetailActivity(it) }
                            }
                            recommendationsRecycler.adapter = adapter
                            recommendationsTitle.visibility = View.VISIBLE
                            recommendationsRecycler.visibility = View.VISIBLE
                        } else {
                            Log.d("LoadRecs", "No suitable recommendations found after filtering favorites.")
                            recommendationsTitle.visibility = View.GONE
                            recommendationsRecycler.visibility = View.GONE
                        }

                    } catch (e: Exception) {
                        Log.e("LoadRecs", "Error fetching recommendations from API", e)
                        if (e is HttpException) {
                            try {
                                val errorBody = e.response()?.errorBody()?.string()
                                Log.e("LoadRecs", "HTTP Error Body: $errorBody")
                            } catch (ioe: java.io.IOException) {
                                Log.e("LoadRecs", "Failed to read error body", ioe)
                            }
                        }
                        recommendationsTitle.visibility = View.GONE
                        recommendationsRecycler.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoadRecs", "Error fetching favorite list from Firestore", e)
                recommendationsTitle.visibility = View.GONE
                recommendationsRecycler.visibility = View.GONE
            }
    }

    private fun checkNewEpisodes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("EpisodeCheck", "Starting check for user $userId")

        db.collection("users").document(userId).collection("animeStatuses")
            .whereEqualTo("status", AnimeStatus.WATCHING) // Шукаємо ті, що "Дивлюся"
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("EpisodeCheck", "No 'Watching' anime found.")
                    return@addOnSuccessListener
                }

                Log.d("EpisodeCheck", "Found ${snapshot.size()} anime in 'Watching' list.")
                for (doc in snapshot.documents) {
                    val animeId = doc.id.toIntOrNull()
                    val lastCheckedCount = doc.getLong("lastCheckedEpisodeCount") // Читаємо попередню кількість
                    val animeTitleFromStatus = doc.getString("animeTitle") // Добре б зберігати назву тут
                    val animeImageUrlFromStatus = doc.getString("animeImageUrl") // і URL постера

                    if (animeId == null || lastCheckedCount == null) {
                        Log.w("EpisodeCheck", "Skipping doc ${doc.id}, missing animeId or lastCheckedCount")
                        continue
                    }

                    lifecycleScope.launch {
                        try {
                            Log.d("EpisodeCheck", "Checking details for anime $animeId")
                            val detailResponse = RetrofitClient.apiService.getAnimeDetails(animeId)
                            val currentEpisodeCount = detailResponse.data?.episodes

                            if (currentEpisodeCount != null && currentEpisodeCount > lastCheckedCount) {
                                Log.i("EpisodeCheck", "NEW EPISODE(S) DETECTED for $animeId! Current: $currentEpisodeCount, LastKnown: $lastCheckedCount")

                                val newEpisodes = currentEpisodeCount - lastCheckedCount
                                val messageText = if (newEpisodes.toInt() == 1) {
                                    "${animeTitleFromStatus ?: "Аніме"}: вийшла нова ${currentEpisodeCount} серія!"
                                } else {
                                    "${animeTitleFromStatus ?: "Аніме"}: вийшло ${newEpisodes} нових серій (до ${currentEpisodeCount})!"
                                }

                                val notification = Notification(
                                    type = "anime_update",
                                    title = "Нова серія!",
                                    message = messageText,
                                    timestamp = com.google.firebase.Timestamp.now(),
                                    isRead = false,
                                    animeId = animeId,
                                    animeTitle = animeTitleFromStatus,
                                    animeImageUrl = animeImageUrlFromStatus
                                )
                                // Зберігаємо сповіщення в Firestore
                                saveNotificationToFirestore(userId, notification)

                                // Оновлюємо лічильник перевірених серій в статусі аніме
                                updateLastCheckedCountInFirestore(userId, animeId, currentEpisodeCount.toLong())

                            } else {
                                Log.d("EpisodeCheck", "No new episodes for $animeId (Current: $currentEpisodeCount, LastKnown: $lastCheckedCount)")
                            }
                        } catch (e: Exception) {
                            Log.e("EpisodeCheck", "Error checking details for anime $animeId", e)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EpisodeCheck", "Error loading 'Watching' list", e)
            }
    }

    // Допоміжна функція збереження сповіщення
    private fun saveNotificationToFirestore(userId: String, notification: Notification) {
        db.collection("users").document(userId).collection("notifications")
            .add(notification) // Додаємо з авто-ID
            .addOnSuccessListener { Log.d("EpisodeCheck", "Notification saved for user $userId, anime ${notification.animeId}") }
            .addOnFailureListener { e -> Log.e("EpisodeCheck", "Error saving notification", e) }
    }

    // Допоміжна функція оновлення лічильника
    private fun updateLastCheckedCountInFirestore(userId: String, animeId: Int, newCount: Long) {
        db.collection("users").document(userId).collection("animeStatuses")
            .document(animeId.toString())
            .update("lastCheckedEpisodeCount", newCount)
            .addOnSuccessListener { Log.d("EpisodeCheck", "Updated lastCheckedEpisodeCount for $animeId to $newCount") }
            .addOnFailureListener { e -> Log.e("EpisodeCheck", "Error updating lastCheckedEpisodeCount for $animeId", e) }
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
