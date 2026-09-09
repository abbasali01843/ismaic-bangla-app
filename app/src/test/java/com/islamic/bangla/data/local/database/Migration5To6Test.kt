package com.islamic.bangla.data.local.database

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.UUID

/**
 * Verifies the v5 -> v6 upgrade on the real production path: a v5 database
 * file is created exactly as older releases left it, then Room opens it with
 * [MIGRATION_5_6]. Room validates the migrated schema on open (any mismatch
 * throws), and the test then asserts user data survived.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration5To6Test {

    @Test
    fun migrate5To6_oldVariant_addsColumnsAndPreservesData() {
        val name = "mig-test-${UUID.randomUUID()}.db"
        createV5Database(name, withNewColumns = false)

        val db = Room.databaseBuilder(
            RuntimeEnvironment.getApplication(),
            IslamicDatabase::class.java,
            name
        ).addMigrations(MIGRATION_5_6).build()

        runBlocking {
            val duas = db.duaDao().getAllDuas().first()
            assertEquals(1, duas.size)
            assertEquals("ঘুমের দোয়া", duas[0].duaNameBangla)
            assertEquals("", duas[0].category)

            val prayer = db.prayerTimeDao().getTodayPrayerTime("Dhaka", "2026-09-09").first()
                ?: throw AssertionError("prayer row lost")
            assertEquals("05:00", prayer.fajr)
            assertEquals("", prayer.hijriDate)
            assertEquals("", prayer.imsak)

            val ayahs = db.ayahDao().getAyahsBySurah(1).first()
            assertEquals(1, ayahs.size)
            assertEquals(0, ayahs[0].globalNumber)

            val bookmarks = db.bookmarkDao().getAll().first()
            assertEquals(1, bookmarks.size)
            assertEquals("ayah", bookmarks[0].type)

            val note = db.noteDao().getNote("ayah", 1001).first()
                ?: throw AssertionError("note lost")
            assertEquals("my note", note.noteText)
        }
        db.close()
    }

    @Test
    fun migrate5To6_latestVariant_isNoopAndPreservesData() {
        // The released v5 APK already contains all new columns; the migration
        // must detect that and leave data untouched.
        val name = "mig-test-${UUID.randomUUID()}.db"
        createV5Database(name, withNewColumns = true)

        val db = Room.databaseBuilder(
            RuntimeEnvironment.getApplication(),
            IslamicDatabase::class.java,
            name
        ).addMigrations(MIGRATION_5_6).build()

        runBlocking {
            val duas = db.duaDao().getAllDuas().first()
            assertEquals(1, duas.size)
            assertEquals("sleep", duas[0].category)
        }
        db.close()
    }

    private fun createV5Database(name: String, withNewColumns: Boolean) {
        val context = RuntimeEnvironment.getApplication()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    V5_TABLES.forEach { db.execSQL(it) }
                    if (withNewColumns) {
                        db.execSQL("ALTER TABLE dua ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                        db.execSQL("ALTER TABLE prayer_times ADD COLUMN hijriDate TEXT NOT NULL DEFAULT ''")
                        db.execSQL("ALTER TABLE prayer_times ADD COLUMN imsak TEXT NOT NULL DEFAULT ''")
                        db.execSQL("ALTER TABLE ayah ADD COLUMN globalNumber INTEGER NOT NULL DEFAULT 0")
                    }
                    V5_SEED.forEach { db.execSQL(it) }
                    if (withNewColumns) {
                        db.execSQL("UPDATE dua SET category = 'sleep' WHERE duaId = 1")
                    }
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) = Unit
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(config).use { it.writableDatabase }
    }

    companion object {
        // v5 schema exactly as older releases created it (new columns absent).
        private val V5_TABLES = listOf(
            """CREATE TABLE quran (surahNumber INTEGER NOT NULL, surahName TEXT NOT NULL,
               surahNameBangla TEXT NOT NULL, meaning TEXT NOT NULL, meaningBangla TEXT NOT NULL,
               totalAyahs INTEGER NOT NULL, revelationType TEXT NOT NULL, PRIMARY KEY(surahNumber))""",
            """CREATE TABLE ayah (id INTEGER NOT NULL, surahNumber INTEGER NOT NULL,
               ayahNumber INTEGER NOT NULL, arabicText TEXT NOT NULL,
               banglaTranslation TEXT NOT NULL, englishTranslation TEXT NOT NULL, PRIMARY KEY(id))""",
            """CREATE TABLE hadith (hadithId INTEGER NOT NULL, collection TEXT NOT NULL,
               bookName TEXT NOT NULL, hadithNumber TEXT NOT NULL, arabicText TEXT NOT NULL,
               banglaTranslation TEXT NOT NULL, englishTranslation TEXT NOT NULL,
               gradeOfAuthenticity TEXT NOT NULL, PRIMARY KEY(hadithId))""",
            """CREATE TABLE dua (duaId INTEGER NOT NULL, duaName TEXT NOT NULL,
               duaNameBangla TEXT NOT NULL, arabicText TEXT NOT NULL,
               banglaTranslation TEXT NOT NULL, englishTranslation TEXT NOT NULL,
               benefit TEXT NOT NULL, benefitBangla TEXT NOT NULL, PRIMARY KEY(duaId))""",
            """CREATE TABLE prayer_times (id INTEGER NOT NULL, date TEXT NOT NULL,
               fajr TEXT NOT NULL, dhuhr TEXT NOT NULL, asr TEXT NOT NULL,
               maghrib TEXT NOT NULL, isha TEXT NOT NULL, city TEXT NOT NULL,
               country TEXT NOT NULL, PRIMARY KEY(id))""",
            """CREATE TABLE prayer_log (date TEXT NOT NULL, fajr INTEGER NOT NULL,
               dhuhr INTEGER NOT NULL, asr INTEGER NOT NULL, maghrib INTEGER NOT NULL,
               isha INTEGER NOT NULL, PRIMARY KEY(date))""",
            """CREATE TABLE bookmarks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
               type TEXT NOT NULL, refId INTEGER NOT NULL, title TEXT NOT NULL,
               subtitle TEXT NOT NULL, timestamp INTEGER NOT NULL)""",
            "CREATE UNIQUE INDEX index_bookmarks_type_refId ON bookmarks (type, refId)",
            """CREATE TABLE last_read (id INTEGER NOT NULL, type TEXT NOT NULL,
               refId INTEGER NOT NULL, title TEXT NOT NULL, subtitle TEXT NOT NULL,
               timestamp INTEGER NOT NULL, PRIMARY KEY(id))""",
            """CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
               type TEXT NOT NULL, refId INTEGER NOT NULL, title TEXT NOT NULL,
               noteText TEXT NOT NULL, updatedAt INTEGER NOT NULL)""",
            "CREATE UNIQUE INDEX index_notes_type_refId ON notes (type, refId)",
            """CREATE TABLE tafsir_cache (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
               slug TEXT NOT NULL, surahNumber INTEGER NOT NULL, ayahNumber INTEGER NOT NULL,
               text TEXT NOT NULL)""",
            """CREATE UNIQUE INDEX index_tafsir_cache_slug_surahNumber_ayahNumber
               ON tafsir_cache (slug, surahNumber, ayahNumber)"""
        )

        private val V5_SEED = listOf(
            """INSERT INTO dua (duaId, duaName, duaNameBangla, arabicText, banglaTranslation,
               englishTranslation, benefit, benefitBangla)
               VALUES (1, 'sleep dua', 'ঘুমের দোয়া', 'a', 'b', 'c', 'd', 'e')""",
            """INSERT INTO prayer_times (id, date, fajr, dhuhr, asr, maghrib, isha, city, country)
               VALUES (1, '2026-09-09', '05:00', '12:00', '15:30', '18:00', '19:30',
               'Dhaka', 'Bangladesh')""",
            """INSERT INTO ayah (id, surahNumber, ayahNumber, arabicText, banglaTranslation,
               englishTranslation) VALUES (1001, 1, 1, 'a', 'b', 'c')""",
            """INSERT INTO bookmarks (type, refId, title, subtitle, timestamp)
               VALUES ('ayah', 1001, 't', 's', 1)""",
            """INSERT INTO notes (type, refId, title, noteText, updatedAt)
               VALUES ('ayah', 1001, 't', 'my note', 1)"""
        )
    }
}
