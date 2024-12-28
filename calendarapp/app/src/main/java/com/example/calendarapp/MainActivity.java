package com.example.calendarapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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
    private GridView gridCalendar;
    private Calendar calendar;
    private CalendarAdapter adapter;
    private GestureDetectorCompat gestureDetector;
    private BroadcastReceiver taskUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.calendarapp.TASK_UPDATED".equals(intent.getAction())) {
                // 只更新日曆視圖，不觸發其他操作
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupCalendar();
        setupAddButton();
        setupDateGridClick();
        setupGestureDetector();

        // 註冊廣播接收器
        IntentFilter filter = new IntentFilter("com.example.calendarapp.TASK_UPDATED");
        registerReceiver(taskUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void initializeViews() {
        tvYearMonth = findViewById(R.id.tvYearMonth);
        gridCalendar = findViewById(R.id.gridCalendar);
    }

    private void setupCalendar() {
        calendar = Calendar.getInstance();
        updateCalendarView();
    }

    private void setupAddButton() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void setupDateGridClick() {
        gridCalendar.setOnItemClickListener((parent, view, position, id) -> {
            Calendar clickedDate = adapter.getDateAtPosition(position);
            if (clickedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                intent.putExtra("year", clickedDate.get(Calendar.YEAR));
                intent.putExtra("month", clickedDate.get(Calendar.MONTH));
                intent.putExtra("day", clickedDate.get(Calendar.DAY_OF_MONTH));
                startActivity(intent);
            }
        });
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());
        gridCalendar.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void updateCalendarView() {
        String yearMonth = String.format("%d年 %d月",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1);
        tvYearMonth.setText(yearMonth);

        adapter = new CalendarAdapter(this, calendar);
        gridCalendar.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // 直接更新日曆視圖，不發送廣播
            if (adapter != null) {
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(taskUpdateReceiver);
        super.onDestroy();
    }
}