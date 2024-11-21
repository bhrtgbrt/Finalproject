package com.example.calendarapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Calendar;

public class CalendarAdapter extends BaseAdapter {
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
    public int getCount() {
        return 42; // 6週 x 7天
    }

    public Calendar getDateAtPosition(int position) {
        Calendar date = (Calendar) calendar.clone();
        date.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1;
        int dayOffset = position - firstDayOfWeek;

        date.add(Calendar.DAY_OF_MONTH, dayOffset);
        return date;
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
            holder.tvLunarDate = convertView.findViewById(R.id.tvLunarDate);
            holder.tvTask = convertView.findViewById(R.id.tvTask);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Calendar currentDate = calculateDate(position);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        boolean isCurrentMonth = currentDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH);

        if (isCurrentMonth) {
            holder.tvDate.setText(String.valueOf(dayOfMonth));
            holder.tvDate.setTextColor(context.getResources().getColor(android.R.color.black));

            String lunarDate = getLunarDayString(currentDate);
            String lunarMonthDay = LunarCalendarUtils.getLunarMonthName(currentDate.get(Calendar.MONTH) + 1) +
                    LunarCalendarUtils.getLunarDayName(dayOfMonth);

            // 檢查是否為節日
            String festival = LunarCalendarUtils.getFestival(lunarMonthDay);
            if (!festival.isEmpty()) {
                holder.tvLunarDate.setTextColor(Color.RED);
                holder.tvLunarDate.setText(festival);
            } else {
                holder.tvLunarDate.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                holder.tvLunarDate.setText(lunarDate);
            }

            String key = getTaskKey(
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    dayOfMonth
            );
            String task = taskPreferences.getString(key, "");

            if (!task.isEmpty()) {
                String[] tasks = task.split("\n");
                String displayTask = tasks.length > 1 ?
                        tasks[0].split("\\|")[0] + "..." :
                        tasks[0].split("\\|")[0];
                holder.tvTask.setText(displayTask);
            } else {
                holder.tvTask.setText("");
            }
        } else {
            holder.tvDate.setText("");
            holder.tvLunarDate.setText("");
            holder.tvTask.setText("");
        }

        return convertView;
    }

    private String getLunarDayString(Calendar date) {
        String lunarDate = LunarCalendarUtils.getLunarDate(date);
        return lunarDate;
    }

    private static class ViewHolder {
        TextView tvDate;
        TextView tvLunarDate;
        TextView tvTask;
    }

    private Calendar calculateDate(int position) {
        Calendar date = (Calendar) calendar.clone();
        date.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1;
        int dayOffset = position - firstDayOfWeek;

        date.add(Calendar.DAY_OF_MONTH, dayOffset);
        return date;
    }

    public void addTask(int year, int month, int day, String task) {
        String key = getTaskKey(year, month, day);
        String existingTasks = taskPreferences.getString(key, "");

        String updatedTasks = existingTasks.isEmpty() ?
                task :
                existingTasks + "\n" + task;

        taskPreferences.edit().putString(key, updatedTasks).apply();
        notifyDataSetChanged();
    }

    private String getTaskKey(int year, int month, int day) {
        return year + "-" + month + "-" + day;
    }
}