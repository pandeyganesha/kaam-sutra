package com.pandeyganesha.kaamsutra.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE todos (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                sortOrder INTEGER NOT NULL,
                completed INTEGER NOT NULL,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO todos (
                id,
                name,
                sortOrder,
                completed,
                status,
                createdAt,
                updatedAt
            )
            SELECT
                t.id,
                t.name,

                ROW_NUMBER() OVER (
                    ORDER BY t.createdAt
                ) - 1,

                CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM task_log tl
                        WHERE tl.taskId = t.id
                          AND tl.completed = 1
                    )
                    THEN 1
                    ELSE 0
                END,

                CASE
                    WHEN t.isActive = 1 THEN 'ACTIVE'
                    ELSE 'DELETED'
                END,

                t.createdAt,
                t.createdAt

            FROM tasks t
            WHERE t.taskType = 'TODO'
        """.trimIndent())
    }
}