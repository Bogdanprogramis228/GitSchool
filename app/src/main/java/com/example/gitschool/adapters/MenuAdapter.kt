package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.R
import com.example.gitschool.data.MenuItem

class MenuAdapter(private var menuList: List<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dishName: TextView = itemView.findViewById(R.id.dishName)
        val weight: TextView = itemView.findViewById(R.id.dishWeight)
        val price: TextView = itemView.findViewById(R.id.dishPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuList[position]
        holder.dishName.text = menuItem.dishName
        holder.weight.text = "Вага: ${menuItem.weight}"
        holder.price.text = "Ціна: ${menuItem.price}"
    }

    override fun getItemCount() = menuList.size

    fun updateData(newList: List<MenuItem>) {
        menuList = newList
        notifyDataSetChanged()
    }
}
