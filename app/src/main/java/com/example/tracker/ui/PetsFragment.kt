package com.example.tracker.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracker.R
import com.example.tracker.adapter.PetAdapter
import com.example.tracker.database.AppDatabase
import com.example.tracker.database.DatabaseProvider
import com.example.tracker.model.Pet
import com.example.tracker.service.PetService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.time.LocalDate

class PetsFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var petService: PetService
    private lateinit var recyclerView: RecyclerView
    private lateinit var petList: LiveData<List<Pet>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pets, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "My Pets"
//        loadPets()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.petRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        db = DatabaseProvider.getDatabase(requireContext())
        petService = PetService(db.petDao())

        val fabAddPet = view.findViewById<FloatingActionButton>(R.id.fab_add_pet)
        fabAddPet.setOnClickListener {
            findNavController().navigate(R.id.action_pets_to_petForm)
        }
        loadPets()
        setupSwipeHandler()
    }

    suspend fun deleteById(id: Long) {
            try {
                petService.deleteById(id)
            } catch (e: RuntimeException) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun loadPets() {
        val userId = requireActivity().intent.getLongExtra("USER_ID", -1L)
        petList = petService.findAllByUserId(userId)

        petList.observe(viewLifecycleOwner){ pets ->
            recyclerView.adapter = PetAdapter(pets) { pet ->
                findNavController().navigate(R.id.action_pets_to_petProfile)
                Toast.makeText(requireContext(), "Clicked: ${pet.name}", Toast.LENGTH_SHORT).show()
            }
            setupPlaceholders(pets)
        }
    }

    fun setupPlaceholders (pets: List<Pet>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_pet)
        if (pets.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupSwipeHandler() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            // drag n drop feature
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Called when an item is swiped
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val pet = petList.value?.get(position)


                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Pet")
                    .setMessage("Are you sure you want to delete ${pet?.name}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                deleteById(pet?.id ?: 0)

                                // User confirmed, remove pet from list then update adapter
//                                petList.removeAt(position)
//                                recyclerView.adapter?.notifyItemRemoved(position)
                                Toast.makeText(requireContext(), "${pet?.name} deleted", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } catch (e: RuntimeException) {}
                        }

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        // User cancelled, reset the item so it doesn’t disappear
                        recyclerView.adapter?.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()

                // Show a simple toast on swipe
                val dir = if (direction == ItemTouchHelper.LEFT) "left" else "right"
                Toast.makeText(requireContext(), "Swiped ${pet?.name} to $dir", Toast.LENGTH_SHORT).show()

            }
        }
        // Attach the swipe handler to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


}