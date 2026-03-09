package com.example.pawcketdoc.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.pawcketdoc.R
import com.example.pawcketdoc.model.Vaccination
import com.example.pawcketdoc.util.DateFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate

class VaccinationDetailsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(vaccination: Vaccination): VaccinationDetailsBottomSheet {
            return VaccinationDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("id", vaccination.id)
                    putString("petId", vaccination.petId)
                    putString("name", vaccination.name)
                    putString("notes", vaccination.notes)
                    putString("administeredDate", vaccination.administeredDate.toString())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vaccination_details_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        val rawDate: LocalDate = LocalDate.parse(args.getString("administeredDate"))
        val formattedDate = DateFormatter.toShortMonthFormat(rawDate)

        view.findViewById<TextView>(R.id.bottomSheetVaccineName).text = args.getString("name")
        view.findViewById<TextView>(R.id.bottomSheetNotes).text = args.getString("notes")
        view.findViewById<TextView>(R.id.bottomSheetDate).text = formattedDate
    }

}