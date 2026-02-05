package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.PetAdapter
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.database.PetEntity
import com.example.tracker.database.toModel
import com.example.tracker.model.Pet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.Executors

class PetsFragment : Fragment() {

    private val dbExecutor = Executors.newSingleThreadExecutor()
    private lateinit var adapter: PetAdapter

    // Later, you can replace this with the logged-in user's ID.
    private val userId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pets, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "My Pets"

        val recyclerView = view.findViewById<RecyclerView>(R.id.petRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        //  Updated adapter (mutable list + click + long click)
        adapter = PetAdapter(
            mutableListOf(),
            onClick = { pet ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView2, PetProfileFragment())
                    .setTransition(TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()

                Toast.makeText(requireContext(), "Clicked: ${pet.name}", Toast.LENGTH_SHORT).show()
            },
            onLongClick = { pet ->
                deletePet(pet)
            }
        )

        recyclerView.adapter = adapter

        // Load pets from Room
        loadPets()

        // Navigate to Add Pet Form
        val fabAddPet = view.findViewById<FloatingActionButton>(R.id.fab_add_pet)
        fabAddPet.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, PetFormActivityFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadPets() {
        val db = DatabaseProvider.getDatabase(requireContext())
        dbExecutor.execute {
            val petsFromDb = db.petDao().getPetsByUserId(userId).map { it.toModel() }
            requireActivity().runOnUiThread {
                adapter.updatePets(petsFromDb)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deletePet(pet: Pet) {
        val id = pet.id ?: run {
            Toast.makeText(requireContext(), "Unable to delete pet (missing ID).", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(requireContext())

        dbExecutor.execute {
            // Convert Pet model to PetEntity for deletion
            val entity = PetEntity(
                id = id,
                userId = pet.userId,
                name = pet.name,
                type = pet.type,
                breed = pet.breed,
                gender = pet.gender,
                birthDate = pet.birthDate
            )

            db.petDao().deletePet(entity)

            val updatedPets = db.petDao().getPetsByUserId(userId).map { it.toModel() }
            requireActivity().runOnUiThread {
                adapter.updatePets(updatedPets)
                Toast.makeText(requireContext(), "Deleted: ${pet.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // When you return from PetFormActivityFragment, refresh list automatically
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        loadPets()
    }
}
