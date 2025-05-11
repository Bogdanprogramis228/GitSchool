package com.example.gitschool.adapters


import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.BaseExpandableListAdapter
import androidx.core.content.ContextCompat
import com.example.gitschool.R

class FaqExpandableListAdapter(
    private val ctx: Context,
    private val questions: List<String>,
    private val answers: List<String>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = questions.size
    override fun getChildrenCount(groupPosition: Int): Int = 1
    override fun getGroup(groupPosition: Int): Any = questions[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int): Any = answers[groupPosition]
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
    override fun hasStableIds(): Boolean = false
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View {
        val view = convertView ?: LayoutInflater.from(ctx)
            .inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        (view.findViewById(android.R.id.text1) as TextView).apply {
            text = questions[groupPosition]
            setTextColor(ContextCompat.getColor(ctx, R.color.white))
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setPadding(32, 24, 16, 24)
        }
        return view
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        val view = convertView ?: LayoutInflater.from(ctx)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        (view.findViewById(android.R.id.text1) as TextView).apply {
            text = answers[groupPosition]
            setTextColor(ContextCompat.getColor(ctx, R.color.white))
            textSize = 14f
            setPadding(48, 16, 16, 16)
        }
        return view
    }
}
