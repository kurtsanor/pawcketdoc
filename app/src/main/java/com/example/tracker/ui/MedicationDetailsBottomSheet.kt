package com.example.tracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.tracker.R
import com.example.tracker.model.Medication
import com.example.tracker.util.DateFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MedicationDetailsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(medication: Medication): MedicationDetailsBottomSheet {
            return MedicationDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong("id", medication.id)
                    putLong("petId", medication.petId)
                    putString("name", medication.name)
                    putString("dosage", medication.dosage)
                    putString("frequency", medication.frequency)
                    putString("startDate", DateFormatter.toShortMonthFormat(medication.startDate))
                    putString("endDate", DateFormatter.toShortMonthFormat(medication.endDate))
                    putString("reason", medication.reason)
                    putString("notes", medication.notes)
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
        return inflater.inflate(R.layout.fragment_medication_details_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        view.findViewById<TextView>(R.id.bsMedName).text = args.getString("name")
        view.findViewById<TextView>(R.id.bsDateRange).text = "${args.getString("startDate")} – ${args.getString("endDate")}"
        view.findViewById<TextView>(R.id.bsReason).text = args.getString("reason")
        view.findViewById<TextView>(R.id.bsDosage).text = args.getString("dosage")
        view.findViewById<TextView>(R.id.bsFrequency).text = args.getString("frequency")
        view.findViewById<TextView>(R.id.bsNotes).text = args.getString("notes")
    }

}