package com.example.gitschool

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log // Додано для логування
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.suspendCoroutine

@Suppress("DEPRECATION")
class EditProfileActivity : AppCompatActivity() {

    private var initialBackgroundLoaded: Boolean = false
    private var initialAvatarLoaded: Boolean = false

    // UI Елементи
    private lateinit var userNameEditText: TextView
    private lateinit var backArrow: ImageView
    private lateinit var avatarImage: ImageView
    private lateinit var userNameTv: TextView
    private lateinit var editProfileBackgroundImage: ImageView
    private lateinit var avatarUploadPreview: ImageView
    private lateinit var backgroundUploadPreview: ImageView
    private lateinit var buttonSaveChanges: Button
    private lateinit var saveProgressIndicator: ProgressBar
    private lateinit var uploadProgressBar: ProgressBar // ProgressBar для завантаження

    // Firebase
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance("https://gitschool-9eede-default-rtdb.europe-west1.firebasedatabase.app/").reference


    // Коди запитів та дозволів
    private val REQUEST_CODE_PICK_AVATAR = 101
    private val REQUEST_CODE_PICK_BACKGROUND = 102
    private val REQUEST_CODE_PERMISSIONS = 103
    private var pendingActionRequestCode: Int? = null
    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE
    private val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    // Змінні стану
    private var initialAvatarBase64Hash: Int? = null
    private var initialBackgroundBase64Hash: Int? = null
    private var avatarChanged = false
    private var backgroundChanged = false
    private var isSaving = false
    private var isStaticAvatarSelected = false
    private var isStaticBackgroundSelected = false

    // Зберігаємо вибрані зображення або ресурси
    private var selectedAvatarBitmap: Bitmap? = null
    private var selectedBackgroundBitmap: Bitmap? = null
    private var selectedStaticAvatarId: Int? = null
    private var selectedStaticBackgroundId: Int? = null

    // Для відстеження виділення
    private var selectedAvatarView: View? = null
    private var selectedBackgroundView: View? = null
    private val defaultBackgroundResId = android.R.color.transparent
    private val selectedBackgroundResId = R.drawable.selected_option_border

    // ID ресурсів для статичних опцій
    private val staticAvatarOptionIds = mapOf(
        R.id.avatar_option1 to R.drawable.ava_01,
        R.id.avatar_option2 to R.drawable.ava_02,
        R.id.avatar_option3 to R.drawable.ava_03,
        R.id.avatar_option4 to R.drawable.ava_04
    )
    private val staticBackgroundOptionIds = mapOf(
        R.id.background_option1 to R.drawable.back_01,
        R.id.background_option2 to R.drawable.back_02,
        R.id.background_option3 to R.drawable.back_03,
        R.id.background_option4 to R.drawable.back_04
    )

    // Зберігаємо ID вибраних елементів
    private var selectedAvatarId: Any? = null
    private var selectedBackgroundId: Any? = null

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()

