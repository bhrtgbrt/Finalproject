package com.example.calendarapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AddTaskActivity extends AppCompatActivity {
    private EditText etTask;
    private EditText etDate;
    private Calendar selectedDate;
    private SharedPreferences taskPreferences;
    private TimePicker tpTime;

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
        etDate = findViewById(R.id.etDate);
        tpTime = findViewById(R.id.tpTime);
        selectedDate = Calendar.getInstance();
        taskPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);

        // 設置日期點擊事件
        etDate.setOnClickListener(v -> showDatePicker());
    }

    private void saveTask() {
        String task = etTask.getText().toString().trim();
        String dateString = etDate.getText().toString().trim();

        if (task.isEmpty()) {
            Toast.makeText(this, "請輸入待辦事項", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateString.isEmpty()) {
            Toast.makeText(this, "請選擇日期", Toast.LENGTH_SHORT).show();
            return;
        }

        // 獲取時間
        int hour = tpTime.getCurrentHour();
        int minute = tpTime.getCurrentMinute();
        String timeString = String.format("%02d:%02d", hour, minute);

        // 將時間加入任務
        String fullTask = task + "|" + timeString;

        // 用 key 來儲存事項
        String key = getTaskKey(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // 取得目前已儲存的事項
        String existingTasks = taskPreferences.getString(key, "");

        // 如果已經有事項，就用換行符號分隔
        String updatedTasks = existingTasks.isEmpty() ?
                fullTask :
                existingTasks + "\n" + fullTask;

        taskPreferences.edit()
                .putString(key, updatedTasks)
                .apply();

        // 創建返回數據
        Intent resultIntent = new Intent();
        resultIntent.putExtra("task", fullTask);
        resultIntent.putExtra("year", selectedDate.get(Calendar.YEAR));
        resultIntent.putExtra("month", selectedDate.get(Calendar.MONTH));
        resultIntent.putExtra("day", selectedDate.get(Calendar.DAY_OF_MONTH));

        // 設置結果並返回
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("選擇日期")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // 轉換選擇的日期
            selectedDate.setTimeInMillis(selection);

            // 格式化日期顯示
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formattedDate = sdf.format(selectedDate.getTime());

            etDate.setText(formattedDate);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void setupButtons() {
        // 返回按鈕
        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(v -> finish());

        // 保存按鈕
        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(v -> saveTask());
    }

    private String getTaskKey(int year, int month, int day) {
        return year + "-" + month + "-" + day;
    }
}