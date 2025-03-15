package com.example.gitschool

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.NewsAdapter
import com.example.gitschool.adapters.TeacherAdapter
import com.example.gitschool.data.NewsParser
import com.example.gitschool.models.TeacherItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jsoup.Jsoup

@Suppress("DEPRECATION")
class SecondActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // RecyclerView для новин, фото та вчителів
    private lateinit var horizontalRecyclerView1: RecyclerView  // новини
    private lateinit var horizontalRecyclerView2: RecyclerView  // фото
    private lateinit var horizontalRecyclerView3: RecyclerView  // вчителі

    private lateinit var btnProfile: TextView
    private lateinit var userNameTextView: TextView

    // Заголовки розділів (TextView)
    private lateinit var titleAnnouncements: TextView  // для новин
    private lateinit var titleEvents: TextView         // для фото (наприклад, "Фотографії")
    private lateinit var titleCatalog: TextView        // для вчителів ("Педагогічний колектив.")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Ініціалізація елементів з макету
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

        // Отримуємо заголовок навігаційного меню та елемент для відображення імені користувача
        val headerView = navigationView.getHeaderView(0)
        userNameTextView = headerView.findViewById(R.id.user_name)

        // Завантажуємо ім'я користувача
        loadUserName()

        // Налаштування Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.toolbar_menu)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Переходи між активностями
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val btnSchedule = findViewById<TextView>(R.id.btn_schedule)
        btnSchedule.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }


        val btnbookmarks = findViewById<TextView>(R.id.btn_bookmarks)
        btnbookmarks.setOnClickListener {
            val intent = Intent(this, HomeworkActivity::class.java)
            startActivity(intent)
        }

        // Приклад у вашій Activity (наприклад, MainActivity)
        val settingsIcon: ImageButton = findViewById(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        val btnGrades: TextView = findViewById(R.id.btn_seasons)

        btnGrades.setOnClickListener {
            // Створюємо список класів
            val classes = arrayOf("1-А", "1-Б", "2-А", "2-Б", "3-А", "3-Б", "4-А", "4-Б")

            // Мапа класів та їх відповідних URL
            val classUrls = mapOf(
                "1-А" to "https://drive.google.com/file/d/17zR0ZDkQ_RiZuj7nRdv2On-3aWEmaT0A/view?usp=sharing",
                "1-Б" to "https://drive.google.com/file/d/your_file_id_2/view",
                "2-А" to "https://drive.google.com/file/d/your_file_id_3/view",
                "2-Б" to "https://drive.google.com/file/d/your_file_id_4/view",
                "3-А" to "https://drive.google.com/file/d/your_file_id_5/view",
                "3-Б" to "https://drive.google.com/file/d/your_file_id_6/view",
                "4-А" to "https://drive.google.com/file/d/your_file_id_7/view",
                "4-Б" to "https://drive.google.com/file/d/your_file_id_8/view"
            )

            // Створюємо AlertDialog для вибору класу
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Виберіть клас")
            builder.setItems(classes) { dialog, which ->
                // При виборі класу, отримуємо URL для цього класу
                val selectedClass = classes[which]
                val classUrl = classUrls[selectedClass]

                // Перевіряємо, чи є URL для вибраного класу
                if (classUrl != null) {
                    // Створюємо Intent для відкриття браузера з відповідним журналом
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(classUrl))
                    startActivity(intent)
                } else {
                    // Якщо URL для класу не знайдено, можна показати помилку
                    Toast.makeText(this, "URL для цього класу не знайдено", Toast.LENGTH_SHORT).show()
                }
            }

            // Показуємо AlertDialog
            builder.show()
        }


        // Налаштування горизонтальних списків
        horizontalRecyclerView1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalRecyclerView3.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Завантаження новин (перший список)
        NewsParser.getNews { newsList ->
            runOnUiThread {
                titleAnnouncements.text = "Новини ліцею" // або інший потрібний заголовок
                val newsAdapter = NewsAdapter(
                    newsList = newsList,
                    onNewsClick = { clickedNews ->
                        val fullUrl = "https://school-yablounic.e-schools.info" + clickedNews.link
                        NewsParser.getNewsDetails(fullUrl) { detailDescription ->
                            runOnUiThread {
                                if (detailDescription.isNotEmpty() && clickedNews.title.isNotEmpty()) {
                                    Log.d(
                                        "NewsDetails",
                                        "Title: ${clickedNews.title}, Description: $detailDescription"
                                    )
                                    showNewsDialog(clickedNews.title, detailDescription)
                                } else {
                                    showToast("Не вдалося завантажити деталі новини.")
                                }
                            }
                        }
                    }
                )
                horizontalRecyclerView1.adapter = newsAdapter
            }
        }

        // Завантаження фото (другий список)
        loadPhotos()

        // Завантаження вчителів (третій список)
        loadTeachers()

        // Обробка кліків у навігаційному меню
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> showToast("Головна")
                R.id.nav_events -> showToast("Заходи та події")
                R.id.nav_news -> showToast("Оголошення")
                R.id.nav_library -> {
                    val intent = Intent(this, LibraryActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_cafeteria -> {
                    val intent = Intent(this, CafeteriaActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_faceboock -> {
                    val url = "https://www.facebook.com/people/Яблуницька-Школа/100013408801256"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
                R.id.nav_contact -> {
                    val url = "https://school-yablounic.e-schools.info/feedback"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun loadPhotos() {
        // Завантаження фотоальбомів із головної сторінки
        Thread {
            try {
                val baseUrl = "https://school-yablounic.e-schools.info"
                val doc = Jsoup.connect(baseUrl).get()

                // Заголовок фото-розділу (наприклад, "Фотографії")
                val header = doc.select("div.grid_ttl_mr2 h2 a").text()

                // Отримання списку фото із <div class="index_ph_box">
                val photoElements =
                    doc.select("div.index_ph_box .index_ph_list_wrap #index_ph_list div.ph a img")
                val photoList = photoElements.map { it.absUrl("src") }

                runOnUiThread {
                    titleEvents.text = header
                    // Використовуємо PhotoAdapter для другого списку
                    val photoAdapter = com.example.gitschool.adapters.PhotoAdapter(photoList, supportFragmentManager)
                    horizontalRecyclerView2.adapter = photoAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showToast("Не вдалося завантажити фото.")
                }
            }
        }.start()
    }

    private fun loadTeachers() {
        // Завантаження інформації про вчителів із сторінки https://school-yablounic.e-schools.info/teachers
        Thread {
            try {
                val url = "https://school-yablounic.e-schools.info/teachers"
                val doc = Jsoup.connect(url).get()

                // Отримання заголовку каталогу вчителів із <title> – наприклад, "Педагогічний колектив."
                var catalogTitle = doc.select("title").text().trim()
                // Видаляємо крапку в кінці, якщо вона є
                if (catalogTitle.endsWith(".")) {
                    catalogTitle = catalogTitle.dropLast(1).trim()
                }

                // Отримання списку вчителів із блоку "teachers without category"
                // Приклад: кожен вчитель представлений у <div class="sch_ptbox_item">
                val teacherElements = doc.select("div.sch_ptbox_item")
                val teacherList = teacherElements.map { element ->
                    val photoUrl = element.select("div.photo_wrap a.photo img").attr("abs:src")
                    val name = element.select("div.name a").text()
                    val link = element.select("div.photo_wrap a.photo").attr("href")
                    TeacherItem(photoUrl, name, link)
                }

                runOnUiThread {
                    titleCatalog.text = catalogTitle
                    val teacherAdapter = TeacherAdapter(teacherList, supportFragmentManager)
                    horizontalRecyclerView3.adapter = teacherAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showToast("Не вдалося завантажити інформацію про вчителів.")
                }
            }
        }.start()
    }


    override fun onResume() {
        super.onResume()
        loadUserName()
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Діалог з описом новини (залишається без змін)
    private fun showNewsDialog(title: String, fullDescription: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_news_description, null)
        val titleView = dialogView.findViewById<TextView>(R.id.dialog_news_title)
        val descView = dialogView.findViewById<TextView>(R.id.dialog_news_description)

        titleView.text = title
        descView.text = fullDescription

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()
    }
}
