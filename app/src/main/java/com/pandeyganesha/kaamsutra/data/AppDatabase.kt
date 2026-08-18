package com.pandeyganesha.kaamsutra.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Task::class, TaskLog::class], version = 8)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskLogDao(): TaskLogDao
    abstract fun taskDao(): TaskDao
}