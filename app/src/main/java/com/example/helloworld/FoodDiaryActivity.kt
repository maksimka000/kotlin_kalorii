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
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// Активность для отображения дневника питания
class FoodDiaryActivity : Activity() {
    // Переменные для хранения ссылок на элементы интерфейса
    private lateinit var foodEntriesContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rootLayout: LinearLayout

    // Форматы для отображения даты и времени
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Ключи для сохранения данных
    private val DIARY_ENTRIES_KEY = "diary_entries"
    private val TAG = "FoodDiaryActivity"

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
        setContentView(R.layout.activity_food_diary)
        
        // Инициализация элементов интерфейса
        rootLayout = findViewById(R.id.rootLayout)
        foodEntriesContainer = findViewById(R.id.foodEntriesContainer)
        sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
        
        // Применение сохраненного цвета фона
        val savedColor = sharedPreferences.getInt("backgroundColor", Color.parseColor("#E8F5E9"))
        rootLayout.setBackgroundColor(savedColor)
        
        // Регистрация приемника для отслеживания изменений цвета
        registerReceiver(colorReceiver, IntentFilter("com.example.helloworld.COLOR_CHANGED"))
        
        // Загрузка записей дневника питания
        loadDiaryEntries()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена регистрации приемника при уничтожении активности
        unregisterReceiver(colorReceiver)
    }

    // Обработчик нажатия кнопки "Назад"
    fun goBack(view: View) {
        finish()
    }

    // Обработчик нажатия кнопки "Добавить продукт"
    fun addFood(view: View) {
        val intent = Intent(this, AddProductActivity::class.java)
        startActivity(intent)
    }

    // Обработчик нажатия кнопки "Добавить рецепт"
    fun addRecipe(view: View) {
        val intent = Intent(this, RecipesActivity::class.java)
        startActivity(intent)
    }

    // Загрузка записей из дневника питания
    private fun loadDiaryEntries() {
        try {
            // Получение записей из SharedPreferences
            val entriesJson = sharedPreferences.getString(DIARY_ENTRIES_KEY, "[]")
            Log.d(TAG, "Loading entries: $entriesJson")
            
            val entriesArray = JSONArray(entriesJson)
            Log.d(TAG, "Number of entries: ${entriesArray.length()}")
            
            // Отображение каждой записи
            for (i in 0 until entriesArray.length()) {
                val entry = entriesArray.getJSONObject(i)
                Log.d(TAG, "Processing entry: ${entry.toString()}")
                addEntryToView(entry)
            }
        } catch (e: Exception) {
            // Обработка ошибок при загрузке записей
            Log.e(TAG, "Error loading entries: ${e.message}")
            e.printStackTrace()
            // Очистка записей при ошибке
            sharedPreferences.edit().putString(DIARY_ENTRIES_KEY, "[]").apply()
        }
    }

    // Добавление записи в интерфейс
    private fun addEntryToView(entry: JSONObject) {
        try {
            // Создание нового элемента для отображения записи
            val entryView = layoutInflater.inflate(R.layout.diary_entry, foodEntriesContainer, false)
            
            // Получение данных из записи
            val type = entry.optString("type", "product")
            val name = entry.getString(if (type == "recipe") "name" else "productName")
            val calories = entry.getInt("calories")
            val time = entry.getString("time")
            
            Log.d(TAG, "Adding entry: type=$type, name=$name, calories=$calories, time=$time")
            
            // Установка данных в элементы интерфейса
            entryView.findViewById<TextView>(R.id.entryName).text = name
            entryView.findViewById<TextView>(R.id.entryCalories).text = "Калории: $calories"
            entryView.findViewById<TextView>(R.id.entryTime).text = time
            
            // Дополнительное форматирование для рецептов
            if (type == "recipe") {
                val ingredients = entry.getString("ingredients")
                entryView.findViewById<TextView>(R.id.entryName).text = "Рецепт: $name"
                entryView.findViewById<TextView>(R.id.entryCalories).text = "Калории: $calories\nИнгредиенты: $ingredients"
            }
            
            // Добавление элемента в контейнер
            foodEntriesContainer.addView(entryView)
        } catch (e: Exception) {
            // Обработка ошибок при добавлении записи
            Log.e(TAG, "Error adding entry to view: ${e.message}")
            e.printStackTrace()
        }
    }

    // Обновление данных при возвращении на экран дневника питания
    override fun onResume() {
        super.onResume()
        // Очистка контейнера и перезагрузка записей
        foodEntriesContainer.removeAllViews()
        loadDiaryEntries()
    }
} 