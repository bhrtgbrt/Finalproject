package com.example.calendarapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

public class TaskDetailActivity extends AppCompatActivity {
    private TextView tvDate;
    private ListView lvTasks;
    private SharedPreferences taskPreferences;
    private ArrayList<String> tasksList;
    private ArrayAdapter<String> adapter;
    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initializeViews();
        setupBackButton();
        setupTaskDeletion();
        displayTaskDetails();
    }

    private void initializeViews() {
        tvDate = findViewById(R.id.tvDate);
        lvTasks = findViewById(R.id.lvTasks);
        taskPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        tasksList = new ArrayList<>();

        year = getIntent().getIntExtra("year", 0);
        month = getIntent().getIntExtra("month", 0);
        day = getIntent().getIntExtra("day", 0);
    }

    private void setupBackButton() {
        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(v -> finish());
    }

    private void setupTaskDeletion() {
        lvTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!tasksList.get(0).equals("無代辦事項")) {
                showDeleteConfirmationDialog(position);
            }
            return true;
        });
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
        String key = year + "-" + month + "-" + day;
        String tasks = taskPreferences.getString(key, "");
        String[] individualTasks = tasks.split("\n");

        // 移除指定位置的待辦事項
        StringBuilder updatedTasksBuilder = new StringBuilder();
        for (int i = 0; i < individualTasks.length; i++) {
            if (i != position) {
                if (updatedTasksBuilder.length() > 0) {
                    updatedTasksBuilder.append("\n");
                }
                updatedTasksBuilder.append(individualTasks[i]);
            }
        }

        String updatedTasksString = updatedTasksBuilder.toString();

        // 保存更新後的待辦事項
        taskPreferences.edit().putString(key, updatedTasksString).apply();

        // 發送廣播通知主活動更新
        Intent intent = new Intent("com.example.calendarapp.TASK_UPDATED");
        sendBroadcast(intent);

        // 更新畫面1
        if (updatedTasksString.isEmpty()) {
            tasksList.clear();
            tasksList.add("無代辦事項");
        } else {
            updateTasksList(updatedTasksString);
        }
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "待辦事項已刪除", Toast.LENGTH_SHORT).show();
    }

    private void displayTaskDetails() {
        // 設置日期顯示
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年 M月 d日", Locale.CHINA);
        tvDate.setText(dateFormat.format(calendar.getTime()));

        // 獲取並顯示待辦事項
        String key = year + "-" + month + "-" + day;
        String tasks = taskPreferences.getString(key, "");

        tasksList.clear();
        if (!tasks.isEmpty()) {
            updateTasksList(tasks);
        } else {
            tasksList.add("無代辦事項");
        }

        // 只在第一次創建時初始化adapter
        if (adapter == null) {
            adapter = new ArrayAdapter<>(this, R.layout.task_item, R.id.tvTaskItem, tasksList);
            lvTasks.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateTasksList(String tasks) {
        tasksList.clear();
        String[] individualTasks = tasks.split("\n");
        for (String task : individualTasks) {
            String[] taskParts = task.split("\\|");
            if (taskParts.length > 1) {
                String displayText = taskParts[0] + " (時間: " + taskParts[1] + ")";
                if (taskParts.length > 2) {
                    displayText += " (" + taskParts[2] + ")";
                }
                tasksList.add(displayText);
            } else {
                tasksList.add(task);
            }
        }
    }
}