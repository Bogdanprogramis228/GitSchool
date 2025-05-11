package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.R

class GenreAdapter(
    private val genres: List<Pair<String, Int>>, // Список пар (назва жанру, ID зображення)
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<GenreAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val genreImageView: ImageView = itemView.findViewById(R.id.genreImageView)
        val genreTextView: TextView = itemView.findViewById(R.id.genreTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (genreName, genreImageId) = genres[position]
        holder.genreTextView.text = genreName
        holder.genreImageView.setImageResource(genreImageId)

        holder.itemView.setOnClickListener {
            onItemClick(genreName)
        }
    }

    override fun getItemCount(): Int = genres.size
}