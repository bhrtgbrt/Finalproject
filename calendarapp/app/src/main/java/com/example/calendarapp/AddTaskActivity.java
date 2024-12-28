package com.example.calendarapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private Spinner spRecurrence;
    private Spinner spReminder;
    private static final long[] REMINDER_TIMES = {
            0,                  // 不提醒
            15 * 60 * 1000,    // 15分鐘 (毫秒)
            60 * 60 * 1000,    // 1小時
            3 * 60 * 60 * 1000,// 3小時
            24 * 60 * 60 * 1000,// 1天
            3 * 24 * 60 * 60 * 1000 // 3天
    };
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // 初始化權限請求
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 權限獲得後繼續保存任務
                        proceedWithSaveTask();
                    } else {
                        Toast.makeText(this, "需要通知權限才能設置提醒", Toast.LENGTH_LONG).show();
                    }
                }
        );

        initializeViews();
        setupButtons();
        setupSpinners();
    }

    private void setupSpinners() {
        spReminder = findViewById(R.id.spReminder);
        ArrayAdapter<CharSequence> reminderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.reminder_options,
                android.R.layout.simple_spinner_item
        );
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReminder.setAdapter(reminderAdapter);
    }

    private void initializeViews() {
        etTask = findViewById(R.id.etTask);
        etDate = findViewById(R.id.etDate);
        tpTime = findViewById(R.id.tpTime);
        spRecurrence = findViewById(R.id.spRecurrence);
        selectedDate = Calendar.getInstance();
        taskPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);

        etDate.setOnClickListener(v -> showDatePicker());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.recurrence_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRecurrence.setAdapter(adapter);
    }

    private void setupButtons() {
        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(v -> finish());

        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        if (!validateInputs()) {
            return;
        }

        // 獲取任務文本並清理空白
        String taskText = etTask.getText().toString().trim();

        // 獲取時間
        int hour = tpTime.getCurrentHour();
        int minute = tpTime.getCurrentMinute();
        String timeString = String.format("%02d:%02d", hour, minute);

        // 創建完整的任務字符串
        String fullTask = taskText + "|" + timeString;

        // 檢查是否需要添加重複標記
        String recurrenceOption = spRecurrence.getSelectedItem().toString();
        if (!recurrenceOption.equals("不重複")) {
            fullTask += "|" + recurrenceOption;
        }

        // 檢查是否已存在相同的任務
        String key = getTaskKey(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        String existingTasks = taskPreferences.getString(key, "");
        if (existingTasks.contains(fullTask)) {
            Toast.makeText(this, "該任務已經存在", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spReminder.getSelectedItemPosition() > 0) {
            checkAndRequestPermissions();
        } else {
            proceedWithSaveTask();
        }
    }

    private boolean validateInputs() {
        String task = etTask.getText().toString().trim();
        String dateString = etDate.getText().toString().trim();

        if (task.isEmpty()) {
            Toast.makeText(this, "請輸入待辦事項", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dateString.isEmpty()) {
            Toast.makeText(this, "請選擇日期", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkAndRequestPermissions() {
        // 檢查通知權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        // 檢查精確鬧鐘權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("需要權限")
                        .setMessage("請允許應用程式使用精確鬧鐘功能來設置提醒")
                        .setPositiveButton("設置", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                        .show();
                return;
            }
        }

        proceedWithSaveTask();
    }

    private void proceedWithSaveTask() {
        String task = etTask.getText().toString().trim();
        int hour = tpTime.getCurrentHour();
        int minute = tpTime.getCurrentMinute();
        String timeString = String.format("%02d:%02d", hour, minute);

        String recurrenceOption = spRecurrence.getSelectedItem().toString();

        // 根據重複選項保存任務
        switch (recurrenceOption) {
            case "不重複":
                saveSingleTask(task, timeString);
                break;
            case "每年":
                saveRecurringYearlyTask(task, timeString);
                break;
            case "每月":
                saveRecurringMonthlyTask(task, timeString);
                break;
            case "每週":
                saveRecurringWeeklyTask(task, timeString);
                break;
        }

        if (spReminder.getSelectedItemPosition() > 0) {
            scheduleNotification(task, timeString);
        }

        // 發送廣播通知更新
        Intent updateIntent = new Intent("com.example.calendarapp.TASK_UPDATED");
        sendBroadcast(updateIntent);
    }

    private void scheduleNotification(String task, String timeString) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task", task);
        intent.putExtra("time", timeString);

        Calendar reminderTime = (Calendar) selectedDate.clone();
        String[] timeParts = timeString.split(":");
        reminderTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        reminderTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));

        long reminderOffset = REMINDER_TIMES[spReminder.getSelectedItemPosition()];
        long notificationTime = reminderTime.getTimeInMillis() - reminderOffset;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "需要精確鬧鐘權限才能設置提醒", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                );
            } catch (SecurityException e) {
                Toast.makeText(this, "設置提醒失敗，請檢查權限設置", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveSingleTask(String task, String timeString) {
        String fullTask = task + "|" + timeString;
        String key = getTaskKey(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        updateTaskPreferences(key, fullTask);
        setResultAndFinish(fullTask);
    }

    private void saveRecurringYearlyTask(String task, String timeString) {
        Calendar startDate = (Calendar) selectedDate.clone();
        for (int year = startDate.get(Calendar.YEAR); year < startDate.get(Calendar.YEAR) + 10; year++) {
            Calendar recurringDate = (Calendar) startDate.clone();
            recurringDate.set(Calendar.YEAR, year);

            String fullTask = task + "|" + timeString + "|yearly";
            String key = getTaskKey(
                    recurringDate.get(Calendar.YEAR),
                    recurringDate.get(Calendar.MONTH),
                    recurringDate.get(Calendar.DAY_OF_MONTH)
            );

            updateTaskPreferences(key, fullTask);
        }
        setResultAndFinish(task);
    }

    private void saveRecurringMonthlyTask(String task, String timeString) {
        Calendar startDate = (Calendar) selectedDate.clone();
        for (int month = 0; month < 120; month++) {
            Calendar recurringDate = (Calendar) startDate.clone();
            recurringDate.add(Calendar.MONTH, month);

            String fullTask = task + "|" + timeString + "|monthly";
            String key = getTaskKey(
                    recurringDate.get(Calendar.YEAR),
                    recurringDate.get(Calendar.MONTH),
                    recurringDate.get(Calendar.DAY_OF_MONTH)
            );

            updateTaskPreferences(key, fullTask);
        }
        setResultAndFinish(task);
    }

    private void saveRecurringWeeklyTask(String task, String timeString) {
        Calendar startDate = (Calendar) selectedDate.clone();
        for (int week = 0; week < 52 * 2; week++) {
            Calendar recurringDate = (Calendar) startDate.clone();
            recurringDate.add(Calendar.WEEK_OF_YEAR, week);

            String fullTask = task + "|" + timeString + "|weekly";
            String key = getTaskKey(
                    recurringDate.get(Calendar.YEAR),
                    recurringDate.get(Calendar.MONTH),
                    recurringDate.get(Calendar.DAY_OF_MONTH)
            );

            updateTaskPreferences(key, fullTask);
        }
        setResultAndFinish(task);
    }

    private void updateTaskPreferences(String key, String task) {
        // 首先檢查是否已經存在相同的任務
        String existingTasks = taskPreferences.getString(key, "");

        // 如果已有任務，檢查新任務是否重複
        if (!existingTasks.isEmpty()) {
            String[] tasks = existingTasks.split("\n");
            for (String existingTask : tasks) {
                if (existingTask.equals(task)) {
                    // 如果找到完全相同的任務，直接返回，不添加
                    return;
                }
            }
            // 如果沒有重複，則添加新任務
            existingTasks = existingTasks + "\n" + task;
        } else {
            // 如果沒有現有任務，直接設置新任務
            existingTasks = task;
        }

        // 保存更新後的任務
        taskPreferences.edit().putString(key, existingTasks).apply();
    }

    private void setResultAndFinish(String task) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("task", task);
        resultIntent.putExtra("year", selectedDate.get(Calendar.YEAR));
        resultIntent.putExtra("month", selectedDate.get(Calendar.MONTH));
        resultIntent.putExtra("day", selectedDate.get(Calendar.DAY_OF_MONTH));

        // 發送廣播通知更新
        Intent updateIntent = new Intent("com.example.calendarapp.TASK_UPDATED");
        sendBroadcast(updateIntent);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("選擇日期")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate.setTimeInMillis(selection);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formattedDate = sdf.format(selectedDate.getTime());

            etDate.setText(formattedDate);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private String getTaskKey(int year, int month, int day) {
        return year + "-" + month + "-" + day;
    }
}