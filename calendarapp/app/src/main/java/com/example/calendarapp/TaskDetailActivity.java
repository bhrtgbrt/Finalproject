package com.example.calendarapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TaskDetailActivity extends AppCompatActivity {
    private TextView tvDate;
    private TextView tvWeather;
    private ListView lvTasks;
    private SharedPreferences taskPreferences;
    private ArrayList<String> tasksList;
    private ArrayList<String> rawTasksList;
    private ArrayAdapter<String> adapter;
    private int year, month, day;
    private WeatherService weatherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initializeViews();
        setupBackButton();
        setupTaskDeletion();
        loadAndDisplayTasks();

        // 初始化天氣服務並加載天氣數據
        weatherService = WeatherService.getInstance();
        loadWeatherData();
    }

    private void initializeViews() {
        tvDate = findViewById(R.id.tvDate);
        tvWeather = findViewById(R.id.tvWeather);
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
            Set<String> uniqueTasks = new LinkedHashSet<>();

            for (String task : tasks) {
                if (!task.trim().isEmpty() && uniqueTasks.add(task)) {
                    rawTasksList.add(task);
                    tasksList.add(formatTaskForDisplay(task));
                }
            }

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

    private void loadWeatherData() {
        try {
            weatherService.getService()
                    .getWeather(weatherService.getApiKey(), "大安區", "MaxT,MinT,平均溫度")
                    .enqueue(new Callback<WeatherService.WeatherResponse>() {
                        @Override
                        public void onResponse(Call<WeatherService.WeatherResponse> call,
                                               Response<WeatherService.WeatherResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                WeatherService.WeatherResponse weatherResponse = response.body();
                                if (weatherResponse.getRecords() != null &&
                                        weatherResponse.getRecords().getLocations() != null &&
                                        !weatherResponse.getRecords().getLocations().isEmpty()) {

                                    List<WeatherService.WeatherResponse.LocationData> locationData =
                                            weatherResponse.getRecords().getLocations().get(0).getLocationData();

                                    if (locationData != null && !locationData.isEmpty()) {
                                        WeatherService.WeatherResponse.LocationData data = locationData.get(0);
                                        Map<String, String> weatherDataByDate = parseWeatherDataByDate(data.getWeatherElements());

                                        // 顯示當前日期的天氣資訊
                                        String key = String.format("%d-%02d-%02d", year, month + 1, day);
                                        String weatherInfo = weatherDataByDate.getOrDefault(key, "無法獲取天氣資訊");
                                        tvWeather.setText(weatherInfo);
                                    }
                                }
                            } else {
                                tvWeather.setText("載入天氣數據失敗");
                            }
                        }

                        @Override
                        public void onFailure(Call<WeatherService.WeatherResponse> call, Throwable t) {
                            tvWeather.setText("網路連線失敗，請稍後再試\n" + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            tvWeather.setText("載入天氣資訊時發生錯誤");
        }
    }


    private Map<String, String> parseWeatherDataByDate(List<WeatherService.WeatherResponse.WeatherElement> elements) {
        Map<String, WeatherData> dailyWeatherInfo = new LinkedHashMap<>();

        if (elements != null) {
            for (WeatherService.WeatherResponse.WeatherElement element : elements) {
                for (WeatherService.WeatherResponse.TimeData timeData : element.getTime()) {
                    if (timeData != null && timeData.getElementValues() != null &&
                            !timeData.getElementValues().isEmpty()) {

                        String date = timeData.getStartTime().split("T")[0]; // 提取日期
                        String value = timeData.getElementValues().get(0).getValue();

                        // 初始化每天的天氣資訊
                        dailyWeatherInfo.putIfAbsent(date, new WeatherData());

                        WeatherData weatherData = dailyWeatherInfo.get(date);

                        // 如果該元素已經有值，跳過（確保只添加第一筆）
                        switch (element.getElementName()) {
                            case "最高溫度":
                            case "MaxT":
                                if (weatherData.getMaxTemperature() == null) {
                                    weatherData.setMaxTemperature(value);
                                }
                                break;
                            case "最低溫度":
                            case "MinT":
                                if (weatherData.getMinTemperature() == null) {
                                    weatherData.setMinTemperature(value);
                                }
                                break;
                            case "平均溫度":
                                if (weatherData.getAvgTemperature() == null) {
                                    weatherData.setAvgTemperature(value);
                                }
                                break;
                        }
                    }
                }
            }
        }

        // 將 WeatherData 轉換為顯示字串
        Map<String, String> weatherDisplayInfo = new LinkedHashMap<>();
        for (Map.Entry<String, WeatherData> entry : dailyWeatherInfo.entrySet()) {
            weatherDisplayInfo.put(entry.getKey(), entry.getValue().toDisplayString());
        }

        return weatherDisplayInfo;
    }

    private static class WeatherData {
        private String maxTemperature;
        private String minTemperature;
        private String avgTemperature;

        public void setMaxTemperature(String maxTemperature) {
            this.maxTemperature = maxTemperature;
        }

        public void setMinTemperature(String minTemperature) {
            this.minTemperature = minTemperature;
        }

        public void setAvgTemperature(String avgTemperature) {
            this.avgTemperature = avgTemperature;
        }

        public String getMaxTemperature() {
            return maxTemperature;
        }

        public String getMinTemperature() {
            return minTemperature;
        }

        public String getAvgTemperature() {
            return avgTemperature;
        }

        public String toDisplayString() {
            return String.format(
                    "平均溫度: %s°C\n最高溫度: %s°C\n最低溫度: %s°C",
                    avgTemperature != null ? avgTemperature : "未知",
                    maxTemperature != null ? maxTemperature : "未知",
                    minTemperature != null ? minTemperature : "未知"
            );
        }
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