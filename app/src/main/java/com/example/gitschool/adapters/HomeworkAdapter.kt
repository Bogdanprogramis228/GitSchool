package com.example.gitschool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.data.HomeworkTask

class HomeworkAdapter(
    private var taskList: MutableList<HomeworkTask>,
    private val onTaskAction: (HomeworkTask, String) -> Unit
) : RecyclerView.Adapter<HomeworkAdapter.TaskViewHolder>() {

    // Список для відображення фільтрованих даних
    private var filteredTaskList: MutableList<HomeworkTask> = taskList

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.text_view_task_title)
        val date: TextView = itemView.findViewById(R.id.text_view_task_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_homework_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = filteredTaskList[position]
        holder.title.text = task.title
        holder.date.text = task.date

        holder.itemView.setOnClickListener {
            onTaskAction(task, "edit")
        }

        holder.itemView.setOnLongClickListener {
            onTaskAction(task, "delete")
            true
        }
    }

    override fun getItemCount() = filteredTaskList.size

    // Функція для фільтрації завдань по назві
    fun filter(query: String) {
        filteredTaskList = if (query.isEmpty()) {
            taskList
        } else {
            taskList.filter { it.title.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    // Функція для фільтрації завдань по статусу
    fun filterByStatus(status: String?) {
        filteredTaskList = if (status == null) {
            taskList
        } else {
            taskList.filter { it.status == status }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
