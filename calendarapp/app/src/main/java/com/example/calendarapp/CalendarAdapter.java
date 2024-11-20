// CalendarAdapter.java
package com.example.calendarapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private Calendar calendar;
    private Map<String, String> tasks;
    private LayoutInflater inflater;

    public CalendarAdapter(Context context, Calendar calendar) {
        this.context = context;
        this.calendar = (Calendar) calendar.clone();
        this.tasks = new HashMap<>();
        this.inflater = LayoutInflater.from(context);
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
            holder.tvTask = convertView.findViewById(R.id.tvTask);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 計算日期
        Calendar currentDate = calculateDate(position);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        // 判斷是否為當前月份的日期
        boolean isCurrentMonth = currentDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH);

        if (isCurrentMonth) {
            // 設置日期
            holder.tvDate.setText(String.valueOf(dayOfMonth));
            holder.tvDate.setTextColor(context.getResources().getColor(android.R.color.black));

            // 顯示待辦事項
            String key = getTaskKey(
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    dayOfMonth
            );
            String task = tasks.get(key);
            holder.tvTask.setText(task != null ? task : "");
        } else {
            // 非當前月份的日期顯示為空
            holder.tvDate.setText("");
            holder.tvTask.setText("");
        }

        return convertView;
    }

    private Calendar calculateDate(int position) {
        Calendar date = (Calendar) calendar.clone();
        date.set(Calendar.DAY_OF_MONTH, 1); // 設置為當月第一天

        int firstDayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1; // 當月第一天是星期幾
        int dayOffset = position - firstDayOfWeek; // 計算偏移量

        date.add(Calendar.DAY_OF_MONTH, dayOffset);
        return date;
    }

    public void addTask(int year, int month, int day, String task) {
        String key = getTaskKey(year, month, day);
        tasks.put(key, task);
        notifyDataSetChanged();
    }

    private String getTaskKey(int year, int month, int day) {
        return year + "-" + month + "-" + day;
    }

    private static class ViewHolder {
        TextView tvDate;
        TextView tvTask;
    }
}