package com.example.calendarapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "TaskReminderChannel";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        String task = intent.getStringExtra("task");
        String time = intent.getStringExtra("time");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)  // 使用新建立的圖標
                .setContentTitle("待辦事項提醒")
                .setContentText(task + " - " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // 提高優先級
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);  // 在鎖定螢幕上顯示完整通知

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH  // 提高重要性
            );

            channel.setDescription("Calendar App Task Reminders");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}