package com.example.finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddTodoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        // 設定返回按鈕功能
        val btnBackToMain = findViewById<FloatingActionButton>(R.id.btnBackToMain)
        btnBackToMain.setOnClickListener {
            // 結束當前 Activity 返回主畫面
            finish()
        }
    }
}