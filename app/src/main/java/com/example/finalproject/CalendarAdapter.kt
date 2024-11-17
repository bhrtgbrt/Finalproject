package com.example.finalproject

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(private val daysOfMonth: List<String>) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_item, parent, false)

        // 設置每個格子為正方形
        val layoutParams = view.layoutParams
        val screenWidth = getScreenWidth(parent.context)
        layoutParams.width = screenWidth / 7 // 7 列
        layoutParams.height = layoutParams.width // 高度等於寬度
        view.layoutParams = layoutParams

        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(daysOfMonth[position])
    }

    override fun getItemCount(): Int {
        return daysOfMonth.size
    }

    private fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val display = context.resources.displayMetrics
        return display.widthPixels
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)

        fun bind(day: String) {
            tvDay.text = day
            tvDay.visibility = if (day.isEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }
}
