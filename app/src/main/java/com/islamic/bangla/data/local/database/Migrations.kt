package com.islamic.bangla.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database v5 -> v6.
 *
 * Past releases evolved the schema without bumping the version, so different
 * v5 databases may or may not have these columns. Each statement runs only
 * when its column is actually missing, which keeps every upgrade path safe:
 * - users on the latest v5 (all columns present): no-op, data untouched
 * - users on older v5 variants: missing columns are added, data preserved
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        addColumnIfMissing(db, "dua", "category", "TEXT NOT NULL DEFAULT ''")
        addColumnIfMissing(db, "prayer_times", "hijriDate", "TEXT NOT NULL DEFAULT ''")
        addColumnIfMissing(db, "prayer_times", "imsak", "TEXT NOT NULL DEFAULT ''")
        addColumnIfMissing(db, "ayah", "globalNumber", "INTEGER NOT NULL DEFAULT 0")
    }
}

private fun addColumnIfMissing(
    db: SupportSQLiteDatabase,
    table: String,
    column: String,
    spec: String
) {
    val exists = db.query("PRAGMA table_info($table)").use { cursor ->
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        generateSequence {
            if (cursor.moveToNext()) cursor.getString(nameIndex) else null
        }.any { it == column }
    }
    if (!exists) {
        db.execSQL("ALTER TABLE $table ADD COLUMN $column $spec")
    }
}
