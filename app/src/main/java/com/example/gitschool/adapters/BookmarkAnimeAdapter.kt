package com.example.gitschool.adapters

import android.util.Log
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

class BookmarkAnimeAdapter(
    private var animeList: List<Anime>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<BookmarkAnimeAdapter.BookmarkViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(anime: Anime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime_bookmark, parent, false)
        return BookmarkViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val currentAnime = animeList[position]

        Glide.with(holder.itemView.context)
            .load(currentAnime.images?.jpg?.imageUrl)
            .apply(RequestOptions().centerCrop())
            .into(holder.imageView)

        holder.titleTextView.text = currentAnime.title ?: "Без назви"

        // Обробка рейтингу
        if (currentAnime.score != null && currentAnime.score.toString() != "N/A") {
            holder.ratingTextView.text = currentAnime.score.toString()
            holder.ratingTextView.visibility = View.VISIBLE
        } else {
            holder.ratingTextView.visibility = View.GONE
        }

        // Обробка кількості серій
        if (currentAnime.episodes != null && currentAnime.episodes.toString() != "N/A") {
            holder.episodesTextView.text = "${currentAnime.episodes} серій"
            holder.episodesTextView.visibility = View.VISIBLE
        } else {
            holder.episodesTextView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(currentAnime)
        }
    }

    override fun getItemCount(): Int {
        Log.d("BookmarkAdapter", "getItemCount: ${animeList.size}")
        return animeList.size
    }

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.anime_image)
        val ratingTextView: TextView = itemView.findViewById(R.id.anime_rating)
        val episodesTextView: TextView = itemView.findViewById(R.id.anime_episodes)
        val titleTextView: TextView = itemView.findViewById(R.id.anime_title)
    }

}