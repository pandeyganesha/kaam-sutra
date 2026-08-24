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

val MIGRATION_10_11 = object : Migration(10, 11) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE goals (
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
            INSERT INTO goals (
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
            WHERE t.taskType = 'GOALS'
        """.trimIndent())
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // 1. Create the new habits table
        db.execSQL("""
            CREATE TABLE habits (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                sortOrder INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                status TEXT NOT NULL
            )
        """.trimIndent())

        // 2. Copy habit tasks into habits
        db.execSQL("""
            INSERT INTO habits (
                id,
                name,
                sortOrder,
                createdAt,
                updatedAt,
                status
            )
            SELECT
                t.id,
                t.name,
                ROW_NUMBER() OVER (
                    ORDER BY t.createdAt, t.id
                ) - 1,
                t.createdAt,
                t.createdAt,
                CASE
                    WHEN t.isActive = 1 THEN 'ACTIVE'
                    ELSE 'DELETED'
                END
            FROM tasks t
            WHERE t.taskType = 'HABITS'
        """.trimIndent())

        // 3. Create the new habit_log table
        db.execSQL("""
            CREATE TABLE habit_log (
                id TEXT NOT NULL PRIMARY KEY,
                habitId TEXT NOT NULL,
                habitDate TEXT NOT NULL,
                completed INTEGER NOT NULL,
                FOREIGN KEY(habitId)
                    REFERENCES habits(id)
                    ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE INDEX index_habit_log_habitId
            ON habit_log(habitId)
            """.trimIndent())

        // 4. Copy the corresponding task logs into habit logs
        db.execSQL("""
            INSERT INTO habit_log (
                id,
                habitId,
                habitDate,
                completed
            )
            SELECT
                tl.id,
                tl.taskId,
                tl.date,
                tl.completed
            FROM task_log tl
            INNER JOIN habits h
                ON h.id = tl.taskId
        """.trimIndent())

        // Old tables are no longer needed
        db.execSQL("DROP TABLE task_log")
        db.execSQL("DROP TABLE tasks")
    }
}