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
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class RecipesActivity : Activity() {
    private lateinit var recipesContainer: LinearLayout
    private lateinit var recipeNameInput: EditText
    private lateinit var recipeIngredientsInput: EditText
    private lateinit var recipeCaloriesInput: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rootLayout: LinearLayout
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val RECIPES_KEY = "recipes"
    private val DIARY_ENTRIES_KEY = "diary_entries"
    private val TAG = "RecipesActivity"
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
        setContentView(R.layout.activity_recipes)
        
        try {
            rootLayout = findViewById(R.id.rootLayout)
            recipesContainer = findViewById(R.id.recipesContainer)
            recipeNameInput = findViewById(R.id.recipeNameInput)
            recipeIngredientsInput = findViewById(R.id.recipeIngredientsInput)
            recipeCaloriesInput = findViewById(R.id.recipeCaloriesInput)
            sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
            
            // Применяем сохраненный цвет фона
            val savedColor = sharedPreferences.getInt("backgroundColor", Color.parseColor("#E8F5E9"))
            rootLayout.setBackgroundColor(savedColor)
            
            // Регистрируем receiver для получения изменений цвета
            registerReceiver(colorReceiver, IntentFilter("com.example.helloworld.COLOR_CHANGED"))
            
            loadRecipes()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(colorReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
    }

    fun goBack(view: View) {
        finish()
    }

    fun addRecipe(view: View) {
        try {
            val name = recipeNameInput.text.toString().trim()
            val ingredients = recipeIngredientsInput.text.toString().trim()
            val caloriesText = recipeCaloriesInput.text.toString().trim()
            
            if (name.isEmpty() || ingredients.isEmpty() || caloriesText.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return
            }
            
            val calories = caloriesText.toInt()
            
            // Создаем новый рецепт
            val recipe = JSONObject().apply {
                put("name", name)
                put("ingredients", ingredients)
                put("calories", calories)
            }
            
            // Сохраняем рецепт
            val recipesJson = sharedPreferences.getString(RECIPES_KEY, "[]")
            val recipesArray = JSONArray(recipesJson)
            recipesArray.put(recipe)
            sharedPreferences.edit().putString(RECIPES_KEY, recipesArray.toString()).apply()
            
            // Добавляем рецепт в дневник
            val diaryEntry = JSONObject().apply {
                put("type", "recipe")
                put("name", name)
                put("ingredients", ingredients)
                put("calories", calories)
                put("time", "${dateFormat.format(Date())} ${timeFormat.format(Date())}")
            }
            
            val entriesJson = sharedPreferences.getString(DIARY_ENTRIES_KEY, "[]")
            val entriesArray = JSONArray(entriesJson)
            
            val newEntriesArray = JSONArray()
            newEntriesArray.put(diaryEntry)
            for (i in 0 until entriesArray.length()) {
                newEntriesArray.put(entriesArray.getJSONObject(i))
            }
            
            sharedPreferences.edit().putString(DIARY_ENTRIES_KEY, newEntriesArray.toString()).apply()
            
            Toast.makeText(this, "Рецепт добавлен в дневник", Toast.LENGTH_SHORT).show()
            finish()
            
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Введите корректное количество калорий", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding recipe: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadRecipes() {
        try {
            val recipesJson = sharedPreferences.getString(RECIPES_KEY, "[]")
            Log.d(TAG, "Loading recipes: $recipesJson")
            
            val recipesArray = JSONArray(recipesJson)
            Log.d(TAG, "Number of recipes: ${recipesArray.length()}")
            
            for (i in 0 until recipesArray.length()) {
                val recipe = recipesArray.getJSONObject(i)
                Log.d(TAG, "Processing recipe: ${recipe.toString()}")
                addRecipeToView(recipe)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading recipes: ${e.message}")
            e.printStackTrace()
            sharedPreferences.edit().putString(RECIPES_KEY, "[]").apply()
        }
    }

    private fun addRecipeToView(recipe: JSONObject) {
        try {
            val recipeView = layoutInflater.inflate(R.layout.recipe_item, recipesContainer, false)
            
            val name = recipe.getString("name")
            val ingredients = recipe.getString("ingredients")
            val calories = recipe.getInt("calories")
            
            Log.d(TAG, "Adding recipe to view: name=$name, calories=$calories")
            
            recipeView.findViewById<TextView>(R.id.recipeName).text = name
            recipeView.findViewById<TextView>(R.id.recipeIngredients).text = "Ингредиенты: $ingredients"
            recipeView.findViewById<TextView>(R.id.recipeCalories).text = "Калории: $calories"
            
            recipesContainer.addView(recipeView)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding recipe to view: ${e.message}")
            e.printStackTrace()
        }
    }
} 