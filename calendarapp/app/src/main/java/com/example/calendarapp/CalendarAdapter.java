package com.example.calendarapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Calendar;

public class CalendarAdapter extends BaseAdapter {
    private static final String TAG = "CalendarAdapter";
    private Context context;
    private Calendar calendar;
    private SharedPreferences taskPreferences;
    private LayoutInflater inflater;

    public CalendarAdapter(Context context, Calendar calendar) {
        this.context = context;
        this.calendar = (Calendar) calendar.clone();
        this.taskPreferences = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public void notifyDataSetChanged() {
        // 重新獲取 SharedPreferences 實例以確保數據最新
        this.taskPreferences = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        super.notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return 42; // 6週 x 7天
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.calendar_cell, parent, false);
            holder = new ViewHolder();
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvLunar = convertView.findViewById(R.id.tvLunar);
            holder.tvTask = convertView.findViewById(R.id.tvTask);
            holder.tvHoliday = convertView.findViewById(R.id.tvHoliday);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 重置所有 TextView 的可見性
        holder.tvDate.setVisibility(View.VISIBLE);
        holder.tvLunar.setVisibility(View.GONE);
        holder.tvTask.setVisibility(View.GONE);
        holder.tvHoliday.setVisibility(View.GONE);

        // 計算日期
        Calendar currentDate = calculateDate(position);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        // 判斷是否為當前月份的日期
        boolean isCurrentMonth = currentDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH);

        if (isCurrentMonth) {
            // 設置陽曆日期
            holder.tvDate.setText(String.valueOf(dayOfMonth));
            holder.tvDate.setTextColor(context.getResources().getColor(android.R.color.black));

            // 設置農曆日期
            try {
                Lunar lunar = new Lunar(currentDate);
                String lunarText = getLunarDayText(lunar.getMonth(), lunar.getDay());

                if (lunarText != null && !lunarText.isEmpty()) {
                    holder.tvLunar.setText(lunarText);
                    holder.tvLunar.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                Log.e(TAG, "農曆轉換錯誤: " + e.getMessage());
                holder.tvLunar.setVisibility(View.GONE);
            }

            // 顯示節日
            String holiday = HolidayManager.getHoliday(currentDate);
            if (holiday != null && !holiday.isEmpty()) {
                holder.tvHoliday.setVisibility(View.VISIBLE);
                holder.tvHoliday.setText(holiday);
            }

            // 重新從 SharedPreferences 讀取並顯示待辦事項
            String key = getTaskKey(
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    dayOfMonth
            );

            // 直接從 SharedPreferences 讀取最新數據
            String task = taskPreferences.getString(key, "");

            if (!task.isEmpty()) {
                String[] tasks = task.split("\n");
                if (tasks.length > 0 && !tasks[0].trim().isEmpty()) {
                    String displayTask = tasks.length > 1 ?
                            tasks[0].split("\\|")[0] + "..." :
                            tasks[0].split("\\|")[0];
                    holder.tvTask.setText(displayTask);
                    holder.tvTask.setVisibility(View.VISIBLE);
                }
            }
        } else {
            // 非當前月份的日期顯示為空
            holder.tvDate.setText("");
            holder.tvDate.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void refreshData() {
        // 強制重新載入數據並更新視圖
        this.taskPreferences = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView tvDate;
        TextView tvLunar;
        TextView tvTask;
        TextView tvHoliday;
    }

    private String getLunarDayText(int lunarMonth, int lunarDay) {
        String[] chineseDayNames = {
                "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        };

        if (lunarDay == 1) {
            String[] chineseMonthNames = {
                    "正月", "二月", "三月", "四月", "五月", "六月",
                    "七月", "八月", "九月", "十月", "冬月", "臘月"
            };
            return chineseMonthNames[lunarMonth - 1];
        } else {
            if (lunarDay > 0 && lunarDay <= chineseDayNames.length) {
                return chineseDayNames[lunarDay - 1];
            } else {
                Log.e(TAG, "無效的農曆日期: " + lunarMonth + "月" + lunarDay + "日");
                return "";
            }
        }
    }

    private Calendar calculateDate(int position) {
        Calendar date = (Calendar) calendar.clone();
        date.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1;
        int dayOffset = position - firstDayOfWeek;

        date.add(Calendar.DAY_OF_MONTH, dayOffset);
        return date;
    }

    private String getTaskKey(int year, int month, int day) {
        return year + "-" + month + "-" + day;
    }

    public Calendar getDateAtPosition(int position) {
        return calculateDate(position);
    }

    public void addTask(int year, int month, int day, String task) {
        String key = getTaskKey(year, month, day);
        String existingTasks = taskPreferences.getString(key, "");
        String updatedTasks = existingTasks.isEmpty() ? task : existingTasks + "\n" + task;
        taskPreferences.edit().putString(key, updatedTasks).apply();
        notifyDataSetChanged();
    }
}