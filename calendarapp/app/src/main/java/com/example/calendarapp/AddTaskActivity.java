// AddTaskActivity.java
package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddTaskActivity extends AppCompatActivity {
    private EditText etTask;
    private DatePicker dpDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // 初始化元件
        initializeViews();

        // 設置按鈕點擊事件
        setupButtons();
    }

    private void initializeViews() {
        etTask = findViewById(R.id.etTask);
        dpDate = findViewById(R.id.dpDate);
    }

    private void setupButtons() {
        // 返回按鈕
        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 保存按鈕
        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
    }

    private void saveTask() {
        String task = etTask.getText().toString().trim();

        if (task.isEmpty()) {
            Toast.makeText(this, "請輸入待辦事項", Toast.LENGTH_SHORT).show();
            return;
        }

        // 創建返回數據
        Intent resultIntent = new Intent();
        resultIntent.putExtra("task", task);
        resultIntent.putExtra("year", dpDate.getYear());
        resultIntent.putExtra("month", dpDate.getMonth());
        resultIntent.putExtra("day", dpDate.getDayOfMonth());

        // 設置結果並返回
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}