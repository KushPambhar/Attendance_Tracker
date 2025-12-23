package com.example.attendancetracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// This is our data model, representing a table in the database
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val totalClasses: Int,
    val attendedClasses: Int
)