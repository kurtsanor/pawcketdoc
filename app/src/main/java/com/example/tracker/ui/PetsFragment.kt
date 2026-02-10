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
import androidx.lifecycle.lifecycleScope
import com.example.tracker.R
import com.example.tracker.adapter.PetAdapter
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.database.PetEntity
import com.example.tracker.database.toModel
import com.example.tracker.model.Pet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PetsFragment : Fragment() {

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
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = PetAdapter(
            mutableListOf(),

            //  UPDATED: pass petId to profile screen
            onClick = { pet ->
                val petId = pet.id ?: run {
                    Toast.makeText(requireContext(), "Missing pet ID.", Toast.LENGTH_SHORT).show()
                    return@PetAdapter
                }

                val fragment = PetProfileFragment().apply {
                    arguments = Bundle().apply {
                        putLong("petId", petId)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView2, fragment)
                    .setTransition(TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()
            },

            onLongClick = { pet ->
                deletePet(pet)
            }
        )

        recyclerView.adapter = adapter

        //  Observe LiveData from Room (auto-refresh)
        observePets()

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
    private fun observePets() {
        val db = DatabaseProvider.getDatabase(requireContext())

        db.petDao().getPetsByUserId(userId).observe(viewLifecycleOwner) { entities ->
            val pets = entities.map { it.toModel() }
            adapter.updatePets(pets)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deletePet(pet: Pet) {
        val id = pet.id ?: run {
            Toast.makeText(requireContext(), "Unable to delete pet (missing ID).", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(requireContext())

        // Suspend delete must be in coroutine
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
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

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Deleted: ${pet.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
