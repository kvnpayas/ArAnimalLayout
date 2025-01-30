package com.example.aranimallayout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.databinding.CategoryCardItemBinding

class CategoryAdapter(
    private val categories: List<Category>,
    private val listener: OnCategoryClickListener
) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    interface OnCategoryClickListener {
        fun onCategoryClick(category: Category)
    }

    class CategoryViewHolder(private val binding: CategoryCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.categoryName.text = category.name
            val imageResId = binding.root.context.resources.getIdentifier(category.imageUrl, "mipmap", binding.root.context.packageName)
            binding.categoryImage.setImageResource(imageResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
        holder.itemView.setOnClickListener {
            listener.onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}