package com.example.finalproject



import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton

    private lateinit var calendarAdapter: CalendarAdapter
    private var currentDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)

        updateCalendar()

        btnPrevious.setOnClickListener {
            currentDate = currentDate.minusMonths(1)
            updateCalendar()
        }

        btnNext.setOnClickListener {
            currentDate = currentDate.plusMonths(1)
            updateCalendar()
        }
    }

    private fun updateCalendar() {
        val daysInMonth = getDaysInMonth(currentDate)
        tvMonthYear.text = currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        calendarAdapter = CalendarAdapter(daysInMonth)
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun getDaysInMonth(date: LocalDate): List<String> {
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDay = yearMonth.atDay(1).dayOfWeek.value % 7

        val daysList = mutableListOf<String>()

        for (i in 1..firstDay) {
            daysList.add("")
        }

        for (day in 1..daysInMonth) {
            daysList.add(day.toString())
        }

        return daysList
    }
}
