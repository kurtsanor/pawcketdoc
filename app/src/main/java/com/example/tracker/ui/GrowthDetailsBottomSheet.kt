package com.example.tracker.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.tracker.R
import com.example.tracker.model.Growth
import com.example.tracker.util.DateFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate

class GrowthDetailsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(growth: Growth): GrowthDetailsBottomSheet {
            return GrowthDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong("id", growth.id)
                    putLong("petId", growth.petId)
                    putFloat("weight", growth.weight)
                    putFloat("height", growth.height)
                    putString("notes", growth.notes)
                    putString("dateRecorded", growth.dateRecorded.toString())
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
        return inflater.inflate(R.layout.fragment_growth_details_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        view.findViewById<TextView>(R.id.growthDate).text = DateFormatter.toShortMonthFormat(LocalDate.parse(args.getString("dateRecorded")))
        view.findViewById<TextView>(R.id.growthWeight).text = "${args.getFloat("weight")} kg"
        view.findViewById<TextView>(R.id.growthHeight).text = "${args.getFloat("height")} cm"
        view.findViewById<TextView>(R.id.growthNotes).text = args.getString("notes")
    }

}