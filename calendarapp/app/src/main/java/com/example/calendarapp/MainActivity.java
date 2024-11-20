// MainActivity.java
package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView tvYearMonth;
    private GridView gridCalendar;
    private Calendar calendar;
    private CalendarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化元件
        initializeViews();

        // 設置日曆
        setupCalendar();

        // 設置添加按鈕點擊事件
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void initializeViews() {
        tvYearMonth = findViewById(R.id.tvYearMonth);
        gridCalendar = findViewById(R.id.gridCalendar);
    }

    private void setupCalendar() {
        calendar = Calendar.getInstance();
        updateCalendarView();
    }

    private void updateCalendarView() {
        // 更新年月顯示
        String yearMonth = String.format("%d年 %d月",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1);
        tvYearMonth.setText(yearMonth);

        // 更新日曆網格
        adapter = new CalendarAdapter(this, calendar);
        gridCalendar.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // 獲取返回的待辦事項數據
            String task = data.getStringExtra("task");
            int year = data.getIntExtra("year", calendar.get(Calendar.YEAR));
            int month = data.getIntExtra("month", calendar.get(Calendar.MONTH));
            int day = data.getIntExtra("day", calendar.get(Calendar.DAY_OF_MONTH));

            // 更新日曆顯示
            if (adapter != null) {
                adapter.addTask(year, month, day, task);
                adapter.notifyDataSetChanged();
            }
        }
    }
}