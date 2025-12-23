package com.example.attendancetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.attendancetracker.database.Subject
import com.example.attendancetracker.database.SubjectDao
import kotlinx.coroutines.launch

// This ViewModel manages the data and business logic for the UI
class SubjectViewModel(private val subjectDao: SubjectDao) : ViewModel() {

    // A LiveData of the list of all subjects, automatically updated by Room
    val allSubjects = subjectDao.getAllSubjects().asLiveData()

    // Function to calculate attendance percentage
    fun getAttendancePercentage(subject: Subject): Double {
        return if (subject.totalClasses == 0) 0.0 else (subject.attendedClasses.toDouble() / subject.totalClasses.toDouble()) * 100
    }

    // Function to calculate attendance forecast
    fun getAttendanceForecast(subject: Subject): String {
        val requiredPercentage = 75.0
        val currentPercentage = getAttendancePercentage(subject)

        return if (currentPercentage >= requiredPercentage) {
            // Calculate how many classes can be skipped
            var classesToSkip = 0
            while (
                (subject.attendedClasses.toDouble() / (subject.totalClasses + classesToSkip).toDouble()) * 100 >= requiredPercentage
            ) {
                classesToSkip++
            }
            "Can skip ${classesToSkip - 1} more class(es)."
        } else {
            // Calculate how many classes need to be attended
            var classesToAttend = 0
            while (
                ( (subject.attendedClasses + classesToAttend).toDouble() / (subject.totalClasses + classesToAttend).toDouble() ) * 100 < requiredPercentage
            ) {
                classesToAttend++
            }
            "Need to attend ${classesToAttend} more class(es)."
        }
    }

    // Coroutine functions for database operations
    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            subjectDao.insert(subject)
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            subjectDao.update(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            subjectDao.delete(subject)
        }
    }
}

// ViewModelFactory is needed to pass the DAO into the ViewModel
class SubjectViewModelFactory(private val subjectDao: SubjectDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectViewModel(subjectDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
