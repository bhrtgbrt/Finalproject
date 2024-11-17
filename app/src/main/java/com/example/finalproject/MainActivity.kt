package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnAddTodo: FloatingActionButton

    private var currentDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnAddTodo = findViewById(R.id.btnAddTodo)

        btnPrevious.setOnClickListener {
            currentDate = currentDate.minusMonths(1)
            updateCalendar()
        }

        btnNext.setOnClickListener {
            currentDate = currentDate.plusMonths(1)
            updateCalendar()
        }

        btnAddTodo.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }

        // 設置 RecyclerView
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
        updateCalendar()
    }

    private fun updateCalendar() {
        val daysInMonth = getDaysInMonth(currentDate)
        tvMonthYear.text = currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        calendarRecyclerView.adapter = CalendarAdapter(daysInMonth)
    }

    private fun getDaysInMonth(date: LocalDate): List<String> {
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7

        val daysList = mutableListOf<String>()

        // 填充空白日期
        for (i in 1..firstDayOfWeek) {
            daysList.add("")
        }

        // 填充有效日期
        for (day in 1..daysInMonth) {
            daysList.add(day.toString())
        }

        return daysList
    }
}
