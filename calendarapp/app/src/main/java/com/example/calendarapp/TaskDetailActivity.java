package com.example.calendarapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {
    private TextView tvDate;
    private ListView lvTasks;
    private SharedPreferences taskPreferences;
    private ArrayList<String> tasksList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // 初始化視圖
        initializeViews();

        // 設置返回按鈕
        setupBackButton();

        // 顯示詳細信息
        displayTaskDetails();
    }

    private void initializeViews() {
        tvDate = findViewById(R.id.tvDate);
        lvTasks = findViewById(R.id.lvTasks);
        taskPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        tasksList = new ArrayList<>();
    }

    private void setupBackButton() {
        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(v -> finish());
    }

    private void displayTaskDetails() {
        // 獲取傳遞的日期信息
        int year = getIntent().getIntExtra("year", 0);
        int month = getIntent().getIntExtra("month", 0);
        int day = getIntent().getIntExtra("day", 0);

        // 格式化日期顯示
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年 M月 d日", Locale.CHINA);
        tvDate.setText(dateFormat.format(calendar.getTime()));

        // 獲取並顯示任務
        String key = year + "-" + month + "-" + day;
        String tasks = taskPreferences.getString(key, "");

        if (!tasks.isEmpty()) {
            String[] individualTasks = tasks.split("\n");
            tasksList.clear();
            for (String task : individualTasks) {
                // 如果有時間資訊，分割任務和時間
                String[] taskParts = task.split("\\|");
                if (taskParts.length > 1) {
                    tasksList.add(taskParts[0] + " (時間: " + taskParts[1] + ")");
                } else {
                    tasksList.add(task);
                }
            }

            adapter = new ArrayAdapter<>(this, R.layout.task_item, R.id.tvTaskItem, tasksList);
            lvTasks.setAdapter(adapter);
        } else {
            tasksList.add("無代辦事項");
            adapter = new ArrayAdapter<>(this, R.layout.task_item, R.id.tvTaskItem, tasksList);
            lvTasks.setAdapter(adapter);
        }
    }
}