package com.example.gitschool

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.LibraryAdapter
import com.example.gitschool.data.BookItem
import com.example.gitschool.data.LibraryItem

class LibraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        // Назад при натисканні на стрілку
        findViewById<TextView>(R.id.toolbar).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLibrary)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 колонки
        recyclerView.adapter = LibraryAdapter(getLibraryItems()) { selectedClass ->
            // Отримуємо список книг для вибраного класу
            val books = getBooksForClass(selectedClass)

            // Створюємо Intent для переходу до BooksActivity
            val intent = Intent(this, BooksActivity::class.java)
            intent.putExtra("class_name", selectedClass)  // Передаємо ім'я класу
            intent.putParcelableArrayListExtra("books_list", ArrayList(books))  // Передаємо список книг
            startActivity(intent)
        }
    }

    private fun getLibraryItems(): List<LibraryItem> {
        return listOf(
            LibraryItem(
                "1 клас",
                "https://i.pinimg.com/736x/78/bf/bc/78bfbc43080562b499e2b4a9ffab51e2.jpg",
                ""
            ),
            LibraryItem(
                "2 клас",
                "https://i.pinimg.com/736x/12/bf/80/12bf8076278db4782a16e29824166250.jpg",
                ""
            ),
            LibraryItem(
                "3 клас",
                "https://i.pinimg.com/736x/da/e3/45/dae3452354be862db5362a82db4db15b.jpg",
                ""
            ),
            LibraryItem(
                "4 клас",
                "https://i.pinimg.com/736x/67/8c/9f/678c9f173ba1c6f2151b8137d8e87d11.jpg",
                ""
            ),
            LibraryItem(
                "5 клас",
                "https://i.pinimg.com/736x/4d/5d/0a/4d5d0a05ae9abf124b2f045aebee64f5.jpg",
                ""
            ),
            LibraryItem(
                "6 клас",
                "https://i.pinimg.com/736x/3b/a7/60/3ba7606336da3abadd4a7529fa587543.jpg",
                ""
            ),
            LibraryItem(
                "7 клас",
                "https://i.pinimg.com/736x/1c/41/a1/1c41a1ebca2d6c1818655d13b71839c3.jpg",
                ""
            ),
            LibraryItem(
                "8 клас",
                "https://i.pinimg.com/736x/98/95/92/98959208fa1f4d6b58a9d2b9636b485b.jpg",
                ""
            ),

            LibraryItem(
                "9 клас",
                "https://i.pinimg.com/736x/7c/2a/5b/7c2a5b6303d13ee8cc937486bd556f85.jpg",
                ""
            ),
            LibraryItem(
                "10 клас",
                "https://i.pinimg.com/736x/c9/d0/a2/c9d0a288dae5d1bca3e94d66c20a31f9.jpg",
                ""
            ),
            LibraryItem(
                "11 клас",
                "https://i.pinimg.com/736x/47/3d/e2/473de227fedd11dbf28f9a949b966f38.jpg",
                ""
            ),
            // Додайте інші класи аналогічно
        )
    }

    private fun getBooksForClass(className: String): List<BookItem> {
        return when (className) {
            "1 клас" -> listOf(
                BookItem("Математика", "Підручник для 1 класу закладів загальної середньої освіти.", "Скворцова С.О.", "2018", "https://lib.imzo.gov.ua/wa-data/public/site/books/Pidruchnuk-1kl-2018-pdf/07_Matematyka_1kl/Matematyka_pidruchnyk%20dlia%201%20klasu%20ZZSO%20(Skvortsova%20S.%20O.,%20Onopriienko%20O.%20V.)%20.pdf"),
                BookItem("Англійська мова", "Підручник для 1 класу закладів загальної середньої освіти.", "Карпюк О.Д.", "2018", "https://lib.imzo.gov.ua/wa-data/public/site/books/Pidruchnuk-1kl-2018-pdf/03_Inozemnamova_english_1kl/KarpyukOD/English_1_2018_Aston.pdf"),
                BookItem("Українська мова. Буквар І частина", "Підручник для 1 класу закладів загальної середньої освіти.", "Вашуленко М. С.", "2019", "https://lib.imzo.gov.ua/wa-data/public/site/books/Pidruchnuk-1kl-2018-pdf/01_Ukr_mova_Bukvar_1kl/VashulenkoMS/Bukvar_1kl_ukr.pdf"),
                BookItem("Українська мова. Буквар ІI частина", "Підручник для 1 класу закладів загальної середньої освіти.", "Вашуленко М. С.", "2020", "https://lib.imzo.gov.ua/wa-data/public/site/books/Pidruchnuk-1kl-2018-pdf/01_Ukr_mova_Bukvar_1kl/VashulenkoMS/BUKVAR_1-KL_VASH_CH-2_.pdf"),
                BookItem("Я досліджую світ І частина", "Підручник для 1 класу закладів загальної середньої освіти.", "Н.Бібік", "2016", "https://drive.google.com/uc?export=download&id=1qfQWpqKhQiA7ubfpCrC1eZumJCHl_ZaA"),
                BookItem("Я досліджую світ ІI частина", "Підручник для 1 класу закладів загальної середньої освіти.", "Н.Бібік", "2018", "https://drive.google.com/uc?export=download&id=1mV68Jx7guOJCwmpFO2RM9dpkkotdYEHj"),
                BookItem("Мистецтво", "Підручник для 1 класу закладів загальної середньої освіти.", "Масол Л.М.", "2004 ", "https://drive.google.com/uc?export=download&id=1MdcbdngZYOYiRAYgaUBR-cyAU6RmS7Nw")
            )
            "2 клас" -> listOf(
                BookItem("Українська мова та читання, І частина", "Опис книги", "Вашуленко М.С.", "2020", "https://lib.imzo.gov.ua/wa-data/public/site/books2/pidruchnyky-2-klas-2019/01-ukrainska-mova-ta-chytannya-2-klas/ukr-mova-2-kl-vashulenko-dubovyk-vashulenko/2-kl-vashylenko-ukr-mova-blok.pdf"),
                BookItem("Українська мова та читання, ІI частина", "Опис книги", "Вашуленко М.С.", "2017", "https://lib.imzo.gov.ua/wa-data/public/site/books2/pidruchnyky-2-klas-2019/01-ukrainska-mova-ta-chytannya-2-klas/ukr-mova-2-kl-vashulenko-dubovyk-vashulenko/2-klas-literatyrne-chutannya-blok.pdf"),
                BookItem("Англійська мова", "Опис книги", "Губарєва С.С., Павліченко О.М.", "2018", "https://lib.imzo.gov.ua/wa-data/public/site/books2/pidruchnyky-2-klas-2019/03-inozemna-mova-angliyska-mova-2-klas/angliyska-mova-2-klas-gubarjeva-pavlichenko-zalyubovska/Anhliiska%20mova%20pidruchnyk%20dlia%202%20klasu%20ZZSO%20(z%20audiosuprovodom)(Hubarieva%20S.%20S.,%20Pavlichenko%20O.%20M.,%20Zaliubovska%20L.%20V.).pdf"),
                BookItem("Математика", "Опис книги", "Скворцова С.О.", "2018", "https://lib.imzo.gov.ua/wa-data/public/site/books2/pidruchnyky-2-klas-2019/07-matematyka-2-klas/matymatyka-pidruchnyk-dlia-2-klasu-zzso-skvortsova-s-o-onopriienko-o-v.pdf"),
                BookItem("Я досліджую світ, І частина", "Опис книги", "Бібік Н.М.", "2016", "https://lib.imzo.gov.ua/wa-data/public/site/books2/pidruchnyky-2-klas-2019/08-ya-doslidzhuyu-svit-2-klas/yads-2-klas-bibikbondarchuk-kornijenko-ta-inshi/ya-doslidzhuiu-svit-pidruchnyk-dlia-2-klasu-zzso-u-2-chastynakh-chastyna-1-avt-bibik-n-m-bondarchuk-h-p.pdf"),
                BookItem("Я досліджую світ, ІІ частина", "Опис книги", "Корнієнко М.М., Крамаровська С.М.", "2014", "https://lib.imzo.gov.ua/wa-data/public/site/books2/pidruchnyky-2-klas-2019/08-ya-doslidzhuyu-svit-2-klas/yads-2-klas-bibikbondarchuk-kornijenko-ta-inshi/Ya%20doslidzhuiu%20svit_pidruchnyk%20dlia%202%20klasu%20ZZSO%20(u%202%20chastynakh)%20(Chastyna%202%20avt.%20Korniienko%20M.%20M.,%20Kramarovska%20S.%20M.,%20Zaretska%20I.%20T.).pdf")
            )
            "3 клас" -> listOf(
                BookItem("Англійська мова", "Опис книги", "Автор", "2021", "")
            )
            "4 клас" -> listOf(
                BookItem("Англійська мова", "Опис книги", "Автор", "2021", "")
            )
            // Додати інші класи
            else -> emptyList()
        }
    }
}
