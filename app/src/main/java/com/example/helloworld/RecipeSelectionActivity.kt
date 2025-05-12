package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class RecipeSelectionActivity : Activity() {
    private lateinit var recipesContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val RECIPES_KEY = "recipes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_selection)
        recipesContainer = findViewById(R.id.recipesContainer)
        sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
        loadRecipes()
    }

    private fun loadRecipes() {
        val recipesJson = sharedPreferences.getString(RECIPES_KEY, "[]")
        val recipesArray = JSONArray(recipesJson)
        
        for (i in 0 until recipesArray.length()) {
            val recipe = recipesArray.getJSONObject(i)
            addRecipeToView(
                recipe.getString("name"),
                recipe.getString("ingredients"),
                recipe.getInt("calories")
            )
        }
    }

    private fun addRecipeToView(name: String, ingredients: String, calories: Int) {
        val recipeLayout = LinearLayout(this)
        recipeLayout.orientation = LinearLayout.VERTICAL
        recipeLayout.setPadding(16, 8, 16, 8)
        recipeLayout.background = getDrawable(android.R.drawable.edit_text)
        recipeLayout.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("recipeName", name)
            resultIntent.putExtra("calories", calories)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        val nameText = TextView(this)
        nameText.text = name
        nameText.textSize = 18f
        nameText.setPadding(0, 0, 0, 8)

        val ingredientsText = TextView(this)
        ingredientsText.text = ingredients
        ingredientsText.setPadding(0, 0, 0, 8)

        val caloriesText = TextView(this)
        caloriesText.text = "$calories ккал"

        recipeLayout.addView(nameText)
        recipeLayout.addView(ingredientsText)
        recipeLayout.addView(caloriesText)

        recipesContainer.addView(recipeLayout)
    }
} 