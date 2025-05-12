package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast

class SettingsActivity : Activity() {
    private lateinit var rootLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        rootLayout = findViewById(R.id.rootLayout)
        sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
        
        // Применяем сохраненный цвет фона
        val savedColor = sharedPreferences.getInt("backgroundColor", Color.parseColor("#E8F5E9"))
        rootLayout.setBackgroundColor(savedColor)
    }

    fun goBack(view: View) {
        finish()
    }

    fun changeBackgroundColor(view: View) {
        val color = when ((view as android.widget.Button).text.toString()) {
            "Светлый" -> Color.parseColor("#E8F5E9")
            "Темный" -> Color.DKGRAY
            "Синий" -> Color.BLUE
            "Зеленый" -> Color.GREEN
            else -> Color.parseColor("#E8F5E9")
        }
        
        // Сохраняем выбранный цвет
        sharedPreferences.edit().putInt("backgroundColor", color).apply()
        
        // Применяем цвет к текущей активности
        rootLayout.setBackgroundColor(color)
        
        // Отправляем broadcast для обновления цвета в других активностях
        val intent = Intent("com.example.helloworld.COLOR_CHANGED")
        intent.putExtra("color", color)
        sendBroadcast(intent)
        
        Toast.makeText(this, "Цвет фона изменен", Toast.LENGTH_SHORT).show()
    }
} 