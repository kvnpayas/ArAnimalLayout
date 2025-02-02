package com.example.aranimallayout.util

import android.content.Context
import com.example.aranimallayout.Animal
import com.example.aranimallayout.Category
import org.json.JSONArray
import org.json.JSONObject

object JsonUtil {
    fun getCategoriesFromAssets(context: Context): List<Category> {
        val jsonString = context.assets.open("animals.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val categories = mutableListOf<Category>()

        for (i in 0 until jsonArray.length()) {
            val categoryObject = jsonArray.getJSONObject(i)
            val id = categoryObject.getInt("id")
            val name = categoryObject.getString("name")
            val imageUrl = categoryObject.getString("imageUrl")
            val animalsArray = categoryObject.getJSONArray("animals")
            val animals = getAnimalsFromJsonArray(animalsArray)

            val category = Category(id, name, imageUrl, animals)
            categories.add(category)
        }

        return categories
    }

    private fun getAnimalsFromJsonArray(animalsArray: JSONArray): List<Animal> {
        val animals = mutableListOf<Animal>()
        for (i in 0 until animalsArray.length()) {
            val animalObject = animalsArray.getJSONObject(i)
            val id = animalObject.getInt("id")
            val name = animalObject.getString("name")
            val briefDescription = animalObject.getString("briefDescription")
            val description = animalObject.getString("description")
            val imageUrl = animalObject.getString("imageUrl")

            val animal = Animal(id, name, briefDescription, description, imageUrl)
            animals.add(animal)
        }
        return animals
    }
}