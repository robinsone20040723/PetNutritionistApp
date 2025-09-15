package com.example.petnutritionistapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

/** Firestore 紀錄模型（含 id，供更新/刪除用） */
data class WeightRecord(
    val id: String,     // Firestore document id
    val date: String,   // yyyy-MM-dd（顯示用）
    val weightKg: Double,
    val bcs: Int,
    val note: String? = null
)

/** 列表 Adapter（使用 item_record.xml，含右上角 ︙ 選單） */
private class RecordAdapter(
    private val onEdit: (WeightRecord) -> Unit,
    private val onDelete: (WeightRecord) -> Unit
) : ListAdapter<WeightRecord, RecordAdapter.VH>(object : DiffUtil.ItemCallback<WeightRecord>() {
    override fun areItemsTheSame(oldItem: WeightRecord, newItem: WeightRecord) =
        oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: WeightRecord, newItem: WeightRecord) = oldItem == newItem
}) {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvWeight: TextView = view.findViewById(R.id.tvWeight)
        val tvBcs: TextView = view.findViewById(R.id.tvBcs)
        val btnMore: ImageButton = view.findViewById(R.id.btnMore)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvDate.text = item.date
        holder.tvWeight.text = String.format(Locale.getDefault(), "%.1f kg", item.weightKg)
        holder.tvBcs.text = "BCS ${item.bcs}"
        holder.tvNote.isVisible = !item.note.isNullOrBlank()
        holder.tvNote.text = item.note ?: ""

        // 右上角 ︙ 選單
        holder.btnMore.setOnClickListener { v ->
            val popup = PopupMenu(v.context, v)
            popup.menuInflater.inflate(R.menu.menu_record_item, popup.menu)
            popup.setOnMenuItemClickListener { mi ->
                when (mi.itemId) {
                    R.id.action_edit -> { onEdit(item); true }
                    R.id.action_delete -> { onDelete(item); true }
                    else -> false
                }
            }
            popup.show()
        }
    }
}

class WeightLogFragment : Fragment() {

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etDate: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var etBcs: MaterialAutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmpty: TextView

    // Adapter（把編輯/刪除 callback 傳進去）
    private val adapter by lazy {
        RecordAdapter(
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
    }

    // Firebase
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var logsRegistration: ListenerRegistration? = null

    private val dayFormatter by lazy {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_weight_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 連結 View
        toolbar = view.findViewById(R.id.toolbar)
        etDate = view.findViewById(R.id.etDate)
        etWeight = view.findViewById(R.id.etWeight)
        etBcs = view.findViewById(R.id.etBcs)
        btnSave = view.findViewById(R.id.btnSave)
        rvHistory = view.findViewById(R.id.rvHistory)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_record -> { view.post { view.scrollTo(0, 0); etWeight.requestFocus() }; true }
                R.id.action_export -> { exportCsvAndShare(); true }
                else -> false
            }
        }

        // 日期
        etDate.setOnClickListener { showDatePicker() }
        etDate.setText(dayFormatter.format(Date())) // 預設今天（UTC）

        // BCS 下拉（1..9）+ 預設 5
        val bcsItems = (1..9).map { it.toString() }
        etBcs.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, bcsItems))
        etBcs.setText("5", false)
        etBcs.setOnClickListener { etBcs.showDropDown() }

        // 列表
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        // 監聽雲端資料
        listenLogs()

        // 儲存
        btnSave.setOnClickListener { onSave() }
        etBcs.setOnEditorActionListener { _, actionId, _ -> if (actionId == EditorInfo.IME_ACTION_DONE) { onSave(); true } else false }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnSave.isEnabled = canSave()
            }
        }
        etDate.addTextChangedListener(watcher)
        etWeight.addTextChangedListener(watcher)
        etBcs.addTextChangedListener(watcher)
        btnSave.isEnabled = canSave()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logsRegistration?.remove()
        logsRegistration = null
    }

    private fun canSave(): Boolean {
        val dateOk = !etDate.text.isNullOrBlank()
        val weightOk = etWeight.text?.toString()?.replace(',', '.')?.toDoubleOrNull()?.let { it > 0 } == true
        val bcsOk = etBcs.text?.toString()?.toIntOrNull()?.let { it in 1..9 } == true
        return dateOk && weightOk && bcsOk
    }

    /** 新增 */
    private fun onSave() {
        if (!canSave()) {
            Toast.makeText(requireContext(), getString(R.string.fill_all_required), Toast.LENGTH_SHORT).show()
            return
        }
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(requireContext(), getString(R.string.not_signed_in), Toast.LENGTH_SHORT).show()
            return
        }

        val dateStr   = etDate.text?.toString()?.trim().orEmpty()
        val weightStr = etWeight.text?.toString()?.trim()?.replace(',', '.') ?: ""
        val bcsStr    = etBcs.text?.toString()?.trim() ?: ""

        val weight = weightStr.toDoubleOrNull()
        val bcs    = bcsStr.toIntOrNull()
        if (weight == null || weight <= 0) { etWeight.error = getString(R.string.invalid_weight); return }
        if (bcs == null || bcs !in 1..9)  { etBcs.error = getString(R.string.invalid_bcs); return }

        val dateMillis = parseDateUtc(dateStr)
        val col = db.collection("users").document(uid).collection("weightLogs")
        val doc = col.document()
        val data = hashMapOf(
            "id"         to doc.id,
            "date"       to dateStr,
            "dateMillis" to dateMillis,
            "weightKg"   to weight,
            "bcs"        to bcs,
            "createdAt"  to System.currentTimeMillis()
        )
        doc.set(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
                etWeight.setText("")
                etBcs.setText("5", false)
                btnSave.isEnabled = canSave()
            }
            .addOnFailureListener { e ->
                val msg = e.message ?: "-"
                Toast.makeText(requireContext(), getString(R.string.save_failed_with_message, msg), Toast.LENGTH_LONG).show()
            }
    }

    /** 監聽（把 id 帶出來） */
    private fun listenLogs() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        logsRegistration?.remove()
        logsRegistration = db.collection("users")
            .document(uid)
            .collection("weightLogs")
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("WeightLog", "Listen failed", error)
                    return@addSnapshotListener
                }
                if (!isAdded || view == null) return@addSnapshotListener

                val list = snapshot?.documents?.map { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val dateMillis = doc.getLong("dateMillis") ?: 0L
                    val dateStr = doc.getString("date") ?: formatDate(dateMillis)
                    val weight = doc.getDouble("weightKg") ?: (doc.getDouble("weight") ?: 0.0)
                    val bcs = (doc.getLong("bcs") ?: 0L).toInt()
                    val note = doc.getString("note")
                    WeightRecord(id = id, date = dateStr, weightKg = weight, bcs = bcs, note = note)
                } ?: emptyList()

                adapter.submitList(list)
                tvEmpty.isVisible = list.isEmpty()
            }
    }

    /** 刪除流程：確認 → 刪除 */
    private fun confirmDelete(record: WeightRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("刪除紀錄")
            .setMessage("確定要刪除 ${record.date}（${String.format(Locale.getDefault(), "%.1f kg", record.weightKg)}, BCS ${record.bcs}）嗎？")
            .setPositiveButton("刪除") { _, _ -> deleteRecord(record) }
            .setNegativeButton("取消", null)
            .show()
    }
    private fun deleteRecord(record: WeightRecord) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("weightLogs")
            .document(record.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "已刪除", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "刪除失敗：${e.message ?: "-"}", Toast.LENGTH_LONG).show()
            }
    }

    /** 編輯對話框（可改體重與 BCS） */
    private fun showEditDialog(record: WeightRecord) {
        val ctx = requireContext()
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 8)
        }
        val etWeightEdit = EditText(ctx).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "體重（kg）"
            setText(String.format(Locale.getDefault(), "%.1f", record.weightKg))
        }
        val etBcsEdit = EditText(ctx).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "BCS（1~9）"
            setText(record.bcs.toString())
        }
        container.addView(etWeightEdit)
        container.addView(etBcsEdit)

        AlertDialog.Builder(ctx)
            .setTitle("編輯紀錄 - ${record.date}")
            .setView(container)
            .setNegativeButton("取消", null)
            .setPositiveButton("儲存") { _, _ ->
                val w = etWeightEdit.text.toString().replace(',', '.').toDoubleOrNull()
                val b = etBcsEdit.text.toString().toIntOrNull()
                if (w == null || w <= 0) {
                    Toast.makeText(ctx, "請輸入正確體重", Toast.LENGTH_SHORT).show(); return@setPositiveButton
                }
                if (b == null || b !in 1..9) {
                    Toast.makeText(ctx, "BCS 必須介於 1~9", Toast.LENGTH_SHORT).show(); return@setPositiveButton
                }
                updateRecord(record.id, w, b)
            }
            .show()
    }
    private fun updateRecord(id: String, weight: Double, bcs: Int) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("weightLogs")
            .document(id)
            .update(mapOf("weightKg" to weight, "bcs" to bcs))
            .addOnSuccessListener { Toast.makeText(requireContext(), "已更新", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "更新失敗：${e.message ?: "-"}", Toast.LENGTH_LONG).show()
            }
    }

    /** 匯出 CSV */
    private fun exportCsvAndShare() {
        val list = adapter.currentList
        if (list.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_data_to_export), Toast.LENGTH_SHORT).show()
            return
        }
        val csv = buildString {
            appendLine("date,weight_kg,bcs")
            list.forEach { appendLine("${it.date},${it.weightKg},${it.bcs}") }
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Weight_BCS_Export.csv")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        startActivity(Intent.createChooser(send, getString(R.string.action_export)))
    }

    private fun showDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.label_date))
            .setSelection(today)
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis -> etDate.setText(formatDate(utcMillis)) }
        picker.show(childFragmentManager, "date_picker")
    }

    // 日期工具
    private fun parseDateUtc(dateStr: String): Long = try {
        dayFormatter.parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
    private fun formatDate(millis: Long): String = dayFormatter.format(Date(millis))
}
