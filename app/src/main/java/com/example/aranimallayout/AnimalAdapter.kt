package com.example.aranimallayout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aranimallayout.databinding.AnimalCardItemBinding

class AnimalAdapter(private val animals: List<Animal>) :
    RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    class AnimalViewHolder(private val binding: AnimalCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(animal: Animal) {
            binding.animalName.text = animal.name
            val imageResId = binding.root.context.resources.getIdentifier(animal.imageUrl, "mipmap", binding.root.context.packageName)
            binding.animalImage.setImageResource(imageResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val binding = AnimalCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animal = animals[position]
        holder.bind(animal)
    }

    override fun getItemCount(): Int = animals.size
}