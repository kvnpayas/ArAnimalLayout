package com.example.aranimallayout.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.R
import com.example.aranimallayout.Category
import com.example.aranimallayout.CategoryAdapter
import com.example.aranimallayout.util.JsonUtil
import androidx.navigation.fragment.findNavController


class CategoryFragment : Fragment(), CategoryAdapter.OnCategoryClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.categoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        val categories = JsonUtil.getCategoriesFromAssets(requireContext())
        val adapter = CategoryAdapter(categories, this)
        recyclerView.adapter = adapter

        return view
    }

    override fun onCategoryClick(category: Category) {
        val bundle = Bundle()
        bundle.putInt("categoryId", category.id)
        bundle.putString("categoryName", category.name)
        findNavController().navigate(R.id.action_categoryFragment_to_animalFragment, bundle)
    }

}