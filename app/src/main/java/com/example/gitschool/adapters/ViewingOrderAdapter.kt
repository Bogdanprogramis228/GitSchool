package com.example.gitschool.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.R
import com.example.gitschool.data.ViewingOrderItem // Імпорт нашого data класу

class ViewingOrderAdapter(
    private var items: List<ViewingOrderItem>, // Змінено на var для оновлення
    private val onItemClick: (ViewingOrderItem) -> Unit
) : RecyclerView.Adapter<ViewingOrderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val relationText: TextView = itemView.findViewById(R.id.viewing_order_item_relation)
        private val titleText: TextView = itemView.findViewById(R.id.viewing_order_item_title)

        fun bind(item: ViewingOrderItem) {
            relationText.text = item.relation
            titleText.text = item.title
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_viewing_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // Додатковий метод для оновлення даних в адаптері
    @SuppressLint("NotifyDataSetChanged") // Використовуємо для простоти
    fun updateData(newItems: List<ViewingOrderItem>) {
        items = newItems
        notifyDataSetChanged() // Повідомляємо адаптер про зміни
    }
}