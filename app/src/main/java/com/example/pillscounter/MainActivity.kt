package com.example.pillscounter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.pillscounter.data.Pill
import com.example.pillscounter.PillsAdapter

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var pillsAdapter: PillsAdapter
    private var selectedImageUri: Uri? = null
    private var dialogView: View? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                dialogView?.findViewById<ImageView>(R.id.pillImagePreview)?.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        observeData()
    }

    private fun setupViews() {
        findViewById<FloatingActionButton>(R.id.addPillFab).setOnClickListener {
            showAddPillDialog()
        }

        pillsAdapter = PillsAdapter(
            onItemClick = { pill ->
                startActivity(PillDetailActivity.createIntent(this@MainActivity, pill.id))
            },
            onItemLongClick = { pill ->
                showDeleteConfirmationDialog(pill)
            }
        )

        findViewById<RecyclerView>(R.id.pillsList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pillsAdapter
        }
    }

    private fun observeData() {
        viewModel.pills.observe(this) { pills ->
            pillsAdapter.submitList(pills)
        }
    }

    private fun showAddPillDialog() {
        selectedImageUri = null
        dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pill, null)
        
        val nameInput = dialogView?.findViewById<EditText>(R.id.pillNameInput)
        val dosageInput = dialogView?.findViewById<EditText>(R.id.dosageInput)
        val countInput = dialogView?.findViewById<EditText>(R.id.totalCountInput)
        
        dialogView?.findViewById<MaterialButton>(R.id.selectImageButton)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_pill))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val name = nameInput?.text.toString()
                val dosage = dosageInput?.text.toString()
                val count = countInput?.text.toString().toIntOrNull() ?: 0

                if (name.isNotBlank() && dosage.isNotBlank() && count > 0) {
                    viewModel.addPill(name, dosage, count, selectedImageUri?.toString())
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDeleteConfirmationDialog(pill: Pill) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_pill))
            .setMessage(getString(R.string.delete_confirmation, pill.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deletePill(pill)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}