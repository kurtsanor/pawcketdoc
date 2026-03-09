package com.example.pawcketdoc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.model.Pet
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide

class PetAdapter(
    private val pets: List<Pet>,
    private val onClick: (Pet) -> Unit
): RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

     class PetViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.petName)
        val breed: TextView = view.findViewById(R.id.petBreed)
        val genderImage: ImageView = view.findViewById(R.id.genderImage)
        val gender: TextView = view.findViewById(R.id.gender)
        val type: TextView = view.findViewById(R.id.petType)
        val avatar: ImageView = view.findViewById(R.id.profilePic)
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
        holder.genderImage.setImageResource(if (pet.gender == "Male") R.drawable.male else R.drawable.female)
        if (pet.gender == "Male") holder.genderImage.setColorFilter("#2196F3".toColorInt()) else holder.genderImage.setColorFilter(
            "#E91E63".toColorInt())
        holder.type.text = pet.type
        Glide.with(holder.avatar.context)
            .load(pet.avatarUrl)
            .placeholder(R.drawable.pet_placeholder)
            .into(holder.avatar)

        holder.itemView.setOnClickListener {
            onClick(pet)
        }
    }

    override fun getItemCount(): Int {
        return pets.size
    }

}