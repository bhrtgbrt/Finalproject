package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.unity3d.player.UnityPlayerGameActivity;

public class TransitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 啟動 UnityPlayerGameActivity
        Intent unityIntent = new Intent(this, UnityPlayerGameActivity.class);
        startActivity(unityIntent);

        // 設置延遲 6 秒後的邏輯
        new Handler().postDelayed(() -> {
            // 發送廣播請求 UnityPlayerGameActivity 關閉
            Intent closeUnityIntent = new Intent("CLOSE_UNITY_ACTIVITY");
            sendBroadcast(closeUnityIntent);

            // 啟動 MainActivity
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);

            // 結束自己
            finish();
        }, 6000); // 延遲 6000 毫秒 (6 秒)
    }
}