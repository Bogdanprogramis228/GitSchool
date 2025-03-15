package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.R

class PhotoAdapter(private var photos: List<String>, private val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.photo_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoUrl = photos[position]

        Glide.with(holder.imageView.context)
            .load(photoUrl)
            .placeholder(R.drawable.logo)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            val dialog = PhotoDialogFragment.newInstance(photoUrl)
            dialog.show(fragmentManager, "photo_dialog")
        }
    }

    override fun getItemCount(): Int = photos.size

    fun updateData(newPhotos: List<String>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
