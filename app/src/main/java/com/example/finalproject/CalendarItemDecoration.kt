package com.example.finalproject

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CalendarItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % 7 // 每行7個item

        // 左右間距
        outRect.left = if (column == 0) 0 else spacing / 2
        outRect.right = if (column == 6) 0 else spacing / 2

        // 上下間距
        if (position < 7) { // 第一行無上間距
            outRect.top = 0
        } else {
            outRect.top = spacing / 2
        }
        outRect.bottom = spacing / 2
    }
}
