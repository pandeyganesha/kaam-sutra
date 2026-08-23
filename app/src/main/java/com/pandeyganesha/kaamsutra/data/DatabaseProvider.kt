package com.pandeyganesha.kaamsutra.data

import android.content.Context
import androidx.room.Room
import com.pandeyganesha.kaamsutra.data.migrations.MIGRATION_9_10

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .addMigrations(MIGRATION_9_10)
                .build()
                .also { instance = it }
        }
    }
}