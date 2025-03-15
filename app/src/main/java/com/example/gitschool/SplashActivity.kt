package com.example.gitschool

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide() // Приховати ActionBar, якщо є

        setContentView(R.layout.activity_splash)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val handler = Handler(Looper.getMainLooper())
        Thread {
            for (i in 0..100) {
                Thread.sleep(40)
                handler.post { progressBar.progress = i }
            }
            checkUserExists()
        }.start()
    }

    private fun checkUserExists() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // Якщо користувача немає в Firebase Auth, переходимо на LoginActivity
            navigateToLogin()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(currentUser.uid)

        userDocRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    // Якщо користувач є у Firestore, відкриваємо головну сторінку
                    navigateToMain()
                } else {
                    // Якщо користувача немає у Firestore, його акаунт видалено → вихід із системи
                    auth.signOut()
                    navigateToLogin()
                }
            } else {
                // Якщо помилка запиту до Firestore, все одно перекидаємо на LoginActivity
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, SecondActivity::class.java))
        finish()
    }
}
