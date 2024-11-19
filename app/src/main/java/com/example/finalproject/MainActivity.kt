package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    // 用來存儲待辦事項的清單
    private val todoList = mutableListOf<String>()
    private lateinit var todoAdapter: ArrayAdapter<String>

    // 用來顯示當前的月份和年份
    private lateinit var monthYearTextView: TextView

    companion object {
        private const val ADD_TODO_REQUEST_CODE = 1 // 用於辨識 AddTodoActivity 的返回結果
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 設定月份年份的文字元件
        monthYearTextView = findViewById(R.id.monthYearTextView)
        monthYearTextView.text = "November 2024" // 預設月份年份

        // 初始化 ListView 和 Adapter
        val listView: ListView = findViewById(R.id.listView)
        todoAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todoList)
        listView.adapter = todoAdapter

        // 左箭頭按鈕切換到前一個月
        val leftArrowButton: FloatingActionButton = findViewById(R.id.leftArrowButton)
        leftArrowButton.setOnClickListener {
            // TODO: 加入邏輯切換到前一個月
        }

        // 右箭頭按鈕切換到下一個月
        val rightArrowButton: FloatingActionButton = findViewById(R.id.rightArrowButton)
        rightArrowButton.setOnClickListener {
            // TODO: 加入邏輯切換到下一個月
        }

        // 浮動按鈕點擊事件，跳轉到 AddTodoActivity
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivityForResult(intent, ADD_TODO_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 處理來自 AddTodoActivity 的返回結果
        if (requestCode == ADD_TODO_REQUEST_CODE && resultCode == RESULT_OK) {
            val todoText = data?.getStringExtra("TODO_TEXT") ?: return
            val todoDate = data?.getStringExtra("TODO_DATE") ?: return

            // 新增到清單並更新畫面
            addTodoToList(todoDate, todoText)
        }
    }

    // 新增待辦事項到清單
    private fun addTodoToList(date: String, todoText: String) {
        todoList.add("$date: $todoText")
        todoAdapter.notifyDataSetChanged() // 通知 Adapter 更新 UI
    }
}
