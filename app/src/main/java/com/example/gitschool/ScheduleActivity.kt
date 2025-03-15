package com.example.gitschool

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.LessonAdapter
import com.example.gitschool.models.LessonItem
import java.util.*

@Suppress("DEPRECATION")
class ScheduleActivity : AppCompatActivity() {

    private lateinit var recyclerViewLessons: RecyclerView
    private lateinit var calendarView: CalendarView
    private var selectedClass: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Встановлюємо українську локаль, щоб місяці й інші дати відображались українською
        val locale = Locale("uk", "UA")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Налаштування Toolbar

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Налаштування RecyclerView
        recyclerViewLessons = findViewById(R.id.recyclerViewLessons)
        recyclerViewLessons.layoutManager = LinearLayoutManager(this)

        // Ініціалізація CalendarView
        calendarView = findViewById(R.id.calendarView)
        calendarView.firstDayOfWeek = Calendar.MONDAY
        // Встановлюємо поточну дату (в мілісекундах)
        calendarView.date = System.currentTimeMillis()

        // Відображення діалогу для вибору класу (діалог неможливо скасувати)
        showClassSelectionDialog()

        // Обробка зміни дати в CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (selectedClass != null) {
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                loadScheduleForSelectedClass(cal, selectedClass!!)
            }
        }

        // Завантаження розкладу для поточної дати (якщо клас вже обраний)
        val today = Calendar.getInstance()
        if (selectedClass != null) {
            loadScheduleForSelectedClass(today, selectedClass!!)
        }
    }

    /**
     * Відображаємо AlertDialog із списком класів.
     * Після вибору зберігаємо вибір і завантажуємо розклад для поточної дати.
     */
    private fun showClassSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_class, null)
        val listView = dialogView.findViewById<ListView>(R.id.listViewClasses)

        val classes = arrayOf("1-А клас", "1-Б клас", "2-А клас", "2-Б клас", "3-А клас", "3-Б клас", "4-А клас", "4-Б клас")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, classes)
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Діалог неможливо закрити без вибору класу
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            selectedClass = classes[position]
            val today = Calendar.getInstance()
            loadScheduleForSelectedClass(today, selectedClass!!)
            dialog.dismiss() // Закриваємо діалог після вибору класу
        }

        dialog.show()
    }


    /**
     * Завантажуємо розклад для вибраного класу на основі вибраної дати.
     * Якщо вибраний клас – "3-А клас", використовуємо готовий розклад, інакше виводимо повідомлення.
     */
    private fun loadScheduleForSelectedClass(date: Calendar, selectedClass: String) {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        val lessons = when (selectedClass) {
            "3-А клас" -> getScheduleFor3A(dayOfWeek)
            else -> listOf(LessonItem("", "Розклад відсутній для цього класу"))
        }
        recyclerViewLessons.adapter = LessonAdapter(lessons)
    }

    /**
     * Повертає список уроків для 3-А класу в залежності від дня тижня.
     * Понеділок - П'ятниця реалізовано згідно з вимогами; для вихідних – повідомлення "Вихідний".
     */
    private fun getScheduleFor3A(dayOfWeek: Int): List<LessonItem> {
        return when (dayOfWeek) {
            Calendar.MONDAY -> listOf(
                LessonItem("08:30 - 09:15", "ЯДС"),
                LessonItem("09:25 - 10:10", "Математика"),
                LessonItem("10:30 - 11:15", "Англійська мова"),
                LessonItem("11:35 - 12:20", "Українська мова"),
                LessonItem("12:35 - 13:20", "Фізкультура")
            )
            Calendar.TUESDAY -> listOf(
                LessonItem("08:30 - 09:15", "ЯДС"),
                LessonItem("09:25 - 10:10", "Математика"),
                LessonItem("10:30 - 11:15", "Українська мова"),
                LessonItem("11:35 - 12:20", "ЯДС"),
                LessonItem("12:35 - 13:20", "Мистецтво"),
                LessonItem("13:30 - 14:50", "Інформатика")
            )
            Calendar.WEDNESDAY -> listOf(
                LessonItem("08:30 - 09:15", "ЯДС"),
                LessonItem("09:25 - 10:10", "Математика"),
                LessonItem("10:30 - 11:15", "Українська мова"),
                LessonItem("11:35 - 12:20", "Фізкультура"),
                LessonItem("12:35 - 13:20", "Християнська етика")
            )
            Calendar.THURSDAY -> listOf(
                LessonItem("08:30 - 09:15", "ЯДС"),
                LessonItem("09:25 - 10:10", "Українська мова"),
                LessonItem("10:30 - 11:15", "Мистецтво"),
                LessonItem("11:35 - 12:20", "Англійська мова"),
                LessonItem("12:35 - 13:20", "Фізкультура"),
                LessonItem("13:30 - 14:50", "Виховна")
            )
            Calendar.FRIDAY -> listOf(
                LessonItem("08:30 - 09:15", "ЯДС"),
                LessonItem("09:25 - 10:10", "Математика"),
                LessonItem("10:30 - 11:15", "Українська мова"),
                LessonItem("11:35 - 12:20", "ЯДС"),
                LessonItem("12:35 - 13:20", "Англійська мова")
            )
            else -> listOf(LessonItem("", "Вихідний"))
        }
    }
}
