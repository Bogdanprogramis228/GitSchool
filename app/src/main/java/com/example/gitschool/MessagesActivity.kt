package com.example.gitschool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.NotificationAdapter
import com.example.gitschool.data.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagesActivity : AppCompatActivity() {

    private lateinit var toolbar: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noNotificationsText: TextView
    private lateinit var notificationAdapter: NotificationAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerView = findViewById(R.id.recyclerNotifications)
        noNotificationsText = findViewById(R.id.textViewNoNotifications)

        toolbar = findViewById(R.id.messagesToolbar)
        // Налаштування Toolbar
        toolbar.setOnClickListener {
            finish()
        }

        setupRecyclerView()

        if (userId == null) {
            showToast("Помилка: користувач не авторизований")
            finish()
            return
        }
        loadNotifications(userId)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(mutableListOf(),
            onItemClick = { notification ->
                // TODO: Обробка кліку на сповіщення (наприклад, перехід на екран аніме)
                showToast("Натиснуто: ${notification.message}")
                // Позначити як прочитане (якщо ще не зроблено)
                if (!notification.isRead && notification.documentId != null) {
                    markNotificationAsRead(notification)
                }
            },
            onItemDelete = { notification ->
                // TODO: Показати діалог підтвердження перед видаленням
                if (notification.documentId != null) {
                    deleteNotification(notification)
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notificationAdapter
    }

    private fun loadNotifications(userId: String) {
        Log.d("MessagesActivity", "Loading notifications for user $userId")
        db.collection("users").document(userId).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Новіші зверху
            .limit(100) // Обмеження кількості
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("MessagesActivity", "No notifications found.")
                    noNotificationsText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    notificationAdapter.updateNotifications(emptyList()) // Очистити адаптер
                } else {
                    val notifications = mutableListOf<Notification>()
                    for (doc in snapshot.documents) {
                        try {
                            val notification = doc.toObject(Notification::class.java)
                            if (notification != null) {
                                notification.documentId = doc.id // Зберігаємо ID документа
                                notifications.add(notification)
                            }
                        } catch(e: Exception) {
                            Log.e("MessagesActivity", "Error parsing notification ${doc.id}", e)
                        }
                    }
                    Log.d("MessagesActivity", "Loaded ${notifications.size} notifications.")
                    notificationAdapter.updateNotifications(notifications)
                    noNotificationsText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivity", "Error loading notifications", e)
                showToast("Помилка завантаження повідомлень")
                noNotificationsText.text = "Помилка завантаження"
                noNotificationsText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
    }

    // --- Додаткові функції (позначка прочитання, видалення) ---

    private fun markNotificationAsRead(notification: Notification) {
        if (userId == null || notification.documentId == null) return
        Log.d("MessagesActivity", "Marking notification ${notification.documentId} as read")
        db.collection("users").document(userId)
            .collection("notifications").document(notification.documentId!!)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d("MessagesActivity", "Notification marked as read in Firestore.")
                // Оновлюємо UI для цього елемента (ховаємо індикатор)
                notification.isRead = true // Оновлюємо локальний об'єкт
                val position = (recyclerView.adapter as? NotificationAdapter)?.notifications?.indexOf(notification)
                if (position != null && position != -1) {
                    notificationAdapter.notifyItemChanged(position) // Оновлюємо лише змінений елемент
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivity", "Error marking notification as read", e)
            }
    }

    private fun deleteNotification(notification: Notification) {
        if (userId == null || notification.documentId == null) return
        Log.d("MessagesActivity", "Deleting notification ${notification.documentId}")

        // TODO: Додати діалог підтвердження AlertDialog.Builder...

        db.collection("users").document(userId)
            .collection("notifications").document(notification.documentId!!)
            .delete()
            .addOnSuccessListener {
                Log.d("MessagesActivity", "Notification deleted from Firestore.")
                showToast("Повідомлення видалено")
                // Видаляємо елемент з адаптера
                notificationAdapter.removeItem(notification)
                // Показуємо текст, якщо список став порожнім
                if (notificationAdapter.itemCount == 0) {
                    noNotificationsText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivity", "Error deleting notification", e)
                showToast("Помилка видалення повідомлення")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}