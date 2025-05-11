package com.example.gitschool

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var registerMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameField = findViewById<EditText>(R.id.editTextName)
        val emailField = findViewById<EditText>(R.id.editTextRegisterEmail)
        val passwordField = findViewById<EditText>(R.id.editTextRegisterPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        registerMessage = findViewById(R.id.textRegisterMessage)

        registerButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showErrorMessage("Введіть свої дані для реєстрації")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showErrorMessage("Пароль має бути не менше 6 символів")
                return@setOnClickListener
            }
            if (password.length > 20) {
                showErrorMessage("Пароль не може бути більше 20 символів")
                return@setOnClickListener
            }

            registerUser(name, email, password)
        }

        findViewById<TextView>(R.id.textNeedHelp).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/support"))
            startActivity(intent)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Зберігаємо дані користувача в Firestore використовуючи uid як документ
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email
                    )
                    user?.uid?.let { uid ->
                        db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "Користувач доданий у Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterActivity", "Помилка зберігання даних: ${e.message}")
                            }
                    }
                    user?.sendEmailVerification()?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            showVerificationDialog()
                        } else {
                            showErrorMessage("Не вдалося надіслати лист із підтвердженням.")
                        }
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        showErrorMessage("Цей email вже використовується!")
                    } else {
                        showErrorMessage("Помилка реєстрації: ${task.exception?.message}")
                    }
                }
            }
    }

    private fun showErrorMessage(message: String) {
        registerMessage.text = message
        registerMessage.setTextColor(resources.getColor(android.R.color.holo_red_light, theme))
        Log.e("RegisterActivity", message)
    }

    private fun showVerificationDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Підтвердження пошти")
            .setMessage("Реєстрація успішна! Перевірте електронну пошту для підтвердження.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .create()
        dialog.show()
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
