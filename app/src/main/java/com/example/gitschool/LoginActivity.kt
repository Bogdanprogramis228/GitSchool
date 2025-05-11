package com.example.gitschool

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Налаштування Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val emailField = findViewById<EditText>(R.id.editTextLoginEmail)
        val passwordField = findViewById<EditText>(R.id.editTextLoginPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val googleLoginButton = findViewById<LinearLayoutCompat>(R.id.buttonGoogleLogin)
        val loginMessage = findViewById<TextView>(R.id.textLoginMessage)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            // --- Валідація полів (залишається без змін) ---
            if (email.isEmpty() || password.isEmpty()) {
                showErrorMessage(loginMessage, "Введіть свою пошту та пароль!")
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showErrorMessage(loginMessage, "Невірне введення пошти. Спробуйте ще раз.")
                return@setOnClickListener
            }
            if (password.length !in 6..20) { // Або ваші правила довжини
                showErrorMessage(loginMessage, "Пароль має містити від 6 до 20 символів.")
                return@setOnClickListener
            }
            // -------------------------------------------

            // --- Вхід через Email/Password ---
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Вхід успішний
                        val user = auth.currentUser
                        if (user != null) {

                            // --- ЗМІНА ЛОГІКИ ТУТ ---
                            // 1. Спочатку перевіряємо, чи це адміністратор
                            if (user.email == "admin@domain.com") {
                                // Якщо це адмін, одразу переходимо в AdminActivity
                                Log.d("LoginActivity", "Admin user logged in. Skipping email verification check.")
                                startActivity(Intent(this, AdminActivity::class.java))
                                finish() // Закриваємо LoginActivity
                                return@addOnCompleteListener // Важливо вийти тут, щоб не виконувати подальші перевірки
                            }

                            // 2. Якщо це НЕ адмін, перевіряємо підтвердження пошти
                            if (!user.isEmailVerified) {
                                // Якщо пошта НЕ підтверджена - надсилаємо лист повторно
                                Log.w("LoginActivity", "User email not verified: ${user.email}")
                                user.sendEmailVerification()
                                    .addOnSuccessListener {
                                        showErrorMessage(loginMessage, "Будь ласка, підтвердьте свою пошту! Лист повторно надіслано на: ${user.email}")
                                    }
                                    .addOnFailureListener { e ->
                                        showErrorMessage(loginMessage, "Не вдалося відправити лист підтвердження: ${e.message}")
                                    }
                                // Не переходимо далі, користувач має підтвердити пошту
                                return@addOnCompleteListener
                            }

                            // 3. Якщо це звичайний користувач І пошта підтверджена
                            Log.d("LoginActivity", "Regular user logged in and verified: ${user.email}")
                            storeUserDataIfNotExist(user) // Зберігаємо дані, якщо потрібно
                            startActivity(Intent(this, SecondActivity::class.java))
                            finish() // Закриваємо LoginActivity
                            // --- КІНЕЦЬ ЗМІНИ ЛОГІКИ ---

                        } else {
                            // Малоймовірно, але обробимо випадок, коли user == null після успішного task
                            Log.e("LoginActivity", "Login successful but currentUser is null.")
                            showErrorMessage(loginMessage, "Помилка отримання даних користувача.")
                        }
                    } else {
                        // Вхід НЕ успішний
                        // FirebaseFirestore.setLoggingEnabled(true) // Увімкнення логування Firestore тут недоречне
                        Log.e("LoginActivity", "Login failed: ${task.exception?.message}")
                        // Показуємо більш загальну помилку, щоб не розкривати деталі
                        showErrorMessage(loginMessage, "Неправильна пошта або пароль.")
                    }
                }
        }

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else {
                Log.e("LoginActivity", "Google login cancelled")
                showErrorMessage(loginMessage, "Вхід через Google скасовано")
            }
        }

        googleLoginButton.setOnClickListener {
            // Виконуємо sign out, щоб змусити показати вибір акаунту
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
                Log.d("LoginActivity", "Google sign-in initiated with account selection")
            }
        }

        findViewById<TextView>(R.id.textNeedackount).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            Log.d("LoginActivity", "Navigate to RegisterActivity")
        }
        findViewById<TextView>(R.id.textNeedHelp).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/support"))
            startActivity(intent)
            Log.d("LoginActivity", "Navigate to support URL")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Закрити програму
        finishAffinity()
    }


    // Якщо документ користувача ще не існує у колекції "users", створити його
    private fun storeUserDataIfNotExist(user: FirebaseUser) {
        val docRef = db.collection("users").document(user.uid)
        docRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val userData = hashMapOf(
                    "name" to (user.displayName ?: ""),
                    "email" to (user.email ?: "")
                )
                docRef.set(userData)
                    .addOnSuccessListener { Log.d("LoginActivity", "User data stored in Firestore.") }
                    .addOnFailureListener { e -> Log.e("LoginActivity", "Error storing user data: ${e.message}") }
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account == null) {
                Log.e("LoginActivity", "Google sign-in failed: Account is null")
                showErrorMessage(findViewById(R.id.textLoginMessage), "Помилка входу в Google: акаунт не вибраний")
            } else {
                Log.d("LoginActivity", "Google sign-in successful: ${account.displayName}")
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google sign-in failed: ${e.message}")
            showErrorMessage(findViewById(R.id.textLoginMessage), "Помилка входу в Google: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d("LoginActivity", "User signed in with Google: ${user.displayName}")
                        storeUserDataIfNotExist(user)
                        // Для Google-користувача теж можна перевіряти isEmailVerified при потребі
                        startActivity(Intent(this, SecondActivity::class.java))
                        finish()
                    }
                } else {
                    Log.e("LoginActivity", "Firebase Google authentication failed: ${task.exception?.message}")
                    showErrorMessage(findViewById(R.id.textLoginMessage), "Помилка авторизації: ${task.exception?.message}")
                }
            }
    }

    private fun showErrorMessage(view: TextView, message: String) {
        view.text = message
        view.setTextColor(Color.RED)
        Log.e("LoginActivity", message)
    }

    // При натисканні поза полями вводу ховаємо клавіатуру
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}
