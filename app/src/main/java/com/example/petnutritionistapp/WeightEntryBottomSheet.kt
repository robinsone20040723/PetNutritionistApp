package com.example.petnutritionistapp.ui

import android.os.Bundle
import android.view.View
import com.example.petnutritionistapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText

class WeightEntryBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_weight_entry) {

    fun interface OnConfirmListener {
        fun onConfirm(date: String, weight: Double, bcs: Int)
    }
    var onConfirm: OnConfirmListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etDate = view.findViewById<TextInputEditText>(R.id.etDate)
        val etWeight = view.findViewById<TextInputEditText>(R.id.etWeight)
        val chipBcs = view.findViewById<ChipGroup>(R.id.chipBcs)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)

        // 預設勾 5
        view.findViewById<Chip>(R.id.chip5)?.isChecked = true

        // 日期挑選
        etDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.label_date))
                .build()
            picker.addOnPositiveButtonClickListener { utcMillis ->
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                fmt.timeZone = java.util.TimeZone.getTimeZone("UTC")
                etDate.setText(fmt.format(java.util.Date(utcMillis)))
            }
            picker.show(parentFragmentManager, "datePicker")
        }

        btnCancel.setOnClickListener { dismiss() }

        btnConfirm.setOnClickListener {
            val date = etDate.text?.toString()?.trim().orEmpty()
            val weight = etWeight.text?.toString()?.trim()?.toDoubleOrNull()
            val checkedId = chipBcs.checkedChipId
            val bcs = chipBcs.findViewById<Chip?>(checkedId)?.text?.toString()?.toIntOrNull()

            if (date.isEmpty()) {
                etDate.error = getString(R.string.required); return@setOnClickListener
            }
            if (weight == null || weight <= 0) {
                etWeight.error = getString(R.string.weight_helper); return@setOnClickListener
            }
            if (bcs == null) {
                // 理論上不會發生，因為 selectionRequired=true
                return@setOnClickListener
            }

            onConfirm?.onConfirm(date, weight, bcs)
            dismiss()
        }
    }

    companion object { fun newInstance() = WeightEntryBottomSheet() }
}