        // Ініціалізація UI
        userNameEditText = findViewById(R.id.editProfile_userName)
        backArrow = findViewById(R.id.editProfile_backArrow)
        avatarImage = findViewById(R.id.editProfile_avatarImage)
        userNameTv = findViewById(R.id.editProfile_userName)
        editProfileBackgroundImage = findViewById(R.id.editProfile_backgroundImage)
        avatarUploadPreview = findViewById(R.id.avatar_upload_background)
        backgroundUploadPreview = findViewById(R.id.background_upload_background)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)
        saveProgressIndicator = findViewById(R.id.saveProgressIndicator)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)
        uploadProgressBar.visibility = View.GONE

        loadUserName()

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        loadCurrentProfileData()

        findViewById<View>(R.id.avatar_upload).setOnClickListener {
            if (isSaving) return@setOnClickListener
            handleSelection(it, true)
            checkAndRequestPermission(REQUEST_CODE_PICK_AVATAR)
        }
        findViewById<View>(R.id.background_upload).setOnClickListener {
            if (isSaving) return@setOnClickListener
            handleSelection(it, false)
            checkAndRequestPermission(REQUEST_CODE_PICK_BACKGROUND)
        }

        staticAvatarOptionIds.forEach { (viewId, drawableId) ->
            findViewById<View>(viewId)?.setOnClickListener { view ->
                handleSelection(view, true, drawableId)
            }
        }
        staticBackgroundOptionIds.forEach { (viewId, drawableId) ->
            findViewById<View>(viewId)?.setOnClickListener { view ->
                handleSelection(view, false, drawableId)
            }
        }

        buttonSaveChanges.setOnClickListener {
            if (isSaving) return@setOnClickListener
            saveChanges()
        }

        updateSaveChangesButtonState()
    }

    private fun loadUserName() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val nameFromDb = document.getString("name") ?: ""
                    userNameEditText.text = nameFromDb
                    Log.d("LoadUserName", "User name loaded: $nameFromDb")
                }
                .addOnFailureListener { e ->
                    userNameEditText.text = "Помилка завантаження"
                    Log.e("LoadUserName", "Failed to load user name", e)
                }
        }
    }

    private fun handleSelection(selectedView: View, isAvatar: Boolean, staticDrawableId: Int? = null) {
        // Скидання попереднього вибору
        if (isAvatar) {
            selectedAvatarView?.setBackgroundResource(defaultBackgroundResId)
        } else {
            selectedBackgroundView?.setBackgroundResource(defaultBackgroundResId)
        }

        if (staticDrawableId != null) {
            // Якщо вибирається статичне зображення
            selectedView.setBackgroundResource(selectedBackgroundResId)
            if (isAvatar) {
                selectedAvatarView = selectedView
                avatarChanged = true
                selectedStaticAvatarId = staticDrawableId
                // Очистити, якщо нове зображення завантаження з галереї
                selectedAvatarBitmap = null
                Glide.with(this)
                    .load(staticDrawableId)
                    .placeholder(R.drawable.ava_01)
                    .circleCrop()
                    .into(avatarImage)
                selectedAvatarId = staticDrawableId
            } else {
                selectedBackgroundView = selectedView
                backgroundChanged = true
                selectedStaticBackgroundId = staticDrawableId
                selectedBackgroundBitmap = null
                Glide.with(this)
                    .load(staticDrawableId)
                    .placeholder(R.drawable.back_01)
                    .centerCrop()
                    .into(editProfileBackgroundImage)
                selectedBackgroundId = staticDrawableId
            }
        } else {
            // Якщо завантажується з галереї – встановлюємо режим "upload"
            if (isAvatar) {
                avatarChanged = true
                selectedAvatarBitmap = null // або залиш новий bitmap після onActivityResult
                selectedStaticAvatarId = null
                selectedAvatarId = "upload"
            } else {
                backgroundChanged = true
                selectedBackgroundBitmap = null // або новий bitmap
                selectedStaticBackgroundId = null
                selectedBackgroundId = "upload"
            }
        }
        updateSaveChangesButtonState()
    }


    private fun loadCurrentProfileData() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        userNameTv.text = user.displayName ?: "Гість"

        // Завантаження аватара:
        database.child("users").child(uid).child("avatarBase64").get()
            .addOnSuccessListener { snapshot ->
                val avatarBase64 = snapshot.getValue(String::class.java)
                if (!avatarBase64.isNullOrEmpty()) {
                    initialAvatarBase64Hash = avatarBase64.hashCode()
                    try {
                        val imageBytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(this)
                            .load(bitmap)
                            .placeholder(R.drawable.ava_01)
                            .circleCrop()
                            .into(avatarImage)
                        Glide.with(this)
                            .load(bitmap)
                            .placeholder(R.drawable.ava_01)
                            .circleCrop()
                            .into(avatarUploadPreview)
                        Log.d("LoadProfile", "Avatar loaded from Base64, size: ${bitmap.width}x${bitmap.height}")
                    } catch (e: Exception) {
                        Log.e("LoadProfile", "Error decoding avatarBase64", e)
                    }
                } else {
                    // Якщо Base64 порожній, перевіряємо ресурсний ID
                    database.child("users").child(uid).child("avatarResourceId").get()
                        .addOnSuccessListener { resourceIdSnapshot ->
                            val resourceId = resourceIdSnapshot.getValue(Int::class.java)
                            if (resourceId != null) {
                                selectedStaticAvatarId = resourceId
                                Glide.with(this)
                                    .load(resourceId)
                                    .placeholder(R.drawable.ava_01)
                                    .circleCrop()
                                    .into(avatarImage)
                                Log.d("LoadProfile", "Avatar loaded from resourceId: $resourceId")
                            } else {
                                avatarImage.setImageResource(R.drawable.ava_01)
                                avatarUploadPreview.setImageResource(R.drawable.ic_download)
                                Log.d("LoadProfile", "Avatar resourceId is null, default used")
                            }
                        }
                        .addOnFailureListener { error ->
                            Log.e("LoadProfile", "Failed to load avatarResourceId", error)
                            avatarImage.setImageResource(R.drawable.ava_01)
                            avatarUploadPreview.setImageResource(R.drawable.ic_download)
                        }
                }
                initialAvatarLoaded = true
            }
            .addOnFailureListener { error ->
                Log.e("LoadProfile", "Failed to load avatarBase64", error)
                initialAvatarLoaded = true
            }

        // Завантаження фону:
        database.child("users").child(uid).child("backgroundBase64").get()
            .addOnSuccessListener { snapshot ->
                val backgroundBase64 = snapshot.getValue(String::class.java)
                if (!backgroundBase64.isNullOrEmpty()) {
                    initialBackgroundBase64Hash = backgroundBase64.hashCode()
                    try {
                        val imageBytes = Base64.decode(backgroundBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Glide.with(this)
                            .load(bitmap)
                            .placeholder(R.drawable.back_01)
                            .centerCrop()
                            .into(editProfileBackgroundImage)
                        Glide.with(this)
                            .load(bitmap)
                            .placeholder(R.drawable.back_01)
                            .centerCrop()
                            .into(backgroundUploadPreview)
                        Log.d("LoadProfile", "Background loaded from Base64, size: ${bitmap.width}x${bitmap.height}")
                    } catch (e: Exception) {
                        Log.e("LoadProfile", "Error decoding backgroundBase64", e)
                    }
                } else {
                    database.child("users").child(uid).child("backgroundResourceId").get()
                        .addOnSuccessListener { resourceIdSnapshot ->
                            val resourceId = resourceIdSnapshot.getValue(Int::class.java)
                            if (resourceId != null) {
                                selectedStaticBackgroundId = resourceId
                                Glide.with(this)
                                    .load(resourceId)
                                    .placeholder(R.drawable.back_01)
                                    .centerCrop()
                                    .into(editProfileBackgroundImage)
                                Log.d("LoadProfile", "Background loaded from resourceId: $resourceId")
                            } else {
                                editProfileBackgroundImage.setImageResource(R.drawable.back_01)
                                backgroundUploadPreview.setImageResource(R.drawable.ic_download)
                                Log.d("LoadProfile", "Background resourceId is null, default used")
                            }
                        }
                        .addOnFailureListener { error ->
                            Log.e("LoadProfile", "Failed to load backgroundResourceId", error)
                            editProfileBackgroundImage.setImageResource(R.drawable.back_01)
                            backgroundUploadPreview.setImageResource(R.drawable.ic_download)
                        }
                }
                initialBackgroundLoaded = true
            }
            .addOnFailureListener { error ->
                Log.e("LoadProfile", "Failed to load backgroundBase64", error)
                initialBackgroundLoaded = true
            }

        avatarChanged = false
        backgroundChanged = false
        updateSaveChangesButtonState()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val startOnActivityResult = System.currentTimeMillis()
        Log.d("OnActivityResult", "Початок onActivityResult: $startOnActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            val imageUri = data.data!!
            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                Log.d("OnActivityResult", "Зображення декодовано успішно, розмір: ${bitmap.width}x${bitmap.height}")
                if (bitmap != null) {
                    when (requestCode) {
                        REQUEST_CODE_PICK_AVATAR -> {
                            showToast("Завантаження аватара розпочато...")
                            lifecycleScope.launch {
                                val localSuccess = withContext(Dispatchers.IO) {
                                    Log.d("ProfileUpload", "Початок saveImageToLocalStorage для аватара, requestCode: $requestCode")
                                    saveImageToLocalStorage(bitmap, "avatar.jpg")
                                }
                                if (localSuccess) {
                                    Log.d("ProfileUpload", "Локальне збереження аватара завершено")
                                    val rtdbSuccess = withContext(Dispatchers.IO) {
                                        Log.d("ProfileUpload", "Початок saveImageToRtdb для аватара, requestCode: $requestCode")
                                        saveImageToRtdb(bitmap, true)
                                    }
                                    withContext(Dispatchers.Main) {
                                        if (rtdbSuccess) {
                                            Glide.with(this@EditProfileActivity)
                                                .load(bitmap)
                                                .placeholder(R.drawable.ava_01)
                                                .circleCrop()
                                                .into(avatarImage)
                                            Glide.with(this@EditProfileActivity)
                                                .load(bitmap)
                                                .placeholder(R.drawable.ava_01)
                                                .circleCrop()
                                                .into(avatarUploadPreview)
                                            showToast("Аватар успішно оновлено!")
                                            Log.d("ProfileUpload", "Завершення saveImageToRtdb для аватара")
                                            val endOnActivityResult = System.currentTimeMillis()
                                            Log.d("OnActivityResult", "onActivityResult для аватара завершено, загальний час: ${endOnActivityResult - startOnActivityResult} мс")
                                        } else {
                                            showToast("Помилка оновлення аватара в базі даних.")
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        showToast("Помилка збереження аватара локально.")
                                    }
                                }
                            }
                        }
                        REQUEST_CODE_PICK_BACKGROUND -> {
                            showToast("Завантаження фону розпочато...")
                            lifecycleScope.launch {
                                val localSuccess = withContext(Dispatchers.IO) {
                                    Log.d("ProfileUpload", "Початок saveImageToLocalStorage для фону, requestCode: $requestCode")
                                    saveImageToLocalStorage(bitmap, "background.jpg")
                                }
                                if (localSuccess) {
                                    Log.d("ProfileUpload", "Локальне збереження фону завершено")
                                    val rtdbSuccess = withContext(Dispatchers.IO) {
                                        Log.d("ProfileUpload", "Початок saveImageToRtdb для фону, requestCode: $requestCode")
                                        saveImageToRtdb(bitmap, false)
                                    }
                                    withContext(Dispatchers.Main) {
                                        if (rtdbSuccess) {
                                            Glide.with(this@EditProfileActivity)
                                                .load(bitmap)
                                                .placeholder(R.drawable.back_01)
                                                .centerCrop()
                                                .into(editProfileBackgroundImage)
                                            Glide.with(this@EditProfileActivity)
                                                .load(bitmap)
                                                .placeholder(R.drawable.back_01)
                                                .centerCrop()
                                                .into(backgroundUploadPreview)
                                            showToast("Фон успішно оновлено!")
                                            Log.d("ProfileUpload", "Завершення saveImageToRtdb для фону")
                                            val endOnActivityResult = System.currentTimeMillis()
                                            Log.d("OnActivityResult", "onActivityResult для фону завершено, загальний час: ${endOnActivityResult - startOnActivityResult} мс")
                                        } else {
                                            showToast("Помилка оновлення фону в базі даних.")
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        showToast("Помилка збереження фону локально.")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    showToast("Не вдалося декодувати зображення")
                }
            } catch (e: Exception) {
                showToast("Помилка обробки зображення: ${e.message}")
                Log.e("ImageProcessing", "Error handling image result", e)
            }
        }
    }

    private fun saveChanges() {
        Log.d("EditProfile", "saveChanges() викликано, avatarChanged: $avatarChanged, backgroundChanged: $backgroundChanged")
        val startSaveChanges = System.currentTimeMillis()
        if (!avatarChanged && !backgroundChanged) {
            showToast("Немає змін для збереження")
            return
        }

        showLoading(true)

        val saveAvatarJob: Deferred<Boolean>? = if (avatarChanged) {
            lifecycleScope.async(Dispatchers.IO) {
                when {
                    selectedAvatarBitmap != null -> {
                        Log.d("EditProfile", "saveChanges(): збереження завантаженого аватара.")
                        saveImageToRtdb(selectedAvatarBitmap!!, true)
                    }
                    selectedStaticAvatarId != null -> {
                        Log.d("EditProfile", "saveChanges(): збереження ID статичного аватара: $selectedStaticAvatarId")
                        saveStaticResourceIdToRtdb(selectedStaticAvatarId!!, true)
                    }
                    else -> true
                }
            }
        } else null

        val saveBackgroundJob: Deferred<Boolean>? = if (backgroundChanged) {
            lifecycleScope.async(Dispatchers.IO) {
                when {
                    selectedBackgroundBitmap != null -> {
                        Log.d("EditProfile", "saveChanges(): збереження завантаженого фону.")
                        saveImageToRtdb(selectedBackgroundBitmap!!, false)
                    }
                    selectedStaticBackgroundId != null -> {
                        Log.d("EditProfile", "saveChanges(): збереження ID статичного фону: $selectedStaticBackgroundId")
                        saveStaticResourceIdToRtdb(selectedStaticBackgroundId!!, false)
                    }
                    else -> true
                }
            }
        } else null

        lifecycleScope.launch(Dispatchers.Main) {
            val avatarSaveResult = saveAvatarJob?.await() ?: true
            val backgroundSaveResult = saveBackgroundJob?.await() ?: true
            val endSaveChanges = System.currentTimeMillis()
            Log.d("EditProfile", "saveChanges() завершено за ${endSaveChanges - startSaveChanges} мс")
            showLoading(false)
            if (avatarSaveResult && backgroundSaveResult) {
                showToast("Зміни успішно збережено!")
                finish()
            } else {
                showToast("Сталася помилка під час збереження.")
            }
        }
    }

    private suspend fun saveImageToRtdb(bitmap: Bitmap, isAvatar: Boolean): Boolean {
        return suspendCoroutine { continuation ->
            val user = auth.currentUser ?: return@suspendCoroutine continuation.resumeWith(Result.success(false))
            val uid = user.uid
            val fieldName = if (isAvatar) "avatarBase64" else "backgroundBase64"
            val resizeLength = 256
            Log.d("EditProfile", "saveImageToRtdb(isAvatar=$isAvatar, fieldName=$fieldName) викликано.")

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val scaledBitmap = resizeBitmap(bitmap, resizeLength)
                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
                    val imageBytes = outputStream.toByteArray()
                    val base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

                    withContext(Dispatchers.Main) {
                        showUploadProgress()
                    }
                    val startUpload = System.currentTimeMillis()
                    Log.d("ProfileUpload", "Початок завантаження до Firebase для $fieldName: $startUpload")
                    database.child("users").child(uid).child(fieldName).setValue(base64String)
                        .addOnCompleteListener { task ->
                            val endUpload = System.currentTimeMillis()
                            Log.d("ProfileUpload", "Завершення завантаження до Firebase для $fieldName: $endUpload, час завантаження: ${endUpload - startUpload} мс")
                            lifecycleScope.launch(Dispatchers.Main) {
                                hideUploadProgress()
                                if (task.isSuccessful) {
                                    if (isAvatar) {
                                        initialAvatarBase64Hash = base64String.hashCode()
                                        avatarChanged = true
                                    } else {
                                        initialBackgroundBase64Hash = base64String.hashCode()
                                        backgroundChanged = true
                                    }
                                    continuation.resumeWith(Result.success(true))
                                } else {
                                    Log.e("ProfileUpload", "Помилка завантаження до Firebase для $fieldName: ${task.exception?.message}")
                                    continuation.resumeWith(Result.success(false))
                                }
                            }
                        }
                } catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        hideUploadProgress()
                        Log.e("ProfileUpload", "Exception під час saveImageToRtdb", e)
                        continuation.resumeWith(Result.success(false))
                    }
                }
            }
        }
    }

    private suspend fun saveStaticResourceIdToRtdb(resourceId: Int, isAvatar: Boolean): Boolean {
        Log.d("TestLog", "saveStaticResourceIdToRtdb викликано")
        val deferred = CompletableDeferred<Boolean>()
        val user = auth.currentUser ?: run {
            deferred.complete(false)
            return deferred.await()
        }
        val uid = user.uid
        // Поле для збереження ID статичного зображення
        val resourceFieldName = if (isAvatar) "avatarResourceId" else "backgroundResourceId"
        // Поле з Base64 – буде очищено, якщо встановлено статичне зображення
        val base64FieldName = if (isAvatar) "avatarBase64" else "backgroundBase64"
        Log.d("FirebaseSave", "saveStaticResourceIdToRtdb: uid=$uid, fieldName=$resourceFieldName, resourceId=$resourceId")

        val startUpload = System.currentTimeMillis()
        Log.d("ProfileUpload", "Початок завантаження до Firebase для статичного ресурсу $resourceFieldName: $startUpload")

        // Зберігаємо статичний ресурс (ID)
        database.child("users").child(uid).child(resourceFieldName).setValue(resourceId)
            .addOnCompleteListener { task ->
                val endUpload = System.currentTimeMillis()
                Log.d("ProfileUpload", "Завершення завантаження до Firebase для статичного $resourceFieldName: $endUpload, час завантаження: ${endUpload - startUpload} мс")
                lifecycleScope.launch(Dispatchers.Main) {
                    if (task.isSuccessful) {
                        // Очищуємо відповідне поле з Base64, щоб система більше не використовувала старе завантажене фото
                        database.child("users").child(uid).child(base64FieldName).setValue(null)
                            .addOnCompleteListener { clearTask ->
                                if (clearTask.isSuccessful) {
                                    Log.d("FirebaseSave", "$base64FieldName очищено, тому використовується статичний ресурс")
                                } else {
                                    Log.e("FirebaseSave", "Помилка очищення $base64FieldName: ${clearTask.exception?.message}")
                                }
                            }
                        if (isAvatar) {
                            initialAvatarBase64Hash = null
                            selectedStaticAvatarId = resourceId
                            avatarChanged = true
                        } else {
                            initialBackgroundBase64Hash = null
                            selectedStaticBackgroundId = resourceId
                            backgroundChanged = true
                        }
                        deferred.complete(true)
                    } else {
                        Log.e("FirebaseSave", "Помилка збереження $resourceFieldName: ${task.exception?.message}")
                        deferred.complete(false)
                    }
                }
            }
        Log.d("FirebaseSave", "addOnCompleteListener додано для $resourceFieldName")
        return deferred.await()
    }


    private fun updateSaveChangesButtonState() {
        val hasChanges = avatarChanged || backgroundChanged
        buttonSaveChanges.isEnabled = hasChanges
        buttonSaveChanges.alpha = if (hasChanges) 1.0f else 0.5f
    }

    private fun showLoading(loading: Boolean) {
        isSaving = loading
        saveProgressIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        buttonSaveChanges.isEnabled = !loading
        backArrow.isEnabled = !loading
        findViewById<View>(R.id.avatar_upload).isEnabled = !loading
        findViewById<View>(R.id.background_upload).isEnabled = !loading
        staticAvatarOptionIds.keys.forEach { findViewById<View>(it)?.isEnabled = !loading }
        staticBackgroundOptionIds.keys.forEach { findViewById<View>(it)?.isEnabled = !loading }
    }

    override fun onBackPressed() {
        if (isSaving) {
            showToast("Будь ласка, зачекайте завершення збереження.")
        } else {
            super.onBackPressed()
        }
    }

    private fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        return try {
            if (source.height <= maxLength && source.width <= maxLength) {
                source
            } else {
                val ratio: Float = source.width.toFloat() / source.height.toFloat()
                val targetWidth: Int
                val targetHeight: Int
                if (ratio > 1) {
                    targetWidth = maxLength
                    targetHeight = (maxLength / ratio).toInt()
                } else {
                    targetHeight = maxLength
                    targetWidth = (maxLength * ratio).toInt()
                }
                Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            source
        }
    }

    private fun checkAndRequestPermission(actionRequestCode: Int) {
        when {
            ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery(actionRequestCode)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, storagePermission) -> {
                showRationaleDialog(actionRequestCode)
            }
            else -> {
                pendingActionRequestCode = actionRequestCode
                ActivityCompat.requestPermissions(this, permissionsToRequest, REQUEST_CODE_PERMISSIONS)
            }
        }
    }

    private fun openGallery(requestCode: Int) {
        pendingActionRequestCode = null
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        try {
            startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            showToast("Не вдалося знайти додаток для вибору зображення.")
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            val actionCode = pendingActionRequestCode
            pendingActionRequestCode = null

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (actionCode != null) {
                    openGallery(actionCode)
                } else {
                    showToast("Дозвіл надано. Натисніть кнопку завантаження ще раз.")
                }
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, storagePermission)) {
                    showSettingsDialog()
                } else {
                    showToast("Дозвіл на доступ до галереї потрібен для завантаження зображень.")
                }
            }
        }
    }

    private fun showRationaleDialog(actionRequestCode: Int) {
        AlertDialog.Builder(this)
            .setTitle("Потрібен дозвіл")
            .setMessage("Для завантаження власного аватара чи фону додатку потрібен доступ до вашої галереї.")
            .setPositiveButton("Надати дозвіл") { _, _ ->
                pendingActionRequestCode = actionRequestCode
                ActivityCompat.requestPermissions(this, permissionsToRequest, REQUEST_CODE_PERMISSIONS)
            }
            .setNegativeButton("Скасувати") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Дозвіл відхилено")
            .setMessage("Ви заборонили доступ до галереї. Увімкніть дозвіл у налаштуваннях додатку, щоб завантажувати зображення.")
            .setPositiveButton("Налаштування") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Скасувати") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // Допоміжна функція для збереження зображення у внутрішнє сховище з вимірюванням часу
    private suspend fun saveImageToLocalStorage(bitmap: Bitmap, fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val startLocal = System.currentTimeMillis()
                Log.d("ProfileUpload", "Початок збереження локального файлу: $startLocal")
                openFileOutput(fileName, MODE_PRIVATE).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                val endLocal = System.currentTimeMillis()
                Log.d("ProfileUpload", "Збереження локального файлу завершено за ${endLocal - startLocal} мс")
                true
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast("Помилка збереження локального зображення.")
                }
                false
            }
        }
    }

    private fun showUploadProgress() {
        uploadProgressBar.visibility = View.VISIBLE
    }

    private fun hideUploadProgress() {
        uploadProgressBar.visibility = View.GONE
    }

    private fun updateUploadProgress(progress: Int) {
        uploadProgressBar.progress = progress
    }
}
