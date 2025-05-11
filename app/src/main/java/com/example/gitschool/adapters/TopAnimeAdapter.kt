package com.example.gitschool.adapters

import com.example.gitschool.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gitschool.Anime

class TopAnimeAdapter(
    private val animeList: List<Anime>,
    private val onItemClick: (Anime) -> Unit
) : RecyclerView.Adapter<TopAnimeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImage: ImageView = itemView.findViewById(R.id.anime_image)
        val animeRating: TextView = itemView.findViewById(R.id.anime_rating)
        val animeTitle: TextView = itemView.findViewById(R.id.anime_title)
        val animeEpisodes: TextView = itemView.findViewById(R.id.anime_episodes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_anime, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentAnime = animeList[position]

        Glide.with(holder.itemView.context)
            .load(currentAnime.images?.jpg?.imageUrl)
            .apply(RequestOptions().centerCrop())
            .into(holder.animeImage)

        holder.animeRating.text = currentAnime.score?.toString() ?: "N/A"
        holder.animeTitle.text = currentAnime.title ?: "Без назви"
        holder.animeEpisodes.text = currentAnime.episodes?.let { "$it серій" } ?: "N/A"

        holder.itemView.setOnClickListener {
            onItemClick(currentAnime)
        }
    }

    override fun getItemCount() = animeList.size
}