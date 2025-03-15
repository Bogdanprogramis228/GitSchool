package com.example.gitschool

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.data.HomeworkTask
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class HomeworkActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeworkAdapter
    private var taskList: MutableList<HomeworkTask> = mutableListOf()
    private lateinit var searchView: SearchView
    private lateinit var statusFilter: Spinner
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homework)

        val toolbarTextView: TextView = findViewById(R.id.toolbar)
        // Додаємо стрілку ліворуч (якщо її немає, встановлюється drawable)
        toolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.strilka, 0, 0, 0)
        // При натисканні на заголовок повертаємося назад
        toolbarTextView.setOnClickListener {
            onBackPressed()
        }

        recyclerView = findViewById(R.id.recycler_view_homework)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HomeworkAdapter(taskList) { task, action ->
            when (action) {
                "edit" -> showTaskDetails(task)
                "delete" -> deleteTask(task)
            }
        }
        recyclerView.adapter = adapter

        // Налаштовуємо SearchView
        searchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // При підтвердженні пошуку ховаємо клавіатуру і знімаємо фокус
                searchView.clearFocus()
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return false
            }
        })
        // Якщо поле втратить фокус, ховаємо клавіатуру
        searchView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) hideKeyboard()
        }
        // Якщо користувач натискає поза межами SearchView (на контейнері RecyclerView), знімаємо фокус
        recyclerView.setOnTouchListener { _, _ ->
            searchView.clearFocus()
            false
        }

        // Налаштовуємо Spinner для фільтрації за статусом
        statusFilter = findViewById(R.id.status_filter)
        val statuses = arrayOf("Всі", "Буду робити", "Роблю", "Зроблено")
        val adapterStatus = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        statusFilter.adapter = adapterStatus
        statusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = statuses[position]
                adapter.filterByStatus(if (selectedStatus == "Всі") null else selectedStatus)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<FloatingActionButton>(R.id.fab_add_task).setOnClickListener {
            showTaskDialog(null)
        }

        loadTasksFromFirestore()
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showTaskDialog(task: HomeworkTask?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.edit_task_title)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.edit_task_description)
        val statusSpinner = dialogView.findViewById<Spinner>(R.id.status_spinner)

        val statuses = arrayOf("Буду робити", "Роблю", "Зроблено")
        statusSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        if (task != null) {
            titleInput.setText(task.title)
            descriptionInput.setText(task.description)
            statusSpinner.setSelection(statuses.indexOf(task.status))
        }

        AlertDialog.Builder(this)
            .setTitle(if (task == null) "Нове завдання" else "Редагування завдання")
            .setView(dialogView)
            .setPositiveButton("Зберегти") { _, _ ->
                // Логіка збереження завдання
                val titleText = titleInput.text.toString().trim()
                val descriptionText = descriptionInput.text.toString().trim()
                val statusText = statusSpinner.selectedItem.toString()

                if (titleText.isEmpty() || descriptionText.isEmpty()) {
                    Toast.makeText(this, "Назва та опис не можуть бути порожніми", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Отримуємо поточну дату
                val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                val newTask = HomeworkTask(
                    id = task?.id ?: db.collection("homework_tasks").document().id,
                    title = titleText,
                    description = descriptionText,
                    date = currentDate,
                    userId = userId,
                    status = statusText
                )
                if (task == null) {
                    taskList.add(newTask)
                } else {
                    val index = taskList.indexOf(task)
                    if (index != -1) {
                        taskList[index] = newTask
                    }
                }
                saveTaskToFirestore(newTask) {
                    loadTasksFromFirestore() // Завантажуємо дані після збереження
                }
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun saveTaskToFirestore(task: HomeworkTask, onSuccess: () -> Unit = {}) {
        db.collection("homework_tasks").document(task.id).set(task)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Не вдалося зберегти завдання: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTasksFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("homework_tasks")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Завдання не знайдено", Toast.LENGTH_SHORT).show()
                } else {
                    taskList.clear()
                    for (document in documents) {
                        document.toObject(HomeworkTask::class.java)?.let { taskList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Не вдалося завантажити завдання: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showTaskDetails(task: HomeworkTask) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task_details, null)

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogStyle02)
            .setView(dialogView)
            .create()

        // Прив'язка до елементів
        val btnClose = dialogView.findViewById<TextView>(R.id.btn_close)
        val taskTitle = dialogView.findViewById<TextView>(R.id.task_title)
        val taskDescription = dialogView.findViewById<TextView>(R.id.task_description)
        val taskDate = dialogView.findViewById<TextView>(R.id.task_date)
        val taskStatus = dialogView.findViewById<TextView>(R.id.task_status)
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)
        val btnEdit = dialogView.findViewById<Button>(R.id.btn_edit)

        // Заповнення даними
        taskTitle.text = task.title
        taskDescription.text = "Завданння: ${task.description}"
        taskDate.text = "Дата: ${task.date}"
        taskStatus.text = "Статус: ${task.status}"

        // Клік на "Закрити"
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Клік на "Редагувати"
        btnEdit.setOnClickListener {
            dialog.dismiss()
            showTaskDialog(task) // Відкрити діалог редагування
        }

        // Клік на "Видалити"
        btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteTask(task) // Видалення завдання
        }

        // Показати діалог
        dialog.show()

    }


    private fun deleteTask(task: HomeworkTask) {
        db.collection("homework_tasks").document(task.id).delete()
            .addOnSuccessListener {
                taskList.remove(task)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Не вдалося видалити завдання: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
