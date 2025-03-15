package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.R
import com.example.gitschool.data.LibraryItem
import com.bumptech.glide.Glide

class LibraryAdapter(
    private val items: List<LibraryItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewClass)
        val textView: TextView = view.findViewById(R.id.textViewClass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Glide.with(holder.imageView.context).load(item.imageUrl).into(holder.imageView)
        holder.textView.text = item.className
        holder.itemView.setOnClickListener { onItemClick(item.className) }
    }

    override fun getItemCount() = items.size
}
