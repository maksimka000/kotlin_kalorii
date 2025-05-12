package com.example.helloworld

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

// Главная активность приложения, содержащая навигационное меню
class MainActivity : Activity() {
    // Переменные для хранения ссылок на элементы интерфейса
    private lateinit var rootLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dailyCaloriesText: TextView
    private lateinit var caloriesProgressText: TextView
    private lateinit var caloriesRemainingText: TextView

    // Ключи для сохранения данных
    private val DAILY_CALORIES_KEY = "daily_calories"
    private val DIARY_ENTRIES_KEY = "diary_entries"

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
        setContentView(R.layout.activity_main)
        
        // Инициализация элементов интерфейса
        rootLayout = findViewById(R.id.rootLayout)
        dailyCaloriesText = findViewById(R.id.dailyCaloriesText)
        caloriesProgressText = findViewById(R.id.caloriesProgressText)
        caloriesRemainingText = findViewById(R.id.caloriesRemainingText)
        sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
        
        // Применение сохраненного цвета фона
        val savedColor = sharedPreferences.getInt("backgroundColor", Color.parseColor("#E8F5E9"))
        rootLayout.setBackgroundColor(savedColor)
        
        // Регистрация приемника для отслеживания изменений цвета
        registerReceiver(colorReceiver, IntentFilter("com.example.helloworld.COLOR_CHANGED"))
        
        // Загрузка и отображение данных о калориях
        loadDailyCalories()
        updateProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена регистрации приемника при уничтожении активности
        unregisterReceiver(colorReceiver)
    }

    // Обработчик нажатия кнопки "Дневник питания"
    fun openFoodDiary(view: View) {
        val intent = Intent(this, FoodDiaryActivity::class.java)
        startActivity(intent)
    }

    // Обработчик нажатия кнопки "Цели и прогресс"
    fun openGoals(view: View) {
        val intent = Intent(this, GoalsActivity::class.java)
        startActivity(intent)
    }

    // Обработчик нажатия кнопки "Рецепты"
    fun openRecipes(view: View) {
        val intent = Intent(this, RecipesActivity::class.java)
        startActivity(intent)
    }

    // Обработчик нажатия кнопки "Настройки"
    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    // Загрузка дневной нормы калорий из SharedPreferences
    private fun loadDailyCalories() {
        val dailyCalories = sharedPreferences.getInt(DAILY_CALORIES_KEY, 0)
        dailyCaloriesText.text = "Дневная норма: $dailyCalories ккал"
    }

    // Обновление прогресса потребления калорий
    private fun updateProgress() {
        try {
            // Получение дневной нормы калорий
            val dailyCalories = sharedPreferences.getInt(DAILY_CALORIES_KEY, 0)
            if (dailyCalories == 0) {
                // Если норма не установлена, показываем сообщение
                caloriesProgressText.text = "Установите дневную норму калорий в настройках"
                caloriesRemainingText.text = ""
                return
            }

            // Получение записей из дневника питания
            val entriesJson = sharedPreferences.getString(DIARY_ENTRIES_KEY, "[]")
            val entriesArray = org.json.JSONArray(entriesJson)
            
            // Подсчет общего количества потребленных калорий
            var totalCalories = 0
            for (i in 0 until entriesArray.length()) {
                val entry = entriesArray.getJSONObject(i)
                totalCalories += entry.getInt("calories")
            }

            // Расчет процента потребленных калорий
            val progress = (totalCalories.toDouble() / dailyCalories * 100).toInt()
            val remaining = dailyCalories - totalCalories

            // Обновление текстовых полей с информацией о прогрессе
            caloriesProgressText.text = "Потреблено: $totalCalories ккал ($progress%)"
            caloriesRemainingText.text = "Осталось: $remaining ккал"
        } catch (e: Exception) {
            // Обработка ошибок при обновлении прогресса
            e.printStackTrace()
            caloriesProgressText.text = "Ошибка при расчете прогресса"
            caloriesRemainingText.text = ""
        }
    }

    // Обновление данных при возвращении на главный экран
    override fun onResume() {
        super.onResume()
        loadDailyCalories()
        updateProgress()
    }
}