package com.example.gitschool

import android.os.Bundle
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gitschool.adapters.FaqExpandableListAdapter

class AboutActivity : AppCompatActivity() {

    private lateinit var toolbar: TextView
    private lateinit var faqListView: ExpandableListView
    private lateinit var faqAdapter: FaqExpandableListAdapter

    private val questions = listOf(
        "Як змінити ім'я користувача?",
        "Як змінити пароль?",
        "Як змінити адресу електронної пошти?",
        "Як змінити аватарку чи фон?",
        "Що робити, якщо я забув пароль?",
        "Чому немає серій аніме для перегляду?",
        "Чому місцями немає перекладу аніме?"
    )

    private val answers = listOf(
        "Перейдіть у налаштування додатку та змініть ім’я користувача у відповідному полі. Для підтвердження натисніть - Оновити дані",
        "Перейдіть у налаштування додатку та змініть пароль користувача у відповідному полі. Для підтвердження натисніть - Оновити дані",
        "Неможливо змініть електронну адресу на нову! Можна видалити акаунт та створити новий із новою електроною адресою.",
        "На сторінці профілю натисніть на кнопку зпрва вгорі ерану та на сторінці редагування профілю натиснути на аватарку або фон, щоб вибрати нове зображення та підтвердити вибір.",
        "Пароль можна побачити на сторінці профілю в розділі - Інформація про акаунт.",
        "Перегляд аніме не можливий через відсутність авторських прав на перегляд аніме. Перевірте будь ласка пізніше.",
        "Деякі місця ще в перекладі. Ми постійно працюємо над додаванням українських текстів."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        faqListView = findViewById(R.id.expandableListViewFaq)
        faqAdapter = FaqExpandableListAdapter(this, questions, answers)
        faqListView.setAdapter(faqAdapter)

        // опціонально розгорнути всі запитання:
//        for (i in 0 until faqAdapter.groupCount) {
//             faqListView.expandGroup(i)
//         }
        
        toolbar = findViewById(R.id.textViewAboutTitle)
        toolbar.setOnClickListener {
            finish()
        }
    }

}
