package com.pandeyganesha.kaamsutra.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class, TaskLog::class, Todo::class, Goal::class], version = 11)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskLogDao(): TaskLogDao
    abstract fun taskDao(): TaskDao
    abstract fun todoDao(): TodoDao
    abstract fun goalDao(): GoalDao
}