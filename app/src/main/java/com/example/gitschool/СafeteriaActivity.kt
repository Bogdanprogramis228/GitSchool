package com.example.gitschool

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.MenuAdapter
import com.example.gitschool.data.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class CafeteriaActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var menuList: MutableList<MenuItem>
    private lateinit var dateText: TextView
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private var currentDate: Calendar = Calendar.getInstance()

    // Список робочих днів українською
    private val daysOfWeek = listOf("Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця")
    private val MAX_DAYS_FORWARD = 5
    private val MIN_DAYS_BACK = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cafeteria_activity)

        db = FirebaseFirestore.getInstance()
        menuList = mutableListOf()
        menuAdapter = MenuAdapter(menuList)

        dateText = findViewById(R.id.dateText)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMenu)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = menuAdapter

        // Обробка кнопки "Назад" (верхня стрілка)
        val backButton: ImageButton = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            onBackPressed()
        }

        updateDateText()
        loadMenu()

        prevButton.setOnClickListener {
            changeDate(-1)
        }

        nextButton.setOnClickListener {
            changeDate(1)
        }
    }

    private fun changeDate(offset: Int) {
        val today = Calendar.getInstance()
        val newDate = currentDate.clone() as Calendar
        newDate.add(Calendar.DAY_OF_MONTH, offset)
        // Пропускаємо вихідні (якщо трапляються)
        while (isWeekend(newDate)) {
            newDate.add(Calendar.DAY_OF_MONTH, offset)
        }
        // Нормалізуємо обидві дати (щоб порівнювати лише дату, без часу)
        val todayZero = today.clone() as Calendar
        todayZero.set(Calendar.HOUR_OF_DAY, 0)
        todayZero.set(Calendar.MINUTE, 0)
        todayZero.set(Calendar.SECOND, 0)
        todayZero.set(Calendar.MILLISECOND, 0)

        val newDateZero = newDate.clone() as Calendar
        newDateZero.set(Calendar.HOUR_OF_DAY, 0)
        newDateZero.set(Calendar.MINUTE, 0)
        newDateZero.set(Calendar.SECOND, 0)
        newDateZero.set(Calendar.MILLISECOND, 0)

        val diffInDays = ((newDateZero.timeInMillis - todayZero.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        if (diffInDays < MIN_DAYS_BACK || diffInDays > MAX_DAYS_FORWARD) {
            // Якщо нова дата поза дозволеним діапазоном – нічого не робимо
            return
        }
        currentDate = newDate
        updateDateText()
        loadMenu()
    }

    private fun updateDateText() {
        // Отримуємо назву дня для відображення
        val dayName = when (currentDate.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> daysOfWeek[0]
            Calendar.TUESDAY -> daysOfWeek[1]
            Calendar.WEDNESDAY -> daysOfWeek[2]
            Calendar.THURSDAY -> daysOfWeek[3]
            Calendar.FRIDAY -> daysOfWeek[4]
            else -> "Вихідний"
        }
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("uk", "UA"))
        val formattedDate = dateFormat.format(currentDate.time)
        dateText.text = "$dayName, $formattedDate"
    }

    private fun loadMenu() {
        // Визначаємо назву документа за поточним днем (для робочих днів)
        val dayName = when (currentDate.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Понеділок"
            Calendar.TUESDAY -> "Віторок"
            Calendar.WEDNESDAY -> "Середа"
            Calendar.THURSDAY -> "Четвер"
            Calendar.FRIDAY -> "Пятниця"
            else -> ""
        }
        if (dayName.isEmpty()) {
            menuList.clear()
            menuAdapter.notifyDataSetChanged()
            return
        }
        db.collection("menu").document(dayName).get()
            .addOnSuccessListener { document ->
                menuList.clear()
                if (document.exists()) {
                    val items = document.get("item") as? List<Map<String, Any>>
                    items?.forEach { item ->
                        val dishName = item["dishName"] as? String ?: ""
                        val weight = item["weight"] as? String ?: ""
                        val price = item["price"] as? String ?: ""
                        menuList.add(MenuItem(dishName, weight, price))
                    }
                } else {
                    Log.d("CafeteriaActivity", "Документ для $dayName не знайдено")
                }
                menuAdapter.notifyDataSetChanged()
                Log.d("CafeteriaActivity", "Завантажено меню для $dayName: $menuList")
            }
            .addOnFailureListener { exception ->
                Log.e("CafeteriaActivity", "Помилка отримання документу: ", exception)
            }
    }

    private fun isWeekend(calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
    }
}
