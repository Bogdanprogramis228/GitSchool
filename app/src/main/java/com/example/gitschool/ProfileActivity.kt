package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        // Кнопка «Назад»
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        val accountSettings = findViewById<LinearLayout>(R.id.accountSetings)
        accountSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }



        // Оновлюємо локальні дані користувача
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
            setupUI()
        } ?: setupUI()
    }

    private fun setupUI() {
        // Кнопка-стрілка для розкривання «Інформація про акаунт»
        val accountInfoArrow: ImageView = findViewById(R.id.accountInfoArrow)
        val accountInfoDetails: LinearLayout = findViewById(R.id.accountInfoDetails)

        accountInfoArrow.setOnClickListener {
            if (accountInfoDetails.visibility == View.GONE) {
                accountInfoDetails.visibility = View.VISIBLE
                accountInfoArrow.setImageResource(R.drawable.ic_arrow_up)
            } else {
                accountInfoDetails.visibility = View.GONE
                accountInfoArrow.setImageResource(R.drawable.ic_arrow_down)
            }
        }

        // Отримуємо поточного користувача
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        // Посилання на поля у верстці
        val userName: TextView = findViewById(R.id.userName)
        val userEmail: TextView = findViewById(R.id.userEmail)
        val userPassword: TextView = findViewById(R.id.userPassword)
        val userRegistrationDate: TextView = findViewById(R.id.userRegistrationDate)
        val userLastLogin: TextView = findViewById(R.id.userLastLogin)

        // Встановлюємо email (тільки саме значення, без «Пошта:»)
        userEmail.text = user?.email ?: "Немає"

        // Дата реєстрації
        val regDateValue = user?.metadata?.creationTimestamp ?: 0L
        if (regDateValue > 0) {
            val formattedRegDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(regDateValue)
            userRegistrationDate.text = formattedRegDate  // тільки дата
        } else {
            userRegistrationDate.text = "Невідомо"
        }

        // Останній вхід
        val lastLoginValue = user?.metadata?.lastSignInTimestamp ?: 0L
        if (lastLoginValue > 0) {
            val formattedLastLogin = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(lastLoginValue)
            userLastLogin.text = formattedLastLogin
        } else {
            userLastLogin.text = "Невідомо"
        }

        // Зчитуємо з Firestore ім’я та пароль
        val db = FirebaseFirestore.getInstance()
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val nameFromDb = document.getString("name") ?: "Немає"
                    val passwordFromDb = document.getString("password") ?: "Немає"

                    // Ім’я у верхній частині
                    userName.text = nameFromDb
                    // Пароль (тільки саме значення)
                    userPassword.text = passwordFromDb
                }
                .addOnFailureListener {
                    userName.text = "Помилка (ім’я)"
                    userPassword.text = "Помилка (пароль)"
                }
        }

        // Кнопка «Вийти з акаунту» з підтвердженням
        val logoutItem: LinearLayout = findViewById(R.id.logoutItem)
        logoutItem.setOnClickListener {
            android.app.AlertDialog.Builder(this, R.style.CustomDialogStyle02)
                .setTitle("Підтвердження дії")
                .setMessage("Підтвердьте вихід із облікового запису.")
                .setPositiveButton("Підтвердити") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Назад", null)
                .show()
        }
    }
}
