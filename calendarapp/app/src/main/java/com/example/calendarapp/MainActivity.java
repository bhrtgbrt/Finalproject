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
                // 強制更新 adapter
                adapter.notifyDataSetChanged();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化元件
        initializeViews();

        // 設置日曆
        setupCalendar();

        // 設置添加按鈕點擊事件
        setupAddButton();

        // 添加日期格子點擊事件
        setupDateGridClick();

        // 添加手勢偵測器
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

                // 只有當前月份的日期才可以點擊
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
        // 更新年月顯示
        String yearMonth = String.format("%d年 %d月",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1);
        tvYearMonth.setText(yearMonth);

        // 更新日曆網格
        adapter = new CalendarAdapter(this, calendar);
        gridCalendar.setAdapter(adapter);
        Log.d("MainActivity", "日曆視圖已更新");
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

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 判斷左右滑動
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX > 0) {
                    // 向右滑動，顯示上一個月
                    calendar.add(Calendar.MONTH, -1);
                } else {
                    // 向左滑動，顯示下一個月
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
        // 取消註冊廣播接收器
        unregisterReceiver(taskUpdateReceiver);
        super.onDestroy();
    }


}