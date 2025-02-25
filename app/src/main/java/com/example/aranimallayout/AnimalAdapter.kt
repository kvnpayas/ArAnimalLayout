package com.example.aranimallayout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class AnimalAdapter(private val animals: List<Animal>) :
    RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    private var onItemClickListener: ((Animal) -> Unit)? = null

    fun setOnItemClickListener(listener: (Animal) -> Unit) {
        onItemClickListener = listener
    }

    class AnimalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animalImage: ImageView = itemView.findViewById(R.id.animalImage)
        val animalName: TextView = itemView.findViewById(R.id.animalName)
        val animalDescription: TextView = itemView.findViewById(R.id.animalDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.animal_card_item, parent, false)
        return AnimalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val currentAnimal = animals[position]
        val imageResId = holder.itemView.context.resources.getIdentifier(
            currentAnimal.imageUrl,
            "drawable",
            holder.itemView.context.packageName
        )
        holder.animalImage.load(imageResId)
        holder.animalName.text = currentAnimal.name
        holder.animalDescription.text = currentAnimal.briefDescription

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(currentAnimal)
        }
    }

    override fun getItemCount(): Int = animals.size
}