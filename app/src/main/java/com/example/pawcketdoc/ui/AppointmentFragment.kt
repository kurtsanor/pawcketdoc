package com.example.pawcketdoc.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawcketdoc.R
import com.example.pawcketdoc.adapter.AppointmentAdapter
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.service.AppointmentService
import com.example.pawcketdoc.util.SnackbarUtil
import com.example.pawcketdoc.util.SwipeDeleteHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class AppointmentFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var appointmentService: AppointmentService
    private lateinit var recyclerView: RecyclerView
    private lateinit var appointments: LiveData<List<Appointment>>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_appointment, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<TextView>(R.id.txtHeaderTitle).text = "Appointments"
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialCalendarView>(R.id.calendarAppointments)
            .setDateSelected(CalendarDay.today(), true)

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        appointmentService = AppointmentService(db.appointmentDao(), firebaseFirestore, firebaseAuth)

        val petId = arguments?.getString("pet_id")!!

        recyclerView = view.findViewById(R.id.recyclerViewAppointments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val bundle = Bundle().apply { putString("pet_id", petId) }
        view.findViewById<Button>(R.id.buttonAddAppointment).setOnClickListener {
            findNavController().navigate(R.id.action_appointment_to_appointmentForm, bundle)
        }

        loadAppointments(petId)
    }

    private fun setupPlaceholders(items: List<Appointment>) {
        val placeholder: LinearLayout? = view?.findViewById(R.id.placeholder_empty_appointment)
        if (items.isEmpty()) {
            placeholder?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeholder?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun loadAppointments(petId: String) {
        appointments = appointmentService.findAllByPetId(petId)
        appointments.observe(viewLifecycleOwner) { items ->
            recyclerView.adapter = AppointmentAdapter(
                appointments = items,
                onClick = { appointment ->
                    AppointmentDetailsBottomSheet.newInstance(appointment)
                        .show(parentFragmentManager, "AppointmentDetailsBottomSheet")
                },
                onDeleteClick = { appointment ->
                    SwipeDeleteHelper.confirmDelete(
                        fragment = this,
                        message = "Are you sure you want to delete ${appointment.title}?"
                    ) {
                        appointmentService.deleteById(appointment.id)
                        SnackbarUtil.showSuccess(
                            view = requireView(),
                            title = "Success",
                            message = "Appointment has been deleted"
                        )
                    }
                }
            )
            setupPlaceholders(items)
        }
    }
}
