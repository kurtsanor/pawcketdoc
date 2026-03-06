package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.tracker.R
import com.example.tracker.model.MedicalRecord
import com.example.tracker.util.DateFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate

class MedicalRecordBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(medicalRecord: MedicalRecord): MedicalRecordBottomSheet {
            return MedicalRecordBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("id", medicalRecord.id)
                    putString("petId", medicalRecord.petId)
                    putString("title", medicalRecord.title)
                    putString("date", medicalRecord.date.toString())
                    putString("diagnosis", medicalRecord.diagnosis)
                    putString("treatment", medicalRecord.treatment)
                    putString("notes", medicalRecord.notes)
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
        return inflater.inflate(R.layout.fragment_medical_record_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()


        view.findViewById<TextView>(R.id.bsTitle).text = args.getString("title")
        view.findViewById<TextView>(R.id.bsDate).text = DateFormatter.toShortMonthFormat(LocalDate.parse(args.getString("date")))
        view.findViewById<TextView>(R.id.bsDiagnosis).text = args.getString("diagnosis")
        view.findViewById<TextView>(R.id.bsTreatment).text = args.getString("treatment")
        view.findViewById<TextView>(R.id.bsNotes).text = args.getString("notes")
    }

}