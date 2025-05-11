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

class AnimeAdapter(
    private val animeList: List<Anime>,
    private val onItemClick: (Anime) -> Unit
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime_card, parent, false)
        return AnimeViewHolder(view)
    }


    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animeList[position]
        holder.bind(anime as Anime, onItemClick)
    }

    override fun getItemCount(): Int = animeList.size

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val animeImage: ImageView = itemView.findViewById(R.id.anime_image)
        private val animeTitle: TextView = itemView.findViewById(R.id.anime_title)
        private val animeRating: TextView = itemView.findViewById(R.id.anime_rating)
        private val animeEpisodes: TextView = itemView.findViewById(R.id.anime_episodes)

        fun bind(anime: Anime, onItemClick: (Anime) -> Unit) {
            animeTitle.text = anime.title ?: "Назва невідома"

            // --- Оновлена логіка для рейтингу ---
            if (anime.score != null && anime.score > 0) { // Показуємо, тільки якщо є і більше 0
                animeRating.text = String.format("%.1f", anime.score) // Форматуємо до 1 знаку після коми
                animeRating.visibility = View.VISIBLE
            } else {
                animeRating.visibility = View.GONE // Ховаємо, якщо даних немає
            }

            // --- Оновлена логіка для епізодів ---
            if (anime.episodes != null && anime.episodes > 0) { // Показуємо, тільки якщо є і більше 0
                animeEpisodes.text = "${anime.episodes} серій"
                animeEpisodes.visibility = View.VISIBLE
            } else {
                animeEpisodes.visibility = View.GONE // Ховаємо, якщо даних немає
            }

            val imageUrl = anime.images?.jpg?.imageUrl
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.onepuch_fon)
                .error(R.drawable.onepuch_fon) // Додайте обробку помилки завантаження
                .into(animeImage)

            itemView.setOnClickListener {
                onItemClick(anime)
            }
        }
    }
}