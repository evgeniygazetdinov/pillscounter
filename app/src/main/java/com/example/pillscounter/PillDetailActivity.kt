package com.example.pillscounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pillscounter.data.PillTaking
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class PillDetailActivity : AppCompatActivity() {
    private val viewModel: PillDetailViewModel by viewModels {
        PillDetailViewModel.Factory(intent.getLongExtra(EXTRA_PILL_ID, -1))
    }

    private lateinit var takingsAdapter: TakingsAdapter
    private lateinit var nameText: TextView
    private lateinit var dosageText: TextView
    private lateinit var remainingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pill_detail)

        setupViews()
        observeData()
    }

    private fun setupViews() {
        nameText = findViewById(R.id.pillNameText)
        dosageText = findViewById(R.id.dosageText)
        remainingText = findViewById(R.id.remainingCountText)

        takingsAdapter = TakingsAdapter(
            onEditClick = { taking -> showEditTakingDialog(taking) },
            onDeleteClick = { taking -> showDeleteTakingDialog(taking) }
        )

        findViewById<RecyclerView>(R.id.takingsList).apply {
            layoutManager = LinearLayoutManager(this@PillDetailActivity)
            adapter = takingsAdapter
        }

        findViewById<FloatingActionButton>(R.id.addTakingFab).setOnClickListener {
            showAddTakingDialog()
        }
    }

    private fun observeData() {
        viewModel.pill.observe(this) { pill ->
            title = pill.name
            nameText.text = pill.name
            dosageText.text = getString(R.string.dosage_format, pill.dosage)
            remainingText.text = getString(R.string.remaining, pill.totalCount)
        }

        viewModel.takings.observe(this) { takings ->
            takingsAdapter.submitList(takings)
        }
    }

    private fun showAddTakingDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_taking, null)
        val countInput = dialogView.findViewById<EditText>(R.id.countInput)
        val timeButton = dialogView.findViewById<MaterialButton>(R.id.timeButton)
        var selectedTime = Calendar.getInstance()

        timeButton.setOnClickListener {
            showTimePicker { calendar ->
                selectedTime = calendar
                timeButton.text = formatTime(calendar)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_taking)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val count = countInput.text.toString().toIntOrNull() ?: 1
                viewModel.addTaking(count, selectedTime.time)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditTakingDialog(taking: PillTaking) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_taking, null)
        val countInput = dialogView.findViewById<EditText>(R.id.countInput)
        val timeButton = dialogView.findViewById<MaterialButton>(R.id.timeButton)
        val calendar = Calendar.getInstance().apply { time = taking.timestamp }

        countInput.setText(taking.count.toString())
        timeButton.text = formatTime(calendar)

        timeButton.setOnClickListener {
            showTimePicker { newCalendar ->
                calendar.set(Calendar.HOUR_OF_DAY, newCalendar.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, newCalendar.get(Calendar.MINUTE))
                timeButton.text = formatTime(calendar)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit_taking)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val newCount = countInput.text.toString().toIntOrNull() ?: taking.count
                viewModel.updateTaking(taking.copy(timestamp = calendar.time), newCount)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteTakingDialog(taking: PillTaking) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_taking)
            .setMessage(R.string.delete_taking_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTaking(taking)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showTimePicker(onTimeSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()

        picker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, picker.hour)
            calendar.set(Calendar.MINUTE, picker.minute)
            onTimeSelected(calendar)
        }

        picker.show(supportFragmentManager, "time_picker")
    }

    private fun formatTime(calendar: Calendar): String {
        return String.format("%02d:%02d", 
            calendar.get(Calendar.HOUR_OF_DAY), 
            calendar.get(Calendar.MINUTE)
        )
    }

    companion object {
        private const val EXTRA_PILL_ID = "pill_id"

        fun createIntent(context: Context, pillId: Long): Intent {
            return Intent(context, PillDetailActivity::class.java).apply {
                putExtra(EXTRA_PILL_ID, pillId)
            }
        }
    }
}

class TakingsAdapter(
    private val onEditClick: (PillTaking) -> Unit,
    private val onDeleteClick: (PillTaking) -> Unit
) : ListAdapter<PillTaking, TakingsAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<PillTaking>() {
        override fun areItemsTheSame(oldItem: PillTaking, newItem: PillTaking) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PillTaking, newItem: PillTaking) = oldItem == newItem
    }
) {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_taking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taking = getItem(position)
        holder.countText.text = holder.itemView.context.getString(R.string.taking_count, taking.count)
        holder.timestampText.text = dateFormat.format(taking.timestamp)
        holder.editButton.setOnClickListener { onEditClick(taking) }
        holder.deleteButton.setOnClickListener { onDeleteClick(taking) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val countText: TextView = view.findViewById(R.id.countText)
        val timestampText: TextView = view.findViewById(R.id.timestampText)
        val editButton: MaterialButton = view.findViewById(R.id.editButton)
        val deleteButton: MaterialButton = view.findViewById(R.id.deleteButton)
    }
}
