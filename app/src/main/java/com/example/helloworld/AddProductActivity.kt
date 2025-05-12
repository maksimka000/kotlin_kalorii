package com.example.helloworld

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// Активность для добавления нового продукта в дневник питания
class AddProductActivity : Activity() {
    // Переменные для хранения ссылок на элементы интерфейса
    private lateinit var productNameInput: EditText
    private lateinit var productCaloriesInput: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rootLayout: LinearLayout

    // Форматы для отображения даты и времени
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Ключи для сохранения данных
    private val DIARY_ENTRIES_KEY = "diary_entries"
    private val TAG = "AddProductActivity"

    // Приемник для отслеживания изменений цвета фона
    private val colorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.helloworld.COLOR_CHANGED") {
                // Получаем новый цвет из Intent и применяем его к корневому layout
                val color = intent.getIntExtra("color", Color.parseColor("#E8F5E9"))
                rootLayout.setBackgroundColor(color)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        
        try {
            // Инициализация элементов интерфейса
            rootLayout = findViewById(R.id.rootLayout)
            productNameInput = findViewById(R.id.productNameInput)
            productCaloriesInput = findViewById(R.id.productCaloriesInput)
            sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
            
            // Применение сохраненного цвета фона
            val savedColor = sharedPreferences.getInt("backgroundColor", Color.parseColor("#E8F5E9"))
            rootLayout.setBackgroundColor(savedColor)
            
            // Регистрация приемника для отслеживания изменений цвета
            registerReceiver(colorReceiver, IntentFilter("com.example.helloworld.COLOR_CHANGED"))
        } catch (e: Exception) {
            // Обработка ошибок при инициализации
            Log.e(TAG, "Error in onCreate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Отмена регистрации приемника при уничтожении активности
            unregisterReceiver(colorReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
    }

    // Обработчик нажатия кнопки "Назад"
    fun goBack(view: View) {
        finish()
    }

    // Обработчик нажатия кнопки "Сохранить"
    fun saveProduct(view: View) {
        try {
            // Получение и проверка введенных данных
            val name = productNameInput.text.toString().trim()
            val caloriesText = productCaloriesInput.text.toString().trim()
            
            if (name.isEmpty() || caloriesText.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Преобразование калорий в число
            val calories = caloriesText.toInt()
            
            // Создание новой записи для дневника
            val diaryEntry = JSONObject().apply {
                put("productName", name)
                put("calories", calories)
                put("time", "${dateFormat.format(Date())} ${timeFormat.format(Date())}")
            }
            
            // Получение существующих записей
            val entriesJson = sharedPreferences.getString(DIARY_ENTRIES_KEY, "[]")
            val entriesArray = JSONArray(entriesJson)
            
            // Добавление новой записи в начало массива
            val newEntriesArray = JSONArray()
            newEntriesArray.put(diaryEntry)
            for (i in 0 until entriesArray.length()) {
                newEntriesArray.put(entriesArray.getJSONObject(i))
            }
            
            // Сохранение обновленного массива
            sharedPreferences.edit().putString(DIARY_ENTRIES_KEY, newEntriesArray.toString()).apply()
            
            Toast.makeText(this, "Продукт добавлен в дневник", Toast.LENGTH_SHORT).show()
            finish()
            
        } catch (e: NumberFormatException) {
            // Обработка ошибки при вводе некорректного числа калорий
            Toast.makeText(this, "Введите корректное количество калорий", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Обработка других ошибок при сохранении
            Log.e(TAG, "Error saving product: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
} 