package com.example.gitschool

import AnimeDetail
import Genre
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Query
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.adapters.AnimeAdapter
import com.example.gitschool.adapters.CommentAdapter
import com.example.gitschool.adapters.ViewingOrderAdapter
import com.example.gitschool.data.AnimeStatus
import com.example.gitschool.data.Comment
import com.example.gitschool.data.ViewingOrderItem
import com.example.gitschool.network.RetrofitClient
import com.example.gitschool.network.RetrofitYouTubeClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import toAnime
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.CollectionReference
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Suppress("DEPRECATION")
class DetailAnimeActivity : AppCompatActivity() {

    // Існуючі компоненти:
    private lateinit var enToUkTranslator: Translator

    private lateinit var playerTabLayout: TabLayout
    private lateinit var playerContentContainer: FrameLayout
    private lateinit var youtubePlayerView: YouTubePlayerView
    private lateinit var placeholderPlayerContainer: FrameLayout
    private lateinit var textViewTrailerError: TextView

    private var youtubeTrailerId: String? = null // Для зберігання ID трейлера
    private var youtubePlayerInstance: YouTubePlayer? = null // Для збереження інстансу плеєра
    private var isTrailerTabSelected = true // Початково вибрано трейлер

    private lateinit var backArrow: TextView
    private lateinit var headerBackground: ImageView
    private lateinit var animeImage: ImageView
    private lateinit var animeTitle: TextView
    private lateinit var animeScore: TextView
    private lateinit var animeDescription: TextView

    private lateinit var genresText: TextView
    private lateinit var yearText: TextView
    private lateinit var studioText: TextView
    private lateinit var typeText: TextView
    private lateinit var statusText: TextView
    private lateinit var durationText: TextView

    private lateinit var buttonWatchUkr: Button
    private lateinit var buttonAnimeStatus: Button
    private var currentAnimeStatus: String? = null

    private lateinit var viewingOrderAdapter: ViewingOrderAdapter
    private lateinit var viewingOrderRecycler: RecyclerView
    private lateinit var similarRecycler: RecyclerView
    private lateinit var commentsRecycler: RecyclerView
    private lateinit var statusOptionsContainer: LinearLayout

    // Поле для введення коментаря
    private lateinit var editTextComment: EditText
    private lateinit var buttonAddComment: Button

    // Нові UI-компоненти для реакцій
    private lateinit var likeButton: ImageButton
    private lateinit var dislikeButton: ImageButton
    private lateinit var likeCountText: TextView
    private lateinit var dislikeCountText: TextView

    private lateinit var db: FirebaseFirestore
    private val auth = FirebaseAuth.getInstance()
    private var animeId: Int = -1
    private var currentAnimeDetail: AnimeDetail? = null
    private var hasSavedHistory = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_anime)

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.UKRAINIAN)
            .build()
        enToUkTranslator = Translation.getClient(options)

