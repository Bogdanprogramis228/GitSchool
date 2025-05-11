package com.example.gitschool

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Змінено імпорт
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.gitschool.data.AnimeStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase // Імпорт RTDB
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var avatarImage: ImageView
    private lateinit var backgroundImage: ImageView
    private lateinit var userNameHeader: TextView

    // Інформація про акаунт (деталі)
    private lateinit var userNameInfo: TextView
    private lateinit var userEmailInfo: TextView
    private lateinit var userPasswordInfo: TextView
    private lateinit var userRegistrationDateInfo: TextView
    private lateinit var userLastLoginInfo: TextView

    // Статистика (деталі)
    private lateinit var statsWatching: TextView
    private lateinit var statsPlanToWatch: TextView
    private lateinit var statsCompleted: TextView
    private lateinit var statsDropped: TextView
    private lateinit var statsOnHold: TextView
    private lateinit var statsFavorite: TextView
    private lateinit var statsComments: TextView

    // Контейнери деталей (для показу/приховування)
    private lateinit var accountInfoDetails: LinearLayout
    private lateinit var statisticsDetails: LinearLayout

    // Стрілки
    private lateinit var accountInfoArrow: ImageView
    private lateinit var accountStatisticsArrow: ImageView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // Використовуємо RTDB для зображень, як у вашому коді (хоча Firestore був би консистентнішим)
    private val database = FirebaseDatabase.getInstance("https://gitschool-9eede-default-rtdb.europe-west1.firebasedatabase.app/").reference


    // Інше
    private var statisticsLoaded = false // Прапорець, щоб не завантажувати статистику щоразу

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile) // Переконайтесь, що макет називається user_profile.xml

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupButtonClickListeners()
        loadProfileData()
    }
    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun initializeViews() {
        // --- Верхній блок ---
        backgroundImage = findViewById(R.id.backgroundImage)
        avatarImage = findViewById(R.id.avatarImage)
        userNameHeader = findViewById(R.id.userName) // Ім'я під аватаркою

        // --- Розкривні деталі ---
        accountInfoDetails = findViewById(R.id.accountInfoDetails)
        statisticsDetails = findViewById(R.id.statisticsDetails)

        // Стрілки
        accountInfoArrow = findViewById(R.id.accountInfoArrow)
        accountStatisticsArrow = findViewById(R.id.accountStatisticsArrow)

        // --- Текст в деталях Акаунта ---
        userNameInfo = findViewById(R.id.userNameInfo) // Якщо у вас є окремий TextView для імені тут
        userEmailInfo = findViewById(R.id.userEmail)
        userPasswordInfo = findViewById(R.id.userPassword) // TextView для пароля
        userRegistrationDateInfo = findViewById(R.id.userRegistrationDate)
        userLastLoginInfo = findViewById(R.id.userLastLogin)

        // --- Текст в деталях Статистики ---
        statsWatching = findViewById(R.id.statsWatching)
        statsPlanToWatch = findViewById(R.id.statsPlanToWatch)
        statsCompleted = findViewById(R.id.statsCompleted)
        statsDropped = findViewById(R.id.statsDropped)
        statsOnHold = findViewById(R.id.statsOnHold)
        statsFavorite = findViewById(R.id.statsFavorite)
        statsComments = findViewById(R.id.statsComments)
    }

    private fun setupButtonClickListeners() {
        // Назад
        findViewById<ImageView>(R.id.backButton).setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Редагувати профіль (аватар/фон)
        findViewById<ImageView>(R.id.editProfileButton).setOnClickListener {
            // TODO: Переконайтесь, що EditProfileActivity зберігає дані в правильне місце (RTDB/Firestore)
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Інформація про акаунт (розкривний)
        findViewById<LinearLayout>(R.id.accountInfoItem).setOnClickListener {
            toggleVisibility(accountInfoDetails, accountInfoArrow)
        }

        // Статистика (розкривний)
        findViewById<LinearLayout>(R.id.accountStatistics).setOnClickListener {
            toggleVisibility(statisticsDetails, accountStatisticsArrow)
            if (!statisticsLoaded && statisticsDetails.visibility == View.VISIBLE) {
                loadStatisticsData()
            }
        }

        // Колекція (перехід)
        findViewById<LinearLayout>(R.id.accountCollection).setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }

        // Коментарі користувача (перехід)
        findViewById<LinearLayout>(R.id.accountComments).setOnClickListener {
            startActivity(Intent(this, MyCommentsActivity::class.java))
        }

        // Повідомлення (перехід)
        findViewById<LinearLayout>(R.id.accountMasseges).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        // Налаштування (перехід)
        findViewById<LinearLayout>(R.id.accountSetings).setOnClickListener { // ID елемента з іконкою налаштувань
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Вийти з акаунту
        findViewById<LinearLayout>(R.id.logoutItem).setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // --- ЗАВАНТАЖЕННЯ ДАНИХ ПРОФІЛЮ ---
    private fun loadProfileData() {
        val user = auth.currentUser
        if (user == null) {
            showToast("Помилка: користувач не авторизований")
            // Можливо, перенаправити на екран входу
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 1. Встановлюємо дані з Firebase Auth
        userEmailInfo.text = user.email ?: "Не вказано"
        setFormattedDate(userRegistrationDateInfo, user.metadata?.creationTimestamp)
        setFormattedDate(userLastLoginInfo, user.metadata?.lastSignInTimestamp)

        // 2. Завантажуємо дані з Firestore (ім'я, пароль)
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nameFromDb = document.getString("name") ?: user.displayName ?: "Гість"
                    userNameHeader.text = nameFromDb
                    userNameInfo.text = nameFromDb // Якщо є окреме поле в деталях

                    // !!! ПОПЕРЕДЖЕННЯ ЩОДО ПАРОЛЯ !!!
                    val passwordFromDb = document.getString("password") ?: "НЕ створений" // Текст за замовчуванням
                    userPasswordInfo.text = passwordFromDb // Відображаємо пароль з Firestore
                    // !!! КІНЕЦЬ ПОПЕРЕДЖЕННЯ !!!

                } else {
                    Log.d("ProfileActivity", "User document not found in Firestore, using Auth name")
                    userNameHeader.text = user.displayName ?: "Гість"
                    userNameInfo.text = user.displayName ?: "Гість"
                    userPasswordInfo.text = "Не знайдено"
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error loading user data from Firestore", e)
                showToast("Помилка завантаження даних Firestore")
                userNameHeader.text = user.displayName ?: "Гість"
                userNameInfo.text = user.displayName ?: "Гість"
                userPasswordInfo.text = "Помилка"
            }

        // 3. Завантажуємо зображення з RTDB (як у вашому попередньому коді)
        loadProfileImagesFromRTDB(user.uid)
    }

    // --- ЗАВАНТАЖЕННЯ ЗОБРАЖЕНЬ з RTDB ---
    private fun loadProfileImagesFromRTDB(uid: String) {
        // --- Завантаження аватара ---
        database.child("users").child(uid).child("avatarBase64").get()
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
                            .into(avatarImage)
                    } catch (e: Exception) {
                        Log.e("LoadProfile", "Error decoding avatarBase64", e)
                        loadAvatarFromResource(uid)
                    }
                } else {
                    loadAvatarFromResource(uid)
                }
            }
            .addOnFailureListener { error ->
                Log.e("LoadProfile", "Failed to load avatarBase64", error)
                loadAvatarFromResource(uid)
            }


        // --- Завантаження фону ---
        database.child("users").child(uid).child("backgroundBase64").get()
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
                            .into(backgroundImage)
                    } catch (e: Exception) {
                        Log.e("LoadProfile", "Error decoding backgroundBase64", e)
                        loadBackgroundFromResource(uid)
                    }
                } else {
                    loadBackgroundFromResource(uid)
                }
            }
            .addOnFailureListener { error ->
                Log.e("LoadProfile", "Failed to load backgroundBase64", error)
                loadBackgroundFromResource(uid)
            }
    }
    private fun loadAvatarFromResource(uid: String) {
        // Завантаження аватара з ресурсного ID (static resource) якщо Base64 відсутній
        database.child("users").child(uid).child("avatarResourceId").get()
            .addOnSuccessListener { resourceIdSnapshot ->
                val resourceId = resourceIdSnapshot.getValue(Int::class.java)
                if (resourceId != null) {
                    Glide.with(this)
                        .load(resourceId)
                        .placeholder(R.drawable.ava_01)
                        .circleCrop()
                        .into(avatarImage)
                    Log.d("LoadProfile", "Avatar loaded from resourceId: $resourceId")
                } else {
                    avatarImage.setImageResource(R.drawable.ava_01)
                    Log.d("LoadProfile", "Avatar resourceId is null, default used")
                }
            }
            .addOnFailureListener { error ->
                Log.e("LoadProfile", "Failed to load avatarResourceId", error)
                avatarImage.setImageResource(R.drawable.ava_01)
            }
    }
    private fun loadBackgroundFromResource(uid: String) {
        // Завантаження фону з ресурсного ID якщо Base64 відсутній
        database.child("users").child(uid).child("backgroundResourceId").get()
            .addOnSuccessListener { resourceIdSnapshot ->
                val resourceId = resourceIdSnapshot.getValue(Int::class.java)
                if (resourceId != null) {
                    Glide.with(this)
                        .load(resourceId)
                        .placeholder(R.drawable.back_01)
                        .centerCrop()
                        .into(backgroundImage)
                    Log.d("LoadProfile", "Background loaded from resourceId: $resourceId")
                } else {
                    backgroundImage.setImageResource(R.drawable.back_01)
                    Log.d("LoadProfile", "Background resourceId is null, default used")
                }
            }
            .addOnFailureListener { error ->
                Log.e("LoadProfile", "Failed to load backgroundResourceId", error)
                backgroundImage.setImageResource(R.drawable.back_01)
            }
    }

    // --- ЗАВАНТАЖЕННЯ ЗОБРАЖЕНЬ (універсальна функція, адаптована під Base64) ---
    private fun loadImage(base64Data: String?, imageView: ImageView, placeholderResId: Int, circleCrop: Boolean) {
        if (!base64Data.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val glideRequest = Glide.with(this).load(imageBytes)
                if (circleCrop) glideRequest.circleCrop() else glideRequest.centerCrop()
                glideRequest.placeholder(placeholderResId).error(placeholderResId).into(imageView)
            } catch (e: IllegalArgumentException) {
                Log.e("LoadImage", "Failed to decode Base64 image", e)
                imageView.setImageResource(placeholderResId)
            }
        } else {
            imageView.setImageResource(placeholderResId)
        }
    }

    // --- ЗАВАНТАЖЕННЯ СТАТИСТИКИ ---
    private fun loadStatisticsData() {
        if (statisticsLoaded) return
        Log.d("ProfileStats", "Loading statistics...")

        val userId = auth.currentUser?.uid ?: return

        // Завантаження статусів
        db.collection("users").document(userId).collection("animeStatuses")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot == null) {
                    Log.w("ProfileStats", "Anime statuses snapshot is null")
                    return@addOnSuccessListener
                }
                var watchingCount = 0; var planToWatchCount = 0; var completedCount = 0
                var droppedCount = 0; var onHoldCount = 0; var favoriteCount = 0

                for (doc in snapshot.documents) {
                    when (doc.getString("status")) {
                        AnimeStatus.WATCHING -> watchingCount++
                        AnimeStatus.PLAN_TO_WATCH -> planToWatchCount++
                        AnimeStatus.COMPLETED -> completedCount++
                        AnimeStatus.DROPPED -> droppedCount++
                        AnimeStatus.ON_HOLD -> onHoldCount++
                        AnimeStatus.FAVORITE -> favoriteCount++
                    }
                }

                statsWatching.text = "Дивлюся: $watchingCount"
                statsPlanToWatch.text = "Буду дивитись: $planToWatchCount"
                statsCompleted.text = "Переглянуто: $completedCount"
                statsDropped.text = "Закинуто: $droppedCount"
                statsOnHold.text = "Нецікаво: $onHoldCount"
                statsFavorite.text = "Улюблене: $favoriteCount"
                statisticsLoaded = true
                Log.d("ProfileStats", "Anime statuses loaded successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileStats", "Error loading anime statuses", e)
                showToast("Помилка завантаження статистики статусів")
                // Можна встановити текст помилки для всіх полів статистики
            }

        // Завантаження кількості коментарів
        db.collection("comments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot?.size() ?: 0
                statsComments.text = "Коментарів залишено: $count"
                Log.d("ProfileStats", "Comments count loaded: $count")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileStats", "Error loading comments count", e)
                statsComments.text = "Коментарів залишено: ?"
                showToast("Помилка завантаження кількості коментарів")
            }
    }

    // --- ФОРМАТУВАННЯ ДАТИ ---
    private fun setFormattedDate(textView: TextView, timestamp: Long?) {
        if (timestamp != null && timestamp > 0) {
            try {
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) // Додав час
                textView.text = sdf.format(Date(timestamp))
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error formatting date", e)
                textView.text = "Невідомо"
            }
        } else {
            textView.text = "Невідомо"
        }
    }

    // --- ПЕРЕМИКАННЯ ВИДИМОСТІ ---
    private fun toggleVisibility(detailsView: View, arrowView: ImageView) {
        val isVisible = detailsView.visibility == View.VISIBLE
        detailsView.animate()
            .alpha(if (isVisible) 0f else 1f)
            .setDuration(200)
            .withStartAction { if (!isVisible) { detailsView.alpha = 0f; detailsView.visibility = View.VISIBLE } }
            .withEndAction { if (isVisible) detailsView.visibility = View.GONE }
            .start()
        arrowView.setImageResource(
            if (isVisible) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
        )
    }

    // --- ДІАЛОГ ВИХОДУ ---
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this) // Стиль поки закоментував
            .setTitle("Підтвердження дії")
            .setMessage("Ви дійсно бажаєте вийти з облікового запису?")
            .setPositiveButton("Вийти") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    // --- TOAST ---
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}