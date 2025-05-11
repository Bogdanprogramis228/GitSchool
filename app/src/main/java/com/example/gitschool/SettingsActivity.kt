package com.example.gitschool

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    // Персональні поля
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText

    // Секція «Загальне»
    private lateinit var switchPush: SwitchCompat
    private lateinit var switchAdult: SwitchCompat
    private lateinit var switchEmail: SwitchCompat

    // Кнопки
    private lateinit var buttonUpdate: Button
    private lateinit var buttonDelete: Button

    // Щоб відслідковувати попередні значення
    private var originalName: String = ""
    private var originalPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Ініціалізуємо Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // Якщо не залогінений — назад в логін
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        currentUser = auth.currentUser!!

        // Прив’язуємо всі view
        bindViews()

        // Робимо поля імені/пароля редагованими по кліку
        setupEditText(editTextName)
        setupEditText(editTextPassword)

        // Завантажуємо дані користувача та загальні налаштування
        loadUserData()
        loadGeneralSettings()

        // listeners
        buttonUpdate.setOnClickListener {
            showConfirmationDialog("оновлення") { updateUserData() }
        }
        buttonDelete.setOnClickListener {
            showConfirmationDialog("видалення") { deleteUserAccount() }
        }
        findViewById<TextView>(R.id.settings_back).setOnClickListener {
            finish()
        }

        // Зберігаємо перемикання секції «Загальне»
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users")
                .document(currentUser.uid)
                .update("pushNotifications", isChecked)
        }
        switchAdult.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users")
                .document(currentUser.uid)
                .update("adultContent", isChecked)
        }
        switchEmail.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users")
                .document(currentUser.uid)
                .update("emailNotifications", isChecked)
        }
    }

    private fun bindViews() {
        editTextName     = findViewById(R.id.editTextName)
        editTextEmail    = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)

        switchPush   = findViewById(R.id.switchPushNotifications)
        switchAdult  = findViewById(R.id.switchAdultContent)
        switchEmail  = findViewById(R.id.switchEmailNotifications)

        buttonUpdate = findViewById(R.id.buttonUpdate)
        buttonDelete = findViewById(R.id.buttonDelete)
    }

    private fun setupEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isClickable  = true
        editText.setOnClickListener {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            showKeyboard(editText)
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
                v.isFocusable = false
            }
        }
    }

    private fun showKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    private fun hideKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadUserData() {
        // email — тільки для читання
        editTextEmail.setText(currentUser.email)
        editTextEmail.isEnabled = false

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                // Ім’я
                val nameFromDb = doc.getString("name") ?: currentUser.displayName ?: ""
                editTextName.setText(nameFromDb)
                originalName = nameFromDb

                // Пароль
                val isGoogle = currentUser.providerData.any { it.providerId == "google.com" }
                if (isGoogle) {
                    editTextPassword.setText("")
                    editTextPassword.hint = "Введіть пароль"
                    originalPassword = ""
                } else {
                    val pwd = doc.getString("password") ?: ""
                    editTextPassword.setText(pwd)
                    originalPassword = pwd
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Не вдалося завантажити дані", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGeneralSettings() {
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                switchPush.isChecked   = doc.getBoolean("pushNotifications") ?: true
                switchAdult.isChecked  = doc.getBoolean("adultContent")       ?: false
                switchEmail.isChecked  = doc.getBoolean("emailNotifications") ?: false
            }
            .addOnFailureListener {
                // нічого страшного, лишимо дефолтні
            }
    }

    private fun updateUserData() {
        val newName = editTextName.text.toString().trim()
        val newPwd  = editTextPassword.text.toString().trim()

        val nameChanged = newName != originalName
        val pwdChanged  = newPwd.isNotEmpty() && newPwd != originalPassword

        if (!nameChanged && !pwdChanged) {
            Toast.makeText(this, "Дані не змінено", Toast.LENGTH_SHORT).show()
            return
        }
        if (nameChanged && newName.length < 3) {
            Toast.makeText(this, "Ім’я має бути ≥3 символів", Toast.LENGTH_SHORT).show()
            return
        }
        if (pwdChanged && newPwd.length < 6) {
            Toast.makeText(this, "Пароль має бути ≥6 символів", Toast.LENGTH_SHORT).show()
            return
        }

        if (nameChanged) {
            db.collection("users").document(currentUser.uid)
                .update("name", newName)
                .addOnSuccessListener {
                    originalName = newName
                    Toast.makeText(this, "Ім’я оновлено", Toast.LENGTH_SHORT).show()
                    if (pwdChanged) updatePassword(newPwd)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this,
                        "Помилка оновлення імені: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
        } else if (pwdChanged) {
            updatePassword(newPwd)
        }
    }

    private fun updatePassword(newPassword: String) {
        val hasEmail = currentUser.providerData.any {
            it.providerId == EmailAuthProvider.PROVIDER_ID
        }
        if (!hasEmail) {
            // зв’язуємо пароль із Google‑акаунтом
            val cred = EmailAuthProvider.getCredential(currentUser.email!!, newPassword)
            currentUser.linkWithCredential(cred)
                .addOnSuccessListener {
                    db.collection("users").document(currentUser.uid)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            originalPassword = newPassword
                            Toast.makeText(this, "Пароль встановлено", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this,
                        "Помилка встановлення пароля: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
        } else {
            currentUser.updatePassword(newPassword)
                .addOnSuccessListener {
                    db.collection("users").document(currentUser.uid)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            originalPassword = newPassword
                            Toast.makeText(this, "Пароль змінено", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this,
                        "Помилка зміни пароля: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteUserAccount() {
        db.collection("users").document(currentUser.uid)
            .delete()
            .addOnSuccessListener {
                currentUser.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Акаунт видалено", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    "Помилка: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmationDialog(action: String, onConfirm: ()->Unit) {
        val message = if (action == "оновлення")
            "Підтвердіть оновлення даних."
        else
            "Підтвердіть видалення акаунту."
        AlertDialog.Builder(this, R.style.CustomDialogStyle02)
            .setTitle("Підтвердіть дію")
            .setMessage(message)
            .setPositiveButton("Підтвердити") { _, _ -> onConfirm() }
            .setNegativeButton("Назад", null)
            .show()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentFocus?.let { v ->
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(v.windowToken, 0)
            v.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}
