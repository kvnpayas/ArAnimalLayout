package com.example.aranimallayout.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.R
import com.example.aranimallayout.Category
import com.example.aranimallayout.CategoryAdapter

class CategoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        // Get the RecyclerView from the layout
        val recyclerView = view.findViewById<RecyclerView>(R.id.categoryRecyclerView)

        // Set the layout manager to GridLayoutManager with 2 columns
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Create the adapter and set it to the RecyclerView
        val adapter = CategoryAdapter(getCategories())
        recyclerView.adapter = adapter

        return view
    }

    private fun getCategories(): List<Category> {
        // Replace with your actual category data
        return listOf(
            Category("Mammals", R.mipmap.ic_mammals_foreground),
            Category("Amphibians", R.mipmap.ic_amphibian_foreground),
            Category("Reptiles", R.mipmap.ic_reptiles_foreground),
            Category("Birds", R.mipmap.ic_birds_foreground),
            Category("Fish", R.mipmap.ic_fish_foreground),
            Category("Insects", R.mipmap.ic_insects_foreground)
        )
    }
}