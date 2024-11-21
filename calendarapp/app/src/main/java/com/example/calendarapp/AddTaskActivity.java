package com.example.calendarapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
    private Spinner spRecurrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initializeViews();
        setupButtons();
    }

    private void initializeViews() {
        etTask = findViewById(R.id.etTask);
        etDate = findViewById(R.id.etDate);
        tpTime = findViewById(R.id.tpTime);
        spRecurrence = findViewById(R.id.spRecurrence);
        selectedDate = Calendar.getInstance();
        taskPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);

        etDate.setOnClickListener(v -> showDatePicker());

        // 設定重複選項
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

        int hour = tpTime.getCurrentHour();
        int minute = tpTime.getCurrentMinute();
        String timeString = String.format("%02d:%02d", hour, minute);

        String recurrenceOption = spRecurrence.getSelectedItem().toString();

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
        String existingTasks = taskPreferences.getString(key, "");
        String updatedTasks = existingTasks.isEmpty() ?
                task :
                existingTasks + "\n" + task;

        taskPreferences.edit().putString(key, updatedTasks).apply();
    }

    private void setResultAndFinish(String task) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("task", task);
        resultIntent.putExtra("year", selectedDate.get(Calendar.YEAR));
        resultIntent.putExtra("month", selectedDate.get(Calendar.MONTH));
        resultIntent.putExtra("day", selectedDate.get(Calendar.DAY_OF_MONTH));

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