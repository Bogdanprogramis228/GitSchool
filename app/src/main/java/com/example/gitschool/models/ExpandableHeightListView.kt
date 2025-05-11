package com.example.gitschool.models


import android.content.Context
import android.util.AttributeSet
import android.widget.ExpandableListView

class ExpandableHeightListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ExpandableListView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = MeasureSpec.makeMeasureSpec(
            Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST
        )
        super.onMeasure(widthMeasureSpec, expandSpec)

        val params = layoutParams
        params.height = measuredHeight
    }
}