// Завантаження моделі
        enToUkTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                Log.d("Translator", "Модель перекладу завантажена")
            }
            .addOnFailureListener {
                Log.e("Translator", "Не вдалося завантажити модель", it)
            }

        db = FirebaseFirestore.getInstance()

        // ---------------- Ініціалізація View ---------------- //
        playerTabLayout = findViewById(R.id.playerTabLayout)
        playerContentContainer = findViewById(R.id.playerContentContainer)
        youtubePlayerView = findViewById(R.id.youtubePlayerView)
        placeholderPlayerContainer = findViewById(R.id.placeholderPlayerContainer)
        textViewTrailerError = findViewById(R.id.textViewTrailerError)
        backArrow = findViewById(R.id.back_arrow)
        headerBackground = findViewById(R.id.detail_header_background)
        animeImage = findViewById(R.id.detail_anime_image)
        animeTitle = findViewById(R.id.detail_anime_title)
        animeScore = findViewById(R.id.detail_anime_score)
        animeDescription = findViewById(R.id.detail_anime_description)
        statusOptionsContainer = findViewById(R.id.statusOptionsContainer)

        genresText = findViewById(R.id.detail_genres)
        yearText = findViewById(R.id.detail_year)
        studioText = findViewById(R.id.detail_studio)
        typeText = findViewById(R.id.detail_type)
        statusText = findViewById(R.id.detail_status)
        durationText = findViewById(R.id.detail_duration)

        buttonWatchUkr = findViewById(R.id.button_watch_ukr)
        buttonAnimeStatus = findViewById(R.id.buttonAnimeStatus)
        buttonAnimeStatus.text = "ДОДАТИ В ЗАКЛАДКИ"

        viewingOrderRecycler = findViewById(R.id.recycler_viewing_order)
        viewingOrderRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        similarRecycler = findViewById(R.id.recycler_similar)
        similarRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        commentsRecycler = findViewById(R.id.recycler_comments)
        commentsRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val commentAdapter = CommentAdapter(
            mutableListOf(),
            onLikeClick = { comment, holder ->
                toggleCommentReaction(comment, "like") { newL, newD, your ->
                    holder.likeCount.text    = newL.toString()
                    holder.dislikeCount.text = newD.toString()
                    holder.likeButton.isSelected    = (your == "like")
                    holder.dislikeButton.isSelected = (your == "dislike")
                }
            },
            onDislikeClick = { comment, holder ->
                toggleCommentReaction(comment, "dislike") { newL, newD, your ->
                    holder.likeCount.text    = newL.toString()
                    holder.dislikeCount.text = newD.toString()
                    holder.likeButton.isSelected    = (your == "like")
                    holder.dislikeButton.isSelected = (your == "dislike")
                }
            }
        )
        commentsRecycler.adapter = commentAdapter

        editTextComment = findViewById(R.id.editTextComment)
        buttonAddComment = findViewById(R.id.button_add_comments)

        // Ініціалізація реакцій (лайки/дизлайки)
        likeButton = findViewById(R.id.likeButton)
        dislikeButton = findViewById(R.id.dislikeButton)
        likeCountText = findViewById(R.id.likeCountText)
        dislikeCountText = findViewById(R.id.dislikeCountText)

        // Налаштовуємо обробники для реакцій
        likeButton.setOnClickListener { toggleReaction(animeId, "like") }
        dislikeButton.setOnClickListener { toggleReaction(animeId, "dislike") }


        findViewById<TextView>(R.id.statusOptionRemove).setOnClickListener {
            removeAnimeStatusInFirestore(animeId)
            statusOptionsContainer.visibility = View.GONE // Ховаємо меню після вибору
        }
        findViewById<TextView>(R.id.statusOptionWatching).setOnClickListener {
            updateAnimeStatusInFirestore(animeId, AnimeStatus.WATCHING)
            statusOptionsContainer.visibility = View.GONE
        }
        findViewById<TextView>(R.id.statusOptionPlanToWatch).setOnClickListener {
            updateAnimeStatusInFirestore(animeId, AnimeStatus.PLAN_TO_WATCH)
            statusOptionsContainer.visibility = View.GONE
        }
        findViewById<TextView>(R.id.statusOptionCompleted).setOnClickListener {
            updateAnimeStatusInFirestore(animeId, AnimeStatus.COMPLETED)
            statusOptionsContainer.visibility = View.GONE
        }
        findViewById<TextView>(R.id.statusOptionDropped).setOnClickListener {
            updateAnimeStatusInFirestore(animeId, AnimeStatus.DROPPED)
            statusOptionsContainer.visibility = View.GONE
        }
        findViewById<TextView>(R.id.statusOptionOnHold).setOnClickListener {
            updateAnimeStatusInFirestore(animeId, AnimeStatus.ON_HOLD)
            statusOptionsContainer.visibility = View.GONE
        }
        findViewById<TextView>(R.id.statusOptionFavorite).setOnClickListener {
            updateAnimeStatusInFirestore(animeId, AnimeStatus.FAVORITE)
            statusOptionsContainer.visibility = View.GONE
        }

        animeId = intent.getIntExtra("anime_id", -1)
        if (animeId == -1) {
            Toast.makeText(this, "Невірний ID аніме", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewingOrderAdapter = ViewingOrderAdapter(emptyList()) { clickedItem ->
            Log.d("ViewingOrder", "Clicked on: ${clickedItem.title} (ID: ${clickedItem.malId})")
            openDetailAgain(clickedItem.malId)
        }
        viewingOrderRecycler.adapter = viewingOrderAdapter

        // ---------------- Кнопки ---------------- //

        // Стрілка "Назад"
        backArrow.setOnClickListener { onBackPressed() }

        // "Дивитися онлайн"
        val scrollView = findViewById<ScrollView>(R.id.detail_scroll)
        val placeholderPlayerContainer = findViewById<View>(R.id.placeholderPlayerContainer)
        val buttonWatchUkr = findViewById<Button>(R.id.button_watch_ukr)

        buttonWatchUkr.setOnClickListener {
            scrollView.post {
                val location = IntArray(2)
                placeholderPlayerContainer.getLocationInWindow(location)
                val scrollLocation = IntArray(2)
                scrollView.getLocationInWindow(scrollLocation)
                val relativeY = location[1] - scrollLocation[1]
                scrollView.smoothScrollTo(0, relativeY)
            }
            // TODO: Запуск VideoView/WebView/ExoPlayer
        }

        setupTabLayout()

        // "Додати в закладки"
        buttonAnimeStatus.text = "Завантаження..."
        loadCurrentAnimeStatus(animeId)

        buttonAnimeStatus.setOnClickListener { view ->
            // Показувати/ховати контейнер опцій
            val isVisible = statusOptionsContainer.visibility == View.VISIBLE
            statusOptionsContainer.visibility = if (isVisible) View.GONE else View.VISIBLE

            // Показувати/ховати опцію "Видалити"
            val removeOption = findViewById<TextView>(R.id.statusOptionRemove)
            removeOption.visibility = if (currentAnimeStatus != null) View.VISIBLE else View.GONE
        }

        // "Надіслати" коментар
        buttonAddComment.setOnClickListener {
            val commentText = editTextComment.text.toString().trim()
            if (commentText.isEmpty()) {
                Toast.makeText(this, "Введіть коментар", Toast.LENGTH_SHORT).show()
            } else if (commentText.length > 200) {
                Toast.makeText(this, "Максимум 200 символів", Toast.LENGTH_SHORT).show()
            } else {
                addCommentToFirebase(animeId, commentText)
            }
        }


        // ---------------- Завантаження даних ---------------- //
        loadViewingOrder(animeId)   // порядок перегляду
        loadAnimeDetails(animeId)
        loadSimilarAnime(animeId)   // схожі аніме
        loadComments(animeId)       // коментарі
        loadReaction(animeId)       // завантаження лайків/дизлайків для даного аніме

        lifecycle.addObserver(youtubePlayerView)
    }

    private val typeTranslations = mapOf(
        "TV" to "Серіал",
        "Movie" to "Фільм",
        "OVA" to "ОВА",
        "ONA" to "ОНА",
        "Special" to "Спешл",
        "Music" to "Музичне",
        "Unknown" to "Невідомо"
    )

    private val statusTranslations = mapOf(
        "Finished Airing" to "Завершено",
        "Currently Airing" to "Транслюється",
        "Not yet aired" to "Ще не вийшло",
        "Upcoming" to "Найближчим часом",
        "Cancelled" to "Скасовано",
        "Hiatus" to "Призупинено"
    )

    private fun translate(text: String, callback: (String) -> Unit) {
        enToUkTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                Log.d("Translator", "переклало")
                callback(translatedText)
            }
            .addOnFailureListener {
                Log.d("Translator", "помилка — залишаємо оригінал")
                callback(text) // якщо помилка — залишаємо оригінал
            }
    }

    // ---------------- Завантаження детальної інформації ---------------- //
    private fun loadAnimeDetails(animeId: Int) {
        lifecycleScope.launch {
            try {
                val detailResponse = RetrofitClient.apiService.getAnimeDetails(animeId)
                val anime = detailResponse.data
                if (anime != null) {
                    currentAnimeDetail = anime

                    // Заголовок (перекладаємо)
                    animeTitle.text = anime.title

                    // Оцінка
                    animeScore.text = anime.score?.toString() ?: "Невизначено"

                    // Опис (перекладаємо)
                    translate(anime.synopsis ?: "Опис відсутній") { animeDescription.text = it }

                    // Трейлер
                    youtubeTrailerId = anime.trailer?.youtubeId
                    if (youtubeTrailerId.isNullOrBlank()) {
                        youtubeTrailerId = searchTrailerOnYouTube(anime.title ?: "")
                        Log.d("Trailer", "Found trailer via YouTube API: $youtubeTrailerId")
                    } else {
                        Log.d("Trailer", "Trailer ID from API: $youtubeTrailerId")
                    }
                    initializeYoutubePlayer()

                    // Постер
                    val imageUrl = anime.images?.jpg?.imageUrl
                    Glide.with(this@DetailAnimeActivity)
                        .load(imageUrl)
                        .into(animeImage)
                    // Фон
                    Glide.with(this@DetailAnimeActivity)
                        .load(imageUrl)
                        .into(headerBackground)

                    // Жанри (перекладаємо)
                    val genres = anime.genres?.joinToString(", ") { it.name ?: "" } ?: "-"
                    translate(genres) { genresText.text = it }

                    // Рік
                    yearText.text = anime.year?.toString() ?: "Невідомо"

                    // Студії (перекладаємо)
                    val studios = anime.studios?.joinToString(", ") { it.name ?: "" } ?: "-"
                    translate(studios) { studioText.text = it }

                    // Тип (локальний переклад)
                    val typeOriginal = anime.type ?: "-"
                    typeText.text = typeTranslations[typeOriginal] ?: typeOriginal

                    // Статус (локальний переклад)
                    val statusOriginal = anime.status ?: "-"
                    statusText.text = statusTranslations[statusOriginal] ?: statusOriginal

                    // Тривалість (перекладаємо)
                    translate(anime.duration ?: "-") { durationText.text = it }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@DetailAnimeActivity,
                    "Помилка завантаження деталей",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun saveViewHistory() {
        val anime = currentAnimeDetail ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val userHistoryRef = db
            .collection("users")
            .document(uid)
            .collection("viewHistory")

        val animeDocRef = userHistoryRef.document(anime.malId.toString())

        val data = hashMapOf(
            "malId"     to anime.malId,
            "title"     to anime.title,
            "imageUrl"  to anime.images?.jpg?.imageUrl,
            "episodes"  to anime.episodes,
            "year"      to anime.year,
            "rating"    to anime.score,
            "genres"    to anime.genres?.mapNotNull { it.name },
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // Зберігаємо/оновлюємо запис
        animeDocRef.set(data)
            .addOnSuccessListener {
                Log.d("History", "Аніме збережено/оновлено в історії.")
                // Після успішного збереження — перевірка ліміту
                enforceHistoryLimit(userHistoryRef)
            }
            .addOnFailureListener { e ->
                Log.e("History", "Помилка при збереженні в історію", e)
            }
    }

    private fun enforceHistoryLimit(historyRef: CollectionReference) {
        historyRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val docs = snapshot.documents
                if (docs.size > 50) {
                    val docsToDelete = docs.subList(50, docs.size)
                    for (doc in docsToDelete) {
                        doc.reference.delete()
                    }
                    Log.d("History", "Видалено ${docsToDelete.size} найстаріших записів з історії")
                }
            }
            .addOnFailureListener { e ->
                Log.e("History", "Помилка при обмеженні кількості записів", e)
            }
    }



    private fun initializeYoutubePlayer() {
        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youtubePlayerInstance = youTubePlayer
                if (!youtubeTrailerId.isNullOrBlank()) {
                    youTubePlayer.cueVideo(youtubeTrailerId!!, 0f)
                    Log.d("Trailer", "YouTube player ready, cued video: $youtubeTrailerId")
                } else {
                    Log.d("Trailer", "YouTube player ready, but no valid trailer ID.")
                }
                updatePlayerVisibility()
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                // Коли плеєр реально починає відтворювати — зберігаємо в історію
                if (state == PlayerConstants.PlayerState.PLAYING && !hasSavedHistory) {
                    saveViewHistory()
                    hasSavedHistory = true
                }
            }

            override fun onError(
                youTubePlayer: YouTubePlayer,
                error: PlayerConstants.PlayerError
            ) {
                super.onError(youTubePlayer, error)
                Log.e("Trailer", "YouTube Player Error: $error")
                textViewTrailerError.text = "Помилка завантаження трейлера"
                updatePlayerVisibility()
            }
        })
    }


    // ---------------- Налаштування вкладок ---------------- //
    private fun setupTabLayout() {
        isTrailerTabSelected = true
        updatePlayerVisibility()

        playerTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isTrailerTabSelected = (tab?.position == 0)
                Log.d("Tabs", "Tab selected: ${tab?.text}, isTrailer: $isTrailerTabSelected")
                updatePlayerVisibility()
                if (isTrailerTabSelected && !youtubeTrailerId.isNullOrBlank()) {
                    saveViewHistory() }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // ---------------- Оновлення видимості плеєра/заглушки ---------------- //
    private fun updatePlayerVisibility() {
        if (isTrailerTabSelected) {
            placeholderPlayerContainer.visibility = android.view.View.GONE
            if (!youtubeTrailerId.isNullOrBlank()) {
                youtubePlayerView.visibility = android.view.View.VISIBLE
                textViewTrailerError.visibility = android.view.View.GONE
            } else {
                youtubePlayerView.visibility = android.view.View.GONE
                textViewTrailerError.text = "Трейлер недоступний"
                textViewTrailerError.visibility = android.view.View.VISIBLE
            }
        } else {
            youtubePlayerView.visibility = android.view.View.GONE
            textViewTrailerError.visibility = android.view.View.GONE
            placeholderPlayerContainer.visibility = android.view.View.VISIBLE
            youtubePlayerInstance?.pause()
        }
    }

    // ---------------- Завантаження даних порядку перегляду ---------------- //
    private fun loadViewingOrder(animeId: Int) {
        val relevantRelations = setOf("Prequel", "Sequel", "Parent story", "Side story", "Spin-off", "Other", "Summary")

        lifecycleScope.launch {
            val titleTextView = findViewById<TextView>(R.id.textViewViewingOrderTitle)
            try {
                Log.d("ViewingOrder", "Fetching relations for ID: $animeId")
                val relationsResponse = RetrofitClient.apiService.getAnimeRelations(animeId)
                val viewingOrderList = mutableListOf<ViewingOrderItem>()
                relationsResponse.data?.forEach { relationGroup ->
                    if (relevantRelations.contains(relationGroup.relation) && !relationGroup.entries.isNullOrEmpty()) {
                        relationGroup.entries.forEach { entry ->
                            if (entry.type == "anime" && entry.malId != null && !entry.name.isNullOrBlank()) {
                                viewingOrderList.add(
                                    ViewingOrderItem(
                                        malId = entry.malId,
                                        title = entry.name,
                                        relation = relationGroup.relation ?: "Зв'язок"
                                    )
                                )
                            }
                        }
                    }
                }
                Log.d("ViewingOrder", "Found ${viewingOrderList.size} related items.")
                if (viewingOrderList.isNotEmpty()) {
                    viewingOrderAdapter.updateData(viewingOrderList)
                    titleTextView?.visibility = android.view.View.VISIBLE
                    viewingOrderRecycler.visibility = android.view.View.VISIBLE
                } else {
                    Log.d("ViewingOrder", "No relevant related items found.")
                    titleTextView?.visibility = android.view.View.GONE
                    viewingOrderRecycler.visibility = android.view.View.GONE
                }
            } catch (e: Exception) {
                Log.e("ViewingOrder", "Error loading viewing order", e)
                titleTextView?.visibility = android.view.View.GONE
                viewingOrderRecycler.visibility = android.view.View.GONE
            }
        }
    }

    // ---------------- Завантаження схожих аніме ---------------- //
    private fun loadSimilarAnime(animeId: Int) {
        val similarTitleTextView = findViewById<TextView>(R.id.detail_similar_title)
        val similarRecyclerView = findViewById<RecyclerView>(R.id.recycler_similar)

        lifecycleScope.launch {
            try {
                Log.d("SimilarAnime", "Fetching recommendations for ID: $animeId")
                val recommendationsResponse = RetrofitClient.apiService.getAnimeRecommendations(animeId)
                val recs = recommendationsResponse.data
                val animeList = recs?.mapNotNull { it.entry?.toAnime() } ?: emptyList()
                Log.d("SimilarAnime", "Found ${animeList.size} recommendations.")
                if (animeList.isNotEmpty()) {
                    val adapter = AnimeAdapter(animeList) { clickedAnime ->
                        clickedAnime.malId?.let { openDetailAgain(it) }
                    }
                    similarRecyclerView.adapter = adapter
                    similarTitleTextView?.visibility = android.view.View.VISIBLE
                    similarRecyclerView.visibility = android.view.View.VISIBLE
                } else {
                    Log.d("SimilarAnime", "No recommendations found.")
                    similarTitleTextView?.visibility = android.view.View.GONE
                    similarRecyclerView.visibility = android.view.View.GONE
                }
            } catch (e: Exception) {
                Log.e("SimilarAnime", "Error loading recommendations", e)
                similarTitleTextView?.visibility = android.view.View.GONE
                similarRecyclerView.visibility = android.view.View.GONE
            }
        }
    }

    private fun openDetailAgain(id: Int) {
        finish()
        startActivity(intent.apply { putExtra("anime_id", id) })
    }

    // ---------------- Коментарі ---------------- //
    private fun addCommentToFirebase(animeId: Int, commentText: String) {
        val user = auth.currentUser ?: return showToast(getString(R.string.auth_required))
        val uid = user.uid

        db.collection("comments")
            .whereEqualTo("animeId", animeId)
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    showToast("Ви вже додали коментар до цього аніме.")
                    return@addOnSuccessListener
                }

                db.collection("users").document(uid).get()
                    .addOnSuccessListener { userDoc ->
                        val userName = userDoc.getString("name") ?: user.displayName ?: "Анонім"
                        val userAvatarUrl = userDoc.getString("avatarUrl") // Отримуємо URL аватара

                        val firestoreID = db.collection("comments").document().id
                        val commentDocRef = db.collection("comments").document(firestoreID)

                        val newComment = Comment(
                            userId = uid,
                            userName = userName,
                            userAvatarUrl = userAvatarUrl, // Зберігаємо URL аватара
                            text = commentText,
                            timestamp = com.google.firebase.Timestamp.now(),
                            animeTitle   = currentAnimeDetail?.title ?: "",
                            animeId = animeId,
                            likeCount = 0,
                            dislikeCount = 0,
                            documentId = firestoreID // Можна зберегти ID для майбутніх операцій
                        )

                        buttonAddComment.isEnabled = false // Блокуємо кнопку

                        // Зберігаємо коментар
                        commentDocRef.set(newComment)
                            .addOnSuccessListener {
                                showToast("Коментар додано")
                                editTextComment.setText("")
                                // Додаємо коментар в адаптер і прокручуємо
                                (commentsRecycler.adapter as? CommentAdapter)?.addComment(newComment)
                                commentsRecycler.scrollToPosition(0)
                                buttonAddComment.isEnabled = true
                                listenComments(animeId)
                                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(editTextComment.windowToken, 0)
                                editTextComment.clearFocus()
                            }
                            .addOnFailureListener { e ->
                                showToast("Помилка додавання коментаря")
                                Log.e("AddComment", "Error adding comment", e)
                                buttonAddComment.isEnabled = true
                            }
                    }
                    .addOnFailureListener { e ->
                        showToast("Помилка отримання даних користувача для коментаря")
                        Log.e("AddComment", "Error fetching user data for comment", e)
                    }
            }
            .addOnFailureListener { e ->
                showToast("Помилка перевірки існуючих коментарів")
                Log.e("AddComment", "Error checking existing comments", e)
            }
    }


    private fun loadComments(animeId: Int) {
        Log.d("LoadComments", "Loading comments for anime ID: $animeId")

        db.collection("comments")
            .whereEqualTo("animeId", animeId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50) // Обмежимо кількість для початку
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("LoadComments", "Query successful. Found ${querySnapshot.size()} documents.")
                val commentList = mutableListOf<Comment>()
                for (doc in querySnapshot.documents) {
                    try {
                        val comment = doc.toObject(Comment::class.java)
                        if (comment != null) {
                            comment.documentId = doc.id // Присвоюємо ID документа
                            commentList.add(comment)
                        } else {
                            Log.w(
                                "LoadComments",
                                "Failed to parse document ${doc.id} to Comment object."
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("LoadComments", "Exception parsing document ${doc.id}", e)
                    }
                }
                Log.d("LoadComments", "Loaded ${commentList.size} comments")
                // Оновлюємо дані в адаптері
                (commentsRecycler.adapter as? CommentAdapter)?.updateComments(commentList)

                if (commentList.isNotEmpty()) {
                    // Якщо коментарі є - показуємо заголовок і список
                    Log.d("LoadComments", "Comments found, setting VISIBLE")
                    commentsRecycler.visibility = View.VISIBLE
                } else {
                    // Якщо коментарів немає - ховаємо заголовок і список
                    Log.d("LoadComments", "Comment list is empty, setting GONE")
                    commentsRecycler.visibility = View.GONE
                }

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка завантаження коментарів", Toast.LENGTH_SHORT).show()
                Log.e("LoadComments", "Error fetching comments", e)
                commentsRecycler.visibility = View.GONE
            }
    }

    // ---------------- Заховування клавіатури при натисканні поза EditText ---------------- //
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    // ------------- Функції для лайків/дизлайків (реакцій) -------------
    // Завантаження поточної реакції та лічильників із Firestore
    private fun loadReaction(animeId: Int) {
        val docRef = db.collection("animeReactions").document(animeId.toString())
        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val likesCount = snapshot.getLong("likesCount") ?: 0
                val dislikesCount = snapshot.getLong("dislikesCount") ?: 0
                likeCountText.text = likesCount.toString()
                dislikeCountText.text = dislikesCount.toString()

                val usersMap = snapshot.get("users") as? Map<String, String>
                val currentUserId = auth.currentUser?.uid
                val userReaction = usersMap?.get(currentUserId)
                if (userReaction == "like") {
                    likeButton.setBackgroundResource(R.drawable.button_selected_background)
                    dislikeButton.setBackgroundResource(R.drawable.button_default_background)
                } else if (userReaction == "dislike") {
                    dislikeButton.setBackgroundResource(R.drawable.button_selected_background)
                    likeButton.setBackgroundResource(R.drawable.button_default_background)
                } else {
                    likeButton.setBackgroundResource(R.drawable.button_default_background)
                    dislikeButton.setBackgroundResource(R.drawable.button_default_background)
                }
            } else {
                // Якщо документ відсутній, встановлюємо 0 для обох лічильників і дефолтний стан кнопок
                likeCountText.text = "0"
                dislikeCountText.text = "0"
                likeButton.setBackgroundResource(R.drawable.button_default_background)
                dislikeButton.setBackgroundResource(R.drawable.button_default_background)
            }
        }.addOnFailureListener { e ->
            Log.e("Reaction", "Error loading reaction", e)
        }
    }

    // Функція для оновлення реакції користувача (лайк/дизлайк) транзакційно
    private fun toggleReaction(animeId: Int, reaction: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val docRef = FirebaseFirestore.getInstance()
            .collection("animeReactions")
            .document(animeId.toString()) // <-- Зберігаємо як рядок, бо document ID — String

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentLikes = snapshot.getLong("likesCount") ?: 0
            val currentDislikes = snapshot.getLong("dislikesCount") ?: 0
            val usersMap = snapshot.get("users") as? Map<String, String> ?: emptyMap()
            val currentUserReaction = usersMap[userId]
            var newLikes = currentLikes
            var newDislikes = currentDislikes
            val updatedUsers = usersMap.toMutableMap()

            if (currentUserReaction == reaction) {
                updatedUsers.remove(userId)
                if (reaction == "like") newLikes-- else newDislikes--
            } else {
                if (currentUserReaction == "like") newLikes--
                if (currentUserReaction == "dislike") newDislikes--
                if (reaction == "like") newLikes++
                if (reaction == "dislike") newDislikes++
                updatedUsers[userId] = reaction
            }

            val newData = hashMapOf(
                "likesCount" to newLikes,
                "dislikesCount" to newDislikes,
                "users" to updatedUsers
            )

            if (snapshot.exists()) {
                transaction.update(docRef, newData)
            } else {
                transaction.set(docRef, newData)
            }
            newData // Повертаємо оновлені дані
        }.addOnSuccessListener { result ->
            Log.d("Reaction", "Reaction updated successfully")
            // Оновлюємо UI на основі результату транзакції
            val newLikesCount = result["likesCount"] as? Long ?: 0
            val newDislikesCount = result["dislikesCount"] as? Long ?: 0
            val updatedUsersMap = result["users"] as? Map<String, String> ?: emptyMap()
            val currentUserNewReaction = updatedUsersMap[userId]

            likeCountText.text = newLikesCount.toString()
            dislikeCountText.text = newDislikesCount.toString()

            // Оновлюємо фон кнопок
            likeButton.setBackgroundResource(
                if (currentUserNewReaction == "like") R.drawable.button_selected_background else R.drawable.button_default_background
            )
            dislikeButton.setBackgroundResource(
                if (currentUserNewReaction == "dislike") R.drawable.button_selected_background else R.drawable.button_default_background
            )
        }.addOnFailureListener { e ->
            Log.e("Reaction", "Error updating reaction", e)
        }
    }


    private fun updateAnimeStatusInFirestore(animeId: Int, status: String) {
        val userId = auth.currentUser?.uid ?: return showToast(getString(R.string.auth_required))
        val statusDocRef = db.collection("users").document(userId)
            .collection("animeStatuses").document(animeId.toString())

        buttonAnimeStatus.isEnabled = false
        buttonAnimeStatus.text = getString(R.string.status_saving)

        lifecycleScope.launch {
            var genres: List<Genre>? = null
            var fetchedAnimeTitle: String? = null
            var fetchedAnimeImageUrl: String? = null
            var currentEpisodeCount: Int? = null // <-- Змінна для к-сті серій

            try {
                Log.d("AnimeStatus", "Fetching details for anime ID: $animeId before saving status '$status'")
                val detailResponse = RetrofitClient.apiService.getAnimeDetails(animeId)
                val anime = detailResponse.data
                genres = anime?.genres
                fetchedAnimeTitle = anime?.title
                fetchedAnimeImageUrl = anime?.images?.jpg?.imageUrl
                currentEpisodeCount = anime?.episodes // <-- Отримуємо кількість серій
                Log.d("AnimeStatus", "Fetched details: Title='${fetchedAnimeTitle}', Episodes=${currentEpisodeCount}, Genres=${genres?.map { it.name }}")
            } catch (e: Exception) {
                Log.e("AnimeStatus", "Failed to fetch details for $animeId when updating status", e)
            }

            val statusData: MutableMap<String, Any> = mutableMapOf(
                "malId" to animeId,
                "status" to status,
                "lastUpdated" to com.google.firebase.Timestamp.now()
            )

            fetchedAnimeTitle?.let { statusData["animeTitle"] = it }
            fetchedAnimeImageUrl?.let { statusData["animeImageUrl"] = it }
            statusData["lastCheckedEpisodeCount"] = currentEpisodeCount ?: 0

            if (!genres.isNullOrEmpty()) {
                val genresMapList = genres.mapNotNull { g ->
                    if (g.malId != null && g.name != null) {
                        mapOf("malId" to g.malId, "name" to g.name)
                    } else null
                }
                if (genresMapList.isNotEmpty()) {
                    statusData["genres"] = genresMapList
                }
            }

            // Викликаємо збереження даних
            saveStatusData(statusDocRef, statusData, status, animeId)

        }
    }

    private fun saveStatusData(
        docRef: com.google.firebase.firestore.DocumentReference,
        data: Map<String, Any>,
        status: String,
        animeId: Int
    ) {
        docRef.set(data)
            .addOnSuccessListener {
                Log.d("AnimeStatus", "Status '$status' updated for $animeId")
                showToast(getString(R.string.status_updated, status))
                currentAnimeStatus = status
                buttonAnimeStatus.text = status
                buttonAnimeStatus.isEnabled = true
            }
            .addOnFailureListener { e ->
                Log.e("AnimeStatus", "Error updating status '$status' for $animeId", e)
                showToast(getString(R.string.status_update_error))
                buttonAnimeStatus.isEnabled = true
                buttonAnimeStatus.text = currentAnimeStatus ?: getString(R.string.status_add_to_list)
            }
    }

    private fun removeAnimeStatusInFirestore(animeId: Int) {
        val userId = auth.currentUser?.uid ?: return showToast(getString(R.string.auth_required))
        val statusDocRef = db.collection("users").document(userId)
            .collection("animeStatuses").document(animeId.toString())

        buttonAnimeStatus.isEnabled = false
        buttonAnimeStatus.text = getString(R.string.status_removing)

        statusDocRef.delete()
            .addOnSuccessListener {
                Log.d("AnimeStatus", "Status removed for $animeId")
                showToast(getString(R.string.status_removed))
                currentAnimeStatus = null
                buttonAnimeStatus.text = getString(R.string.status_add_to_list)
                buttonAnimeStatus.isEnabled = true
            }
            .addOnFailureListener { e ->
                Log.e("AnimeStatus", "Error removing status for $animeId", e)
                showToast(getString(R.string.status_remove_error))
                buttonAnimeStatus.text = currentAnimeStatus ?: getString(R.string.status_add_to_list)
                buttonAnimeStatus.isEnabled = true
            }
    }

    private fun loadCurrentAnimeStatus(animeId: Int) {
        val userId = auth.currentUser?.uid ?: return
        buttonAnimeStatus.text = getString(R.string.status_loading)
        buttonAnimeStatus.isEnabled = false
        val statusDocRef = db.collection("users").document(userId)
            .collection("animeStatuses").document(animeId.toString())

        statusDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val status = documentSnapshot.getString("status")
                    currentAnimeStatus = status
                    buttonAnimeStatus.text = status ?: getString(R.string.status_add_to_list)
                    Log.d("AnimeStatus", "Loaded status for $animeId: $status")
                } else {
                    currentAnimeStatus = null
                    buttonAnimeStatus.text = getString(R.string.status_add_to_list)
                    Log.d("AnimeStatus", "No status found for $animeId")
                }
                buttonAnimeStatus.isEnabled = true
            }
            .addOnFailureListener { e ->
                Log.e("AnimeStatus", "Error loading status for $animeId", e)
                buttonAnimeStatus.text = getString(R.string.status_error)
                buttonAnimeStatus.isEnabled = true
                currentAnimeStatus = null
            }
    }

    private suspend fun searchTrailerOnYouTube(animeTitle: String): String? {
        val query = "$animeTitle trailer"
        val apiKey = "AIzaSyDgETDvr2yABzXksU8KE4ZYuohrRJoPJag"
        return try {
            val response = RetrofitYouTubeClient.apiService.searchTrailer(query = query, key = apiKey)
            if (response.items.isNotEmpty()) {
                response.items.first().id.videoId
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun loadCommentReaction(
        comment: Comment,
        onLoaded: (likes: Long, dislikes: Long, userReaction: String?) -> Unit
    ) {
        val docRef = comment.documentId?.let { db.collection("commentReactions").document(it) }
        docRef?.get()
            ?.addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    // Створюємо початковий документ
                    val initData = mapOf(
                        "likesCount" to 0L,
                        "dislikesCount" to 0L,
                        "users" to emptyMap<String, String>()
                    )
                    docRef.set(initData)
                        .addOnSuccessListener {
                            onLoaded(0L, 0L, null)
                        }
                        .addOnFailureListener { e ->
                            Log.e("CommentReaction", "Error creating reaction doc", e)
                            onLoaded(0L, 0L, null)
                        }
                } else {
                    val likes = snap.getLong("likesCount") ?: 0L
                    val dislikes = snap.getLong("dislikesCount") ?: 0L
                    val usersMap = snap.get("users") as? Map<String, String> ?: emptyMap()
                    val userReaction = auth.currentUser?.uid?.let { usersMap[it] }
                    onLoaded(likes, dislikes, userReaction)
                }
            }
            ?.addOnFailureListener { e ->
                Log.e("CommentReaction", "Error loading reaction", e)
                onLoaded(0L, 0L, null)
            }
    }

    private fun toggleCommentReaction(
        comment: Comment,
        reaction: String,
        onUpdated: (newLikes: Long, newDislikes: Long, yourReaction: String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return showToast("Потрібна авторизація")
        val docRef = comment.documentId?.let { db.collection("commentReactions").document(it) }

        db.runTransaction { tx ->
            val snap = docRef?.let { tx.get(it) }
            val currentLikes = snap?.getLong("likesCount") ?: 0L
            val currentDislikes = snap?.getLong("dislikesCount") ?: 0L
            val usersMap = (snap?.get("users") as? Map<String, String>)?.toMutableMap() ?: mutableMapOf()
            val prev = usersMap[userId]

            var newLikes = currentLikes
            var newDislikes = currentDislikes

            if (prev == reaction) {
                // Відміна тієї самої реакції
                usersMap.remove(userId)
                if (reaction == "like") newLikes-- else newDislikes--
            } else {
                // Перемикаємо
                if (prev == "like") newLikes--
                if (prev == "dislike") newDislikes--
                if (reaction == "like") newLikes++ else newDislikes++
                usersMap[userId] = reaction
            }

            val data = mapOf(
                "likesCount" to newLikes,
                "dislikesCount" to newDislikes,
                "users" to usersMap
            )
            if (snap != null) {
                if (snap.exists()) docRef?.let { tx.update(it, data) } else docRef?.let { tx.set(it, data) }
            }
            data
        }.addOnSuccessListener { result ->
            val nl = (result["likesCount"] as? Long) ?: 0L
            val nd = (result["dislikesCount"] as? Long) ?: 0L
            val um = (result["users"] as? Map<String, String>) ?: emptyMap()
            onUpdated(nl, nd, um[userId])
        }.addOnFailureListener { e ->
            Log.e("CommentReaction", "Error updating reaction", e)
        }
    }
    private fun listenComments(animeId: Int) {
        db.collection("comments")
            .whereEqualTo("animeId", animeId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshots?.map { doc ->
                    val c = doc.toObject(Comment::class.java)
                    c.documentId = doc.id
                    c
                }?.toMutableList() ?: mutableListOf()
                (commentsRecycler.adapter as? CommentAdapter)?.updateComments(list)
                commentsRecycler.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
