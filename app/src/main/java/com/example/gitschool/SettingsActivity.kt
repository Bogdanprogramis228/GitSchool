package com.example.gitschool

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText

    private lateinit var buttonUpdate: Button
    private lateinit var buttonDelete: Button

    private lateinit var buttonUnlinkGoogle: LinearLayoutCompat
    private lateinit var buttonLinkDiscord: LinearLayoutCompat
    private lateinit var buttonLinkTelegram: LinearLayoutCompat

    // Змінні для збереження первинних даних
    private var originalName: String = ""
    private var originalPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonUpdate = findViewById(R.id.buttonUpdate)
        buttonDelete = findViewById(R.id.buttonDelete)
        buttonUnlinkGoogle = findViewById(R.id.buttonUnlinkGoogle)
        buttonLinkDiscord = findViewById(R.id.buttonLinkDiscord)
        buttonLinkTelegram = findViewById(R.id.buttonLinkTelegram)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        currentUser = auth.currentUser!!

        setupEditText(editTextName)
        setupEditText(editTextPassword)

        loadUserData()

        buttonUpdate.setOnClickListener {
            showConfirmationDialog("оновлення") { updateUserData() }
        }

        buttonDelete.setOnClickListener {
            showConfirmationDialog("видалення") { deleteUserAccount() }
        }

        buttonUnlinkGoogle.setOnClickListener {
            if (currentUser.providerData.any { it.providerId == "google.com" }) {
                Toast.makeText(this, "Google акаунт не можна відв'язати.", Toast.LENGTH_SHORT).show()
            }
        }
        buttonLinkDiscord.setOnClickListener { linkDiscord() }
        buttonLinkTelegram.setOnClickListener { linkTelegram() }

        findViewById<TextView>(R.id.settings_back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isClickable = true
        editText.setOnClickListener {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            showKeyboard(editText)
        }
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(view)
                view.isFocusable = false
            }
        }
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadUserData() {
        editTextEmail.setText(currentUser.email)
        editTextEmail.isEnabled = false

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                // Завантажуємо ім'я з бази або використовуємо displayName з Firebase
                val nameFromDb = document.getString("name") ?: currentUser.displayName ?: ""
                editTextName.setText(nameFromDb)
                originalName = nameFromDb

                // Перевіряємо, чи користувач увійшов через Google
                val isGoogleUser = currentUser.providerData.any { it.providerId == "google.com" }
                if (isGoogleUser) {
                    // Для Google-користувача пароль спочатку відсутній
                    editTextPassword.setText("")
                    editTextPassword.hint = "Введіть пароль"
                    originalPassword = ""
                } else {
                    val storedPassword = document.getString("password") ?: ""
                    editTextPassword.setText(storedPassword)
                    originalPassword = storedPassword
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Не вдалося завантажити дані", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserData() {
        val newName = editTextName.text.toString().trim()
        val newPassword = editTextPassword.text.toString().trim()

        val nameChanged = newName != originalName
        val passwordChanged = newPassword.isNotEmpty() && newPassword != originalPassword

        if (!nameChanged && !passwordChanged) {
            Toast.makeText(this, "Дані не змінено", Toast.LENGTH_SHORT).show()
            return
        }

        // Перевірка мінімальної довжини (тільки якщо дані змінюються)
        if (nameChanged && newName.length < 3) {
            Toast.makeText(this, "Ім'я повинно містити щонайменше 3 символи", Toast.LENGTH_SHORT).show()
            return
        }
        if (passwordChanged && newPassword.length < 6) {
            Toast.makeText(this, "Пароль повинен містити щонайменше 6 символів", Toast.LENGTH_SHORT).show()
            return
        }

        // Якщо зміни стосуються імені, оновлюємо його
        if (nameChanged) {
            db.collection("users").document(currentUser.uid)
                .update("name", newName)
                .addOnSuccessListener {
                    originalName = newName
                    Toast.makeText(this, "Ім'я оновлено!", Toast.LENGTH_SHORT).show()
                    // Якщо крім імені змінився ще й пароль, запускаємо оновлення пароля після успіху оновлення імені
                    if (passwordChanged) {
                        updatePassword(newPassword)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка при оновленні імені: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else if (passwordChanged) {
            // Якщо змінився тільки пароль
            updatePassword(newPassword)
        }
    }

    private fun updatePassword(newPassword: String) {
        val hasEmailProvider = currentUser.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
        if (!hasEmailProvider) {
            // Якщо користувач увійшов через Google і ще не має email/password, виконуємо зв'язування
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, newPassword)
            currentUser.linkWithCredential(credential)
                .addOnSuccessListener {
                    db.collection("users").document(currentUser.uid)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            originalPassword = newPassword
                            Toast.makeText(this, "Пароль встановлено!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Помилка при встановленні пароля: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка при встановленні пароля: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Якщо акаунт вже має email/password, оновлюємо пароль стандартним способом
            currentUser.updatePassword(newPassword)
                .addOnSuccessListener {
                    db.collection("users").document(currentUser.uid)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            originalPassword = newPassword
                            Toast.makeText(this, "Пароль змінено!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Помилка при оновленні пароля: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Помилка при зміні пароля: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteUserAccount() {
        db.collection("users").document(currentUser.uid).delete()
            .addOnSuccessListener {
                currentUser.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Акаунт видалено", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e -> Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
            .addOnFailureListener { e -> Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    private fun linkDiscord() {
        Toast.makeText(this, "Прив'язка Discord не реалізована", Toast.LENGTH_SHORT).show()
    }

    private fun linkTelegram() {
        Toast.makeText(this, "Прив'язка Telegram не реалізована", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun showConfirmationDialog(action: String, onConfirm: () -> Unit) {
        val message = if (action == "оновлення") "Підтвердіть оновлення даних." else "Підтвердіть видалення акаунту."
        AlertDialog.Builder(this, R.style.CustomDialogStyle02)
            .setTitle("Підтвердіть дію")
            .setMessage(message)
            .setPositiveButton("Підтвердити") { _, _ -> onConfirm() }
            .setNegativeButton("Назад", null)
            .show()

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
