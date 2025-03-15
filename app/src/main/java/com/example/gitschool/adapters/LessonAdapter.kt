package com.example.gitschool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.R
import com.example.gitschool.models.LessonItem

class LessonAdapter(private val lessonList: List<LessonItem>) :
    RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.lesson_time)
        val subjectTextView: TextView = view.findViewById(R.id.lesson_subject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessonList[position]
        holder.timeTextView.text = lesson.time
        holder.subjectTextView.text = lesson.subject
    }

    override fun getItemCount() = lessonList.size
}
