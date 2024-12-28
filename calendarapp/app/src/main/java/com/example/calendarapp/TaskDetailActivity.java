package com.example.calendarapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;

public class TaskDetailActivity extends AppCompatActivity {
    private TextView tvDate;
    private ListView lvTasks;
    private SharedPreferences taskPreferences;
    private ArrayList<String> tasksList;
    private ArrayList<String> rawTasksList;
    private ArrayAdapter<String> adapter;
    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initializeViews();
        setupBackButton();
        setupTaskDeletion();
        loadAndDisplayTasks();
    }

    private void initializeViews() {
        tvDate = findViewById(R.id.tvDate);
        lvTasks = findViewById(R.id.lvTasks);
        taskPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        tasksList = new ArrayList<>();
        rawTasksList = new ArrayList<>();

        year = getIntent().getIntExtra("year", 0);
        month = getIntent().getIntExtra("month", 0);
        day = getIntent().getIntExtra("day", 0);

        adapter = new ArrayAdapter<>(this, R.layout.task_item, R.id.tvTaskItem, tasksList);
        lvTasks.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年 M月 d日", Locale.CHINA);
        tvDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupBackButton() {
        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(v -> finish());
    }

    private void setupTaskDeletion() {
        lvTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!tasksList.isEmpty() && !tasksList.get(0).equals("無代辦事項")) {
                showDeleteConfirmationDialog(position);
            }
            return true;
        });
    }

    private void loadAndDisplayTasks() {
        String key = year + "-" + month + "-" + day;
        String tasksStr = taskPreferences.getString(key, "");

        tasksList.clear();
        rawTasksList.clear();

        if (tasksStr.isEmpty()) {
            tasksList.add("無代辦事項");
        } else {
            String[] tasks = tasksStr.split("\n");
            Set<String> uniqueTasks = new LinkedHashSet<>();  // 使用 LinkedHashSet 來保持順序並去重

            for (String task : tasks) {
                if (!task.trim().isEmpty() && uniqueTasks.add(task)) {  // 只有當任務不重複時才添加
                    rawTasksList.add(task);
                    tasksList.add(formatTaskForDisplay(task));
                }
            }

            // 如果發現有重複任務，更新存儲的任務列表
            if (uniqueTasks.size() < tasks.length) {
                StringBuilder newTasks = new StringBuilder();
                boolean first = true;
                for (String task : uniqueTasks) {
                    if (!first) {
                        newTasks.append("\n");
                    }
                    newTasks.append(task);
                    first = false;
                }
                taskPreferences.edit().putString(key, newTasks.toString()).apply();
            }
        }

        if (tasksList.isEmpty()) {
            tasksList.add("無代辦事項");
        }

        adapter.notifyDataSetChanged();
    }

    private String formatTaskForDisplay(String rawTask) {
        String[] parts = rawTask.split("\\|");
        if (parts.length > 1) {
            StringBuilder display = new StringBuilder(parts[0])
                    .append(" (時間: ").append(parts[1]).append(")");
            if (parts.length > 2) {
                display.append(" (").append(parts[2]).append(")");
            }
            return display.toString();
        }
        return rawTask;
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("刪除待辦事項")
                .setMessage("確定要刪除此待辦事項嗎？")
                .setPositiveButton("確定", (dialog, which) -> deleteTask(position))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteTask(int position) {
        if (tasksList.get(0).equals("無代辦事項")) {
            return;
        }

        String key = year + "-" + month + "-" + day;
        String taskToRemove = rawTasksList.get(position);
        rawTasksList.remove(position);

        StringBuilder newTasks = new StringBuilder();
        for (int i = 0; i < rawTasksList.size(); i++) {
            if (i > 0) {
                newTasks.append("\n");
            }
            newTasks.append(rawTasksList.get(i));
        }

        taskPreferences.edit().putString(key, newTasks.toString()).apply();

        loadAndDisplayTasks();

        Intent intent = new Intent("com.example.calendarapp.TASK_UPDATED");
        sendBroadcast(intent);

        Toast.makeText(this, "待辦事項已刪除", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplayTasks();
    }
}