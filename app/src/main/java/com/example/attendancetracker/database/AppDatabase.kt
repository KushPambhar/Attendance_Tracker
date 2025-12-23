package com.example.attendancetracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// This is the database class itself. It needs to be a singleton.
@Database(entities = [Subject::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao

    companion object {
        // The singleton instance prevents multiple database instances
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
