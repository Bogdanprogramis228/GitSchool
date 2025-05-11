package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gitschool.Anime
import com.example.gitschool.R

class AnimeListByGenreAdapter(
    private val animeList: List<Anime>,
    private val onItemClick: (Anime) -> Unit
) : RecyclerView.Adapter<AnimeListByGenreAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImageView: ImageView = itemView.findViewById(R.id.animeImageView)
        val animeTitleTextView: TextView = itemView.findViewById(R.id.animeTitleTextView)
        val animeSynopsisTextView: TextView = itemView.findViewById(R.id.animeSynopsisTextView)
        val animeEpisodesTextView: TextView = itemView.findViewById(R.id.animeEpisodesTextView)
        val animeYearTextView: TextView = itemView.findViewById(R.id.animeYearTextView)
        val animeRatingTextView: TextView = itemView.findViewById(R.id.animeRatingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime_by_genre, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentAnime = animeList[position]
        holder.animeTitleTextView.text = currentAnime.title ?: "Без назви"

        // Відображення опису, якщо є
        if (!currentAnime.synopsis.isNullOrEmpty()) {
            holder.animeSynopsisTextView.text = currentAnime.synopsis
            holder.animeSynopsisTextView.visibility = View.VISIBLE
        } else {
            holder.animeSynopsisTextView.visibility = View.GONE
        }

        // Відображення кількості серій, якщо є
        if (currentAnime.episodes != null && currentAnime.episodes > 0) {
            holder.animeEpisodesTextView.text = "${currentAnime.episodes} серій"
            holder.animeEpisodesTextView.visibility = View.VISIBLE
        } else {
            holder.animeEpisodesTextView.visibility = View.GONE
        }

        val year: String? = currentAnime.aired?.from?.substringBefore("-")?.trim()
        if (!year.isNullOrEmpty() && year != "null") {
            holder.animeYearTextView.text = "($year)"
            holder.animeYearTextView.visibility = View.VISIBLE
        } else {
            holder.animeYearTextView.visibility = View.GONE
        }

        // Відображення рейтингу, якщо є
        if (currentAnime.score != null && currentAnime.score > 0) {
            holder.animeRatingTextView.text = String.format("%.1f", currentAnime.score)
            holder.animeRatingTextView.visibility = View.VISIBLE
        } else {
            holder.animeRatingTextView.visibility = View.GONE
        }

        Glide.with(holder.itemView.context)
            .load(currentAnime.images?.jpg?.imageUrl)
            .apply(RequestOptions().centerCrop())
//            .placeholder(R.drawable.placeholder)
//            .error(R.drawable.error) // Переконайтеся, що R.drawable.error існує
            .into(holder.animeImageView)

        holder.itemView.setOnClickListener {
            onItemClick(currentAnime)
        }
    }

    override fun getItemCount(): Int = animeList.size
}