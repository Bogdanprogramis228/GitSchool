package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.R
import com.example.gitschool.ViewHistoryItem

class HistoryAdapter(
    private var historyList: List<ViewHistoryItem>,
    private val onItemClick: (ViewHistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImageView: ImageView = itemView.findViewById(R.id.animeImageView)
        val animeTitleTextView: TextView = itemView.findViewById(R.id.animeTitleTextView)
        val animeEpisodesTextView: TextView = itemView.findViewById(R.id.animeEpisodesTextView)
        val animeYearTextView: TextView = itemView.findViewById(R.id.animeYearTextView)
        val animeRatingTextView: TextView = itemView.findViewById(R.id.animeRatingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anime_by_genre, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        // Завантажуємо зображення
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.onepuch_fon)
            .into(holder.animeImageView)
        // Встановлюємо текст
        holder.animeTitleTextView.text = item.title
        holder.animeEpisodesTextView.text = "Серій: ${item.episodes}"
        holder.animeYearTextView.text = "Рік: ${item.year}"
        holder.animeRatingTextView.text = "Рейтинг: ${item.rating} ⭐"

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newHistoryList: List<ViewHistoryItem>) {
        historyList = newHistoryList
        notifyDataSetChanged()
    }
}
