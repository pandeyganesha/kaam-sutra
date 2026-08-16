package com.pandeyganesha.kaamsutra.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Habit::class, HabitLog::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitDao(): HabitDao
}