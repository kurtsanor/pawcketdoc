package com.example.tracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.model.Pet

class PetAdapter(
    private val pets: MutableList<Pet>,
    private val onClick: (Pet) -> Unit,
    private val onLongClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.petName)
        val breed: TextView = view.findViewById(R.id.petBreed)
        val genderImage: ImageView = view.findViewById(R.id.genderImage)
        val gender: TextView = view.findViewById(R.id.gender)
        val type: TextView = view.findViewById(R.id.petType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]

        holder.name.text = pet.name
        holder.breed.text = pet.breed
        holder.gender.text = pet.gender
        holder.type.text = pet.type

        // Set gender icon and color
        holder.genderImage.setImageResource(
            if (pet.gender == "Male") R.drawable.male else R.drawable.female
        )

        if (pet.gender == "Male") {
            holder.genderImage.setColorFilter("#2196F3".toColorInt())
        } else {
            holder.genderImage.setColorFilter("#E91E63".toColorInt())
        }

        // Normal click
        holder.itemView.setOnClickListener {
            onClick(pet)
        }

        // Long press (for delete)
        holder.itemView.setOnLongClickListener {
            onLongClick(pet)
            true
        }
    }

    override fun getItemCount(): Int {
        return pets.size
    }

    // ✅ NEW: Method to refresh list after Room DB changes
    fun updatePets(newPets: List<Pet>) {
        pets.clear()
        pets.addAll(newPets)
        notifyDataSetChanged()
    }
}
