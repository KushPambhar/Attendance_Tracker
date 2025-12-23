package com.example.attendancetracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.attendancetracker.R
import com.example.attendancetracker.database.Subject
import com.example.attendancetracker.databinding.ItemSubjectBinding
import com.example.attendancetracker.viewmodel.SubjectViewModel

// Adapter for the RecyclerView to display subjects
class SubjectAdapter(
    private val onPresentClick: (Subject) -> Unit,
    private val onAbsentClick: (Subject) -> Unit,
    private val onLongClick: (Subject) -> Unit,
    private val viewModel: SubjectViewModel // We need the ViewModel to get attendance data
) : ListAdapter<Subject, SubjectAdapter.SubjectViewHolder>(DiffCallback) {

    // ViewHolder to hold and bind the views for each subject item
    class SubjectViewHolder(
        private val binding: ItemSubjectBinding,
        private val viewModel: SubjectViewModel,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            // Bind the data to the views
            binding.tvSubjectName.text = subject.name
            binding.tvClassesCount.text = "Attended: ${subject.attendedClasses} / Total: ${subject.totalClasses}"

            // Calculate and display the attendance percentage
            val percentage = viewModel.getAttendancePercentage(subject)
            binding.tvAttendancePercentage.text = "%.2f%%".format(percentage)

            // Warning indicator: change text color if below 75%
            if (percentage < 75.0) {
                binding.tvAttendancePercentage.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.red)
                )
            } else {
                binding.tvAttendancePercentage.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.green)
                )
            }

            // Display the forecast message
            binding.tvForecast.text = viewModel.getAttendanceForecast(subject)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val currentSubject = getItem(position)
        holder.bind(currentSubject)

        // Set up click listeners for the buttons and long press
        holder.itemView.setOnClickListener { /* No-op, long click is for options */ }
        holder.itemView.setOnLongClickListener {
            onLongClick(currentSubject)
            true // Return true to consume the long-click event
        }
        holder.itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_present).setOnClickListener { onPresentClick(currentSubject) }
        holder.itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_absent).setOnClickListener { onAbsentClick(currentSubject) }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Subject>() {
            override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
                return oldItem == newItem
            }
        }
    }
}
// Kush Pambhar
