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
        lvTasks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteConfirmationDialog(position);
                return true;
            }
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
        ArrayList<String> updatedTasks = new ArrayList<>();
        for (int i = 0; i < individualTasks.length; i++) {
            if (i != position) {
                updatedTasks.add(individualTasks[i]);
            }
        }

        // 重新儲存待辦事項
        String updatedTasksString = updatedTasks.isEmpty() ? "" : String.join("\n", updatedTasks);
        taskPreferences.edit().putString(key, updatedTasksString).apply();

        // 同步更新 MainActivity 的 CalendarAdapter
        Intent intent = new Intent("com.example.calendarapp.TASK_UPDATED");
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        sendBroadcast(intent);

        // 更新畫面
        displayTaskDetails();

        Toast.makeText(this, "待辦事項已刪除", Toast.LENGTH_SHORT).show();
    }

    private void displayTaskDetails() {
        // 重用原本的顯示邏輯
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年 M月 d日", Locale.CHINA);
        tvDate.setText(dateFormat.format(calendar.getTime()));

        String key = year + "-" + month + "-" + day;
        String tasks = taskPreferences.getString(key, "");

        if (!tasks.isEmpty()) {
            String[] individualTasks = tasks.split("\n");
            tasksList.clear();
            for (String task : individualTasks) {
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