package com.example.finalproject

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class AddTodoActivity : AppCompatActivity() {

    private lateinit var etTodoInput: EditText
    private lateinit var tvDatePicker: TextView
    private lateinit var btnAddTodo: FloatingActionButton
    private lateinit var btnBackToMain: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        // 綁定元件
        etTodoInput = findViewById(R.id.etTodoInput)
        tvDatePicker = findViewById(R.id.tvDatePicker)
        btnAddTodo = findViewById(R.id.btnAddTodo)
        btnBackToMain = findViewById(R.id.btnBackToMain)

        // 日期選擇功能
        tvDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        // 返回主畫面按鈕功能
        btnBackToMain.setOnClickListener {
            finish()
        }

        // 新增待辦事項功能
        btnAddTodo.setOnClickListener {
            addTodo()
        }
    }

    // 彈出日期選擇器
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                tvDatePicker.text = date
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    // 新增待辦事項的邏輯
    private fun addTodo() {
        val todoText = etTodoInput.text.toString()
        val date = tvDatePicker.text.toString()

        if (todoText.isEmpty()) {
            Toast.makeText(this, "請輸入待辦事項", Toast.LENGTH_SHORT).show()
            return
        }
        if (date == "選擇日期") {
            Toast.makeText(this, "請選擇日期", Toast.LENGTH_SHORT).show()
            return
        }

        // 使用 Intent 傳送資料回主畫面
        val intent = intent
        intent.putExtra("TODO_TEXT", todoText)
        intent.putExtra("TODO_DATE", date)
        setResult(RESULT_OK, intent)
        finish() // 結束 Activity
    }
}
