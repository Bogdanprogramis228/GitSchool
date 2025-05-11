package com.example.gitschool.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.R
import com.example.gitschool.data.Notification // Ваш data клас
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    internal var notifications: MutableList<Notification>,
    private val onItemClick: (Notification) -> Unit,
    private val onItemDelete: (Notification) -> Unit // Для кнопки видалення
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("HH:mm   dd.MM.yyyy", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.notificationIcon)
        val title: TextView = itemView.findViewById(R.id.notificationTitle)
        val message: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestamp: TextView = itemView.findViewById(R.id.notificationTimestamp)
        val readIndicator: ImageView = itemView.findViewById(R.id.notificationReadIndicator)

        fun bind(notification: Notification) {
            title.text = notification.title
            message.text = notification.message
            timestamp.text = notification.timestamp?.toDate()?.let { dateFormat.format(it) } ?: ""
            readIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            // Встановлюємо іконку/постер залежно від типу
            if (notification.type == "anime_update" && !notification.animeImageUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(notification.animeImageUrl)
                    .placeholder(R.drawable.action_placeholder) // Заглушка для аніме
                    .error(R.drawable.action_placeholder)
                    .into(icon)
            } else {
                // Для системних або якщо немає URL
                icon.setImageResource(R.drawable.onepuch_fon) // Іконка додатку
            }

            itemView.setOnClickListener {
                onItemClick(notification)
                // Можна позначити як прочитане при кліку
                // notification.isRead = true
                // readIndicator.visibility = View.GONE
                // TODO: Оновити isRead в Firestore
            }

            // TODO: Додати кнопку видалення в item_notification.xml і її обробник тут,
            // який викликатиме onItemDelete(notification)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateNotifications(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    // Функція для видалення елемента (викликається з Activity після видалення з Firestore)
    fun removeItem(notification: Notification) {
        val position = notifications.indexOf(notification)
        if (position > -1) {
            notifications.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}