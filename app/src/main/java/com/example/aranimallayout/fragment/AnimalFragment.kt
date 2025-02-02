package com.example.aranimallayout.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.AnimalAdapter
import com.example.aranimallayout.R
import com.example.aranimallayout.util.JsonUtil
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController

class AnimalFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_animal, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.animalRecyclerView)
        val categoryNameTextView = view.findViewById<TextView>(R.id.categoryNameTextView)
        val backButton = view.findViewById<AppCompatButton>(R.id.backButton)
        recyclerView.layoutManager = LinearLayoutManager(context)


        val categoryId = arguments?.getInt("categoryId") ?: -1
        val categoryName = arguments?.getString("categoryName") ?: ""
        if (categoryId != -1) {
            val categories = JsonUtil.getCategoriesFromAssets(requireContext())
            val category = categories.find { it.id == categoryId }
            val animals = category?.animals ?: emptyList()

            val adapter = AnimalAdapter(animals)
            recyclerView.adapter = adapter
        }

        categoryNameTextView.text = categoryName

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }
}