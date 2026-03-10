package com.example.pawcketdoc.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.DocumentAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.service.DocumentService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class DocumentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var documentService: DocumentService
    private lateinit var db: AppDatabase
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "Documents"
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewDocuments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseFirestore = Firebase.firestore
        firebaseAuth = Firebase.auth
        documentService = DocumentService(db.documentDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!
        val emptyState = view.findViewById<View>(R.id.emptyState)

        val bundle = Bundle().apply {
            putString("pet_id", petId)
        }

        documentService.findAllByPetId(petId).observe(viewLifecycleOwner) { documents ->
            recyclerView.adapter = DocumentAdapter(documents) { document ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(document.fileUrl))
                startActivity(intent)
            }
            if (documents.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fabAddDocument).setOnClickListener {
            findNavController().navigate(R.id.action_documents_to_documentForm, bundle)
        }
    }
}