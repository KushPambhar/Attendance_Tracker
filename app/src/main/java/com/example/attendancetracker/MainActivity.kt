package com.example.attendancetracker

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendancetracker.adapter.SubjectAdapter
import com.example.attendancetracker.database.AppDatabase
import com.example.attendancetracker.database.Subject
import com.example.attendancetracker.databinding.ActivityMainBinding
import com.example.attendancetracker.databinding.DialogAddEditSubjectBinding
import com.example.attendancetracker.viewmodel.SubjectViewModel
import com.example.attendancetracker.viewmodel.SubjectViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SubjectViewModel
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database and ViewModel
        val database = AppDatabase.getDatabase(this)
        val factory = SubjectViewModelFactory(database.subjectDao())
        viewModel = ViewModelProvider(this, factory)[SubjectViewModel::class.java]

        // Setup RecyclerView
        subjectAdapter = SubjectAdapter(
            onPresentClick = { subject ->
                // Mark present: increment both total and attended classes
                val updatedSubject = subject.copy(
                    totalClasses = subject.totalClasses + 1,
                    attendedClasses = subject.attendedClasses + 1
                )
                viewModel.updateSubject(updatedSubject)
                checkAttendanceStatus(updatedSubject)
            },
            onAbsentClick = { subject ->
                // Mark absent: increment only total classes
                val updatedSubject = subject.copy(
                    totalClasses = subject.totalClasses + 1
                )
                viewModel.updateSubject(updatedSubject)
                checkAttendanceStatus(updatedSubject)
            },
            onLongClick = { subject ->
                // Show a dialog to edit or delete the subject on a long press
                showEditDeleteDialog(subject)
            },
            viewModel = viewModel // Pass the ViewModel to the adapter for percentage/forecast calculation
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = subjectAdapter
        }

        // Observe the list of subjects from the ViewModel and update the adapter
        viewModel.allSubjects.observe(this) { subjects ->
            subjectAdapter.submitList(subjects)
        }

        // Set up the Floating Action Button to add a new subject
        binding.fabAddSubject.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    // Checks if attendance is below 75% and shows a toast
    private fun checkAttendanceStatus(subject: Subject) {
        val percentage = viewModel.getAttendancePercentage(subject)
        if (percentage < 75.0 && percentage > 0) {
            val message = "${subject.name} attendance is below 75%! Current: ${"%.2f".format(percentage)}%"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Function to show the Add/Edit subject dialog
    private fun showAddEditDialog(subject: Subject?) {
        val dialogBinding = DialogAddEditSubjectBinding.inflate(layoutInflater)
        val isEditMode = subject != null

        if (isEditMode) {
            dialogBinding.etSubjectName.setText(subject?.name)
            dialogBinding.etTotalClasses.setText(subject?.totalClasses.toString())
            dialogBinding.etAttendedClasses.setText(subject?.attendedClasses.toString())
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEditMode) "Edit Subject" else "Add Subject")
            .setView(dialogBinding.root)
            .setPositiveButton(if (isEditMode) "Update" else "Add") { dialog, _ ->
                val name = dialogBinding.etSubjectName.text.toString()
                val totalClasses = dialogBinding.etTotalClasses.text.toString().toIntOrNull() ?: 0
                val attendedClasses = dialogBinding.etAttendedClasses.text.toString().toIntOrNull() ?: 0

                if (name.isNotEmpty()) {
                    val newSubject = if (isEditMode) {
                        subject!!.copy(name = name, totalClasses = totalClasses, attendedClasses = attendedClasses)
                    } else {
                        Subject(name = name, totalClasses = totalClasses, attendedClasses = attendedClasses)
                    }
                    if (isEditMode) viewModel.updateSubject(newSubject) else viewModel.addSubject(newSubject)
                } else {
                    Toast.makeText(this, "Subject name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Function to show a confirmation dialog for editing or deleting
    private fun showEditDeleteDialog(subject: Subject) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Options for ${subject.name}")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showAddEditDialog(subject) // Edit
                    1 -> showDeleteConfirmationDialog(subject) // Delete
                }
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Function to show a confirmation dialog before deleting
    private fun showDeleteConfirmationDialog(subject: Subject) {
        AlertDialog.Builder(this)
            .setTitle("Delete Subject")
            .setMessage("Are you sure you want to delete ${subject.name}?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteSubject(subject)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
