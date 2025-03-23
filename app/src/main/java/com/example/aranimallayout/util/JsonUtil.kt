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
            val tagalogName = animalObject.getString("tagalogName")
            val scientificName = animalObject.getString("scientificName")
            val lifeSpan = animalObject.getString("lifeSpan")
            val funFact = animalObject.getString("funFact")
            val description = animalObject.getString("description")
            val imageUrl = animalObject.getString("imageUrl")
            val sound = animalObject.getString("sound")
            val soundDesc = animalObject.getString("soundDesc")
            val model = animalObject.getString("model")

            val animal = Animal(id, name, tagalogName, scientificName, lifeSpan, funFact, description, imageUrl, sound, soundDesc, model)
            animals.add(animal)
        }
        return animals
    }
}