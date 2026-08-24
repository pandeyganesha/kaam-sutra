package com.pandeyganesha.kaamsutra.data

import android.content.Context
import androidx.room.Room
import com.pandeyganesha.kaamsutra.data.migrations.MIGRATION_9_10
import com.pandeyganesha.kaamsutra.data.migrations.MIGRATION_10_11
import com.pandeyganesha.kaamsutra.data.migrations.MIGRATION_11_12
import com.pandeyganesha.kaamsutra.data.migrations.MIGRATION_12_13

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
                .addMigrations(
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13
                )
                .build()
                .also { instance = it }
        }
    }
}