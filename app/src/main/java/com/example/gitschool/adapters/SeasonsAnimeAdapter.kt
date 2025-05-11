package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.Anime
import com.example.gitschool.R

class SeasonsAnimeAdapter(
    private var animeList: List<Anime>,
    private val onItemClick: (Anime) -> Unit
) : RecyclerView.Adapter<SeasonsAnimeAdapter.GenreAnimeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreAnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime_by_genre, parent, false)
        return GenreAnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreAnimeViewHolder, position: Int) {
        val anime = animeList[position]
        holder.bind(anime, onItemClick)
    }

    override fun getItemCount(): Int = animeList.size

    fun updateData(newList: List<Anime>) {
        animeList = newList
        notifyDataSetChanged()
    }

    class GenreAnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.animeImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.animeTitleTextView)
        private val synopsisTextView: TextView = itemView.findViewById(R.id.animeSynopsisTextView)
        private val episodesTextView: TextView = itemView.findViewById(R.id.animeEpisodesTextView)
        private val yearTextView: TextView = itemView.findViewById(R.id.animeYearTextView)

        fun bind(anime: Anime, onItemClick: (Anime) -> Unit) {
            titleTextView.text = anime.title ?: "Без назви"

            // Якщо є короткий опис – показуємо, інакше ховаємо
            if (!anime.synopsis.isNullOrEmpty()) {
                synopsisTextView.text = anime.synopsis
                synopsisTextView.visibility = View.VISIBLE
            } else {
                synopsisTextView.visibility = View.GONE
            }

            // Якщо є кількість епізодів (> 0)
            if (anime.episodes != null && anime.episodes > 0) {
                episodesTextView.text = "${anime.episodes} серій"
                episodesTextView.visibility = View.VISIBLE
            } else {
                episodesTextView.visibility = View.GONE
            }

            // Якщо є рік
            if (!anime.aired?.from.isNullOrEmpty()) {
                // Наприклад, можна витягти лише рік із дати
                yearTextView.text = "(${anime.aired?.from?.substring(0, 4)})"
                yearTextView.visibility = View.VISIBLE
            } else {
                yearTextView.visibility = View.GONE
            }

            // Завантаження зображення
            Glide.with(itemView.context)
                .load(anime.images?.jpg?.imageUrl)
                .placeholder(R.drawable.onepuch_fon)
                .error(R.drawable.onepuch_fon)
                .into(imageView)

            // Обробка кліку
            itemView.setOnClickListener {
                onItemClick(anime)
            }
        }
    }
}
