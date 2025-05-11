package com.example.gitschool.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.gitschool.R // Замініть на свій R-файл

class CustomSpinnerAdapter(
    context: Context,
    private val statuses: Array<String>
) : ArrayAdapter<String>(context, R.layout.spinner_item, statuses) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false)
            viewHolder = ViewHolder(view.findViewById(R.id.spinnerTextView))
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.textView.text = statuses[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: DropdownViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.spinner_dropdown_item, parent, false)
            viewHolder = DropdownViewHolder(view.findViewById(R.id.spinnerTextView))
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as DropdownViewHolder
        }

        viewHolder.textView.text = statuses[position]
        return view
    }

    private class ViewHolder(val textView: TextView)
    private class DropdownViewHolder(val textView: TextView)
}