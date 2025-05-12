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

class MyProductsActivity : Activity() {
    private lateinit var productsContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val PRODUCTS_KEY = "my_products"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_products)
        productsContainer = findViewById(R.id.productsContainer)
        sharedPreferences = getSharedPreferences("FoodDiary", MODE_PRIVATE)
        loadProducts()
    }

    private fun loadProducts() {
        val productsJson = sharedPreferences.getString(PRODUCTS_KEY, "[]")
        val productsArray = JSONArray(productsJson)
        
        for (i in 0 until productsArray.length()) {
            val product = productsArray.getJSONObject(i)
            val name = product.getString("name")
            val calories = product.getInt("calories")
            addProductToView(name, calories)
        }
    }

    private fun saveProducts() {
        val productsArray = JSONArray()
        for (i in 0 until productsContainer.childCount) {
            val productLayout = productsContainer.getChildAt(i) as LinearLayout
            val nameText = productLayout.getChildAt(0) as TextView
            val caloriesText = productLayout.getChildAt(1) as TextView
            
            val product = JSONObject().apply {
                put("name", nameText.text.toString())
                put("calories", caloriesText.text.toString().replace(" ккал", "").toInt())
            }
            productsArray.put(product)
        }
        
        sharedPreferences.edit().putString(PRODUCTS_KEY, productsArray.toString()).apply()
    }

    fun addProduct(view: View) {
        val intent = Intent(this, AddProductActivity::class.java)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val productName = data?.getStringExtra("productName") ?: ""
            val calories = data?.getIntExtra("calories", 0) ?: 0
            addProductToView(productName, calories)
            saveProducts()
        }
    }

    private fun addProductToView(name: String, calories: Int) {
        val productLayout = LinearLayout(this)
        productLayout.orientation = LinearLayout.HORIZONTAL
        productLayout.setPadding(16, 8, 16, 8)
        productLayout.background = getDrawable(android.R.drawable.edit_text)

        val nameText = TextView(this)
        nameText.text = name
        nameText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)

        val caloriesText = TextView(this)
        caloriesText.text = "$calories ккал"
        caloriesText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        productLayout.addView(nameText)
        productLayout.addView(caloriesText)

        productsContainer.addView(productLayout)
    }
} 