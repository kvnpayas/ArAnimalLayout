package com.example.aranimallayout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import coil.request.CachePolicy
import de.hdodenhof.circleimageview.CircleImageView

sealed class AnimalItem {
    data class CategoryItem(val category: Category) : AnimalItem()
    data class AnimalData(val animal: Animal) : AnimalItem()
    object BackItem : AnimalItem()
}

class AnimalFunc(
    private val context: Context,
    private var animalItems: List<AnimalItem>
) : RecyclerView.Adapter<AnimalFunc.AnimalViewHolder>() {

    private var onItemClickListener: ((String) -> Unit)? = null
    private var onBackClickListener: (() -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }

    fun updateData(newAnimalItems: List<AnimalItem>) {
        animalItems = newAnimalItems
        notifyDataSetChanged()
    }

    class AnimalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animalName: TextView = itemView.findViewById(R.id.animalNameCamera)
        val animalImage: CircleImageView = itemView.findViewById(R.id.animalImageCamera)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal, parent, false)
        return AnimalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val currentItem = animalItems[position]

        holder.animalImage.dispose()
        holder.animalImage.setImageResource(0)

        when (currentItem) {
            is AnimalItem.CategoryItem -> {
                holder.animalName.text = currentItem.category.name
                val imageResId = context.resources.getIdentifier(
                    currentItem.category.imageUrl,
                    "mipmap",
                    context.packageName
                )
                holder.animalImage.load(imageResId) {
                    memoryCachePolicy(CachePolicy.ENABLED)
                }
                holder.itemView.setOnClickListener {
                    // Handle category click
                }
            }

            is AnimalItem.AnimalData -> {
                holder.animalName.text = currentItem.animal.name
                val imageResId = context.resources.getIdentifier(
                    currentItem.animal.imageUrl,
                    "drawable",
                    context.packageName
                )
                holder.animalImage.load(imageResId) {
                    memoryCachePolicy(CachePolicy.ENABLED)
                }
                holder.itemView.setOnClickListener {
                    onItemClickListener?.invoke(currentItem.animal.name)
                }
            }
            is AnimalItem.BackItem -> {
                holder.animalName.text = "Back"
                holder.animalImage.setImageResource(R.drawable.baseline_arrow_back_24)
                holder.itemView.setOnClickListener {
                    onBackClickListener?.invoke()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return animalItems.size
    }
}