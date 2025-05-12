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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class GoalsActivity : Activity() {
    private lateinit var goalsContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rootLayout: LinearLayout
    private lateinit var dailyCaloriesInput: EditText
    private lateinit var caloriesProgressText: TextView
    private lateinit var caloriesRemainingText: TextView
    private val DAILY_CALORIES_KEY = "daily_calories"
    private val DIARY_ENTRIES_KEY = "diary_entries"
    private val colorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.helloworld.COLOR_CHANGED") {
                val color = intent.getIntExtra("color", Color.parseColor("#E8F5E9"))
                rootLayout.setBackgroundColor(color)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)
        
        rootLayout = findViewById(R.id.rootLayout)
        goalsContainer = findViewById(R.id.goalsContainer)
        sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
        dailyCaloriesInput = findViewById(R.id.dailyCaloriesInput)
        caloriesProgressText = findViewById(R.id.caloriesProgressText)
        caloriesRemainingText = findViewById(R.id.caloriesRemainingText)
        
        // Применяем сохраненный цвет фона
        val savedColor = sharedPreferences.getInt("backgroundColor", Color.parseColor("#E8F5E9"))
        rootLayout.setBackgroundColor(savedColor)
        
        // Регистрируем receiver для получения изменений цвета
        registerReceiver(colorReceiver, IntentFilter("com.example.helloworld.COLOR_CHANGED"))
        
        loadDailyCalories()
        updateProgress()
        loadGoals()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(colorReceiver)
    }

    private fun loadDailyCalories() {
        val savedCalories = sharedPreferences.getInt(DAILY_CALORIES_KEY, 0)
        if (savedCalories > 0) {
            dailyCaloriesInput.setText(savedCalories.toString())
        }
    }

    fun saveDailyCalories(view: View) {
        val caloriesText = dailyCaloriesInput.text.toString()
        if (caloriesText.isNotEmpty()) {
            val calories = caloriesText.toInt()
            sharedPreferences.edit().putInt(DAILY_CALORIES_KEY, calories).apply()
            Toast.makeText(this, "Дневная норма сохранена", Toast.LENGTH_SHORT).show()
            updateProgress()
        } else {
            Toast.makeText(this, "Введите дневную норму калорий", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProgress() {
        val dailyCalories = sharedPreferences.getInt(DAILY_CALORIES_KEY, 0)
        val entriesJson = sharedPreferences.getString(DIARY_ENTRIES_KEY, "[]")
        val entriesArray = org.json.JSONArray(entriesJson)
        
        var totalCalories = 0
        for (i in 0 until entriesArray.length()) {
            val entry = entriesArray.getJSONObject(i)
            totalCalories += entry.getInt("calories")
        }
        
        caloriesProgressText.text = "Потреблено калорий: $totalCalories"
        val remaining = dailyCalories - totalCalories
        caloriesRemainingText.text = "Осталось калорий: $remaining"
    }

    fun goBack(view: View) {
        finish()
    }

    private fun loadGoals() {
        val goalsJson = sharedPreferences.getString("goals", "[]")
        val goalsArray = JSONArray(goalsJson)
        
        for (i in 0 until goalsArray.length()) {
            val goal = goalsArray.getJSONObject(i)
            addGoalToView(goal)
        }
    }

    private fun addGoalToView(goal: JSONObject) {
        val goalView = layoutInflater.inflate(R.layout.goal_item, goalsContainer, false)
        
        goalView.findViewById<TextView>(R.id.goalName).text = goal.getString("name")
        goalView.findViewById<TextView>(R.id.goalProgress).text = "Прогресс: ${goal.getInt("progress")}%"
        
        goalsContainer.addView(goalView)
    }
} 