package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.R
import com.example.gitschool.models.TeacherItem

class TeacherAdapter(
    private var teachers: List<TeacherItem>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder>() {

    class TeacherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.teacher_image)
        val nameView: TextView = view.findViewById(R.id.teacher_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_teacher, parent, false)
        return TeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        val teacher = teachers[position]
        Glide.with(holder.imageView.context)
            .load(teacher.photoUrl)
            .placeholder(R.drawable.logo)
            .into(holder.imageView)
        holder.nameView.text = teacher.name

        holder.itemView.setOnClickListener {
            val dialog = TeacherDialogFragment.newInstance(teacher.photoUrl, teacher.name)
            dialog.show(fragmentManager, "teacher_dialog")
        }
    }

    override fun getItemCount(): Int = teachers.size

    fun updateData(newTeachers: List<TeacherItem>) {
        teachers = newTeachers
        notifyDataSetChanged()
    }
}

