package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView tvYearMonth;
    private TextView tvLunarDate;
    private GridView gridCalendar;
    private Calendar calendar;
    private CalendarAdapter adapter;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupCalendar();
        setupAddButton();
        setupDateGridClick();
        setupGestureDetector();
    }

    private void initializeViews() {
        tvYearMonth = findViewById(R.id.tvYearMonth);
        tvLunarDate = findViewById(R.id.tvLunarDate);
        gridCalendar = findViewById(R.id.gridCalendar);
    }

    private void setupCalendar() {
        calendar = Calendar.getInstance();
        updateCalendarView();
    }

    private void setupAddButton() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void setupDateGridClick() {
        gridCalendar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Calendar clickedDate = adapter.getDateAtPosition(position);

                if (clickedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                    Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                    intent.putExtra("year", clickedDate.get(Calendar.YEAR));
                    intent.putExtra("month", clickedDate.get(Calendar.MONTH));
                    intent.putExtra("day", clickedDate.get(Calendar.DAY_OF_MONTH));
                    startActivity(intent);
                }
            }
        });
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());
        gridCalendar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void updateCalendarView() {
        String yearMonth = String.format("%d年 %d月",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1);
        tvYearMonth.setText(yearMonth);

        String lunarDate = LunarCalendarUtils.getLunarDate(calendar);
        tvLunarDate.setText(lunarDate);

        adapter = new CalendarAdapter(this, calendar);
        gridCalendar.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String task = data.getStringExtra("task");
            int year = data.getIntExtra("year", calendar.get(Calendar.YEAR));
            int month = data.getIntExtra("month", calendar.get(Calendar.MONTH));
            int day = data.getIntExtra("day", calendar.get(Calendar.DAY_OF_MONTH));

            if (adapter != null) {
                adapter.addTask(year, month, day, task);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX > 0) {
                    calendar.add(Calendar.MONTH, -1);
                } else {
                    calendar.add(Calendar.MONTH, 1);
                }
                updateCalendarView();
                return true;
            }
            return false;
        }
    }
}