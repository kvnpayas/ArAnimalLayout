package com.example.aranimallayout

import android.annotation.SuppressLint
import android.content.ClipData
import android.view.MotionEvent
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnimalFunc(
    private val context: Context,
    private val animalList: ArrayList<AnimalModel>
) : RecyclerView.Adapter<AnimalFunc.AnimalViewHolder>() {

    private var onItemClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_animal, parent, false)
        return AnimalViewHolder(view)
    }

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animal = animalList[position]
        holder.animalName.text = animal.name
        val imageResId = context.resources.getIdentifier(animal.imageName, "drawable", context.packageName)
        holder.animalImage.setImageResource(imageResId)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(animal.name)
        }
    }

    override fun getItemCount(): Int {
        return animalList.size
    }

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    inner class AnimalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animalName: TextView = itemView.findViewById(R.id.animalName)
        val animalImage: de.hdodenhof.circleimageview.CircleImageView = itemView.findViewById(R.id.animalImage)
    }
}