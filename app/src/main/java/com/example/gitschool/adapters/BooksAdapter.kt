package com.example.gitschool.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.R
import com.example.gitschool.data.BookItem

class BooksAdapter(
    private val items: List<BookItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val item = items[position]
        holder.textViewTitle.text = item.title
        holder.textViewDescription.text = item.description
        holder.textViewAuthor.text = item.author
        holder.textViewYear.text = item.year

        holder.itemView.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.pdfUrl))
            holder.itemView.context.startActivity(browserIntent)
        }

//        holder.itemView.setOnClickListener {
//            // Перевірка типу посилання перед відправленням
//            if (item.pdfUrl.contains("drive.google.com")) {
//                // Відкриття книги через Google Drive
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.pdfUrl))
//                intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://com.android.chrome"))
//                holder.itemView.context.startActivity(intent)
//            } else {
//                // Завантаження PDF файлу
//                val intent = Intent(holder.itemView.context, PdfViewerActivity::class.java)
//                intent.putExtra("PDF_URL", item.pdfUrl)
//                holder.itemView.context.startActivity(intent)
//            }
//        }
    }


    override fun getItemCount() = items.size

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
        val textViewAuthor: TextView = view.findViewById(R.id.textViewAuthor)
        val textViewYear: TextView = view.findViewById(R.id.textViewYear)
    }
}
