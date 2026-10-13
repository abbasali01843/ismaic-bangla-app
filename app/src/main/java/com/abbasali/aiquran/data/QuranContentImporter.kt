package com.abbasali.aiquran.data

import android.content.Context
import org.json.JSONObject
import java.io.FileNotFoundException

/**
 * Imports a vetted, versioned JSON content bundle from app/src/main/assets/quran-content.json.
 *
 * The bundle must contain only text the project is permitted to distribute.
 * This importer deliberately does not fetch or invent Quran text.
 */
class QuranContentImporter(private val context: Context) {
    data class Result(
        val versesImported: Int,
        val translationsImported: Int,
        val source: String,
        val version: String
    )

    suspend fun importBundledContent(): Result? {
        val raw = try {
            context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (_: FileNotFoundException) {
            return null
        }

        val root = JSONObject(raw)
        val source = root.optString("source").trim()
        val version = root.optString("contentVersion").trim()
        require(source.isNotEmpty()) { "Content bundle must declare its source." }
        require(version.isNotEmpty()) { "Content bundle must declare contentVersion." }

        val preferenceKey = "$source:$version"
        val preferences = context.getSharedPreferences("quran_content_import", Context.MODE_PRIVATE)
        if (preferences.getString("imported_bundle", null) == preferenceKey) return null

        val versesJson = root.optJSONArray("verses")
            ?: throw IllegalArgumentException("Content bundle is missing the verses array.")
        val translationsJson = root.optJSONArray("translations")
            ?: throw IllegalArgumentException("Content bundle is missing the translations array.")

        val verses = buildList {
            for (i in 0 until versesJson.length()) {
                val item = versesJson.getJSONObject(i)
                val surah = item.getInt("surahNumber")
                val ayah = item.getInt("ayahNumber")
                val key = item.optString("verseKey").ifBlank { "$surah:$ayah" }
                val arabic = item.getString("arabicText").trim()
                require(surah in 1..114) { "Invalid surah number at verse row $i." }
                require(ayah > 0) { "Invalid ayah number at verse row $i." }
                require(arabic.isNotEmpty()) { "Arabic text is empty at verse row $i." }
                add(
                    QuranVerseEntity(
                        verseKey = key,
                        surahNumber = surah,
                        ayahNumber = ayah,
                        arabicText = arabic,
                        juzNumber = item.takeNullableInt("juzNumber"),
                        pageNumber = item.takeNullableInt("pageNumber"),
                        contentSource = source,
                        contentVersion = version
                    )
                )
            }
        }

        val translations = buildList {
            for (i in 0 until translationsJson.length()) {
                val item = translationsJson.getJSONObject(i)
                val key = item.getString("verseKey").trim()
                val language = item.getString("languageCode").trim()
                val editionId = item.getString("editionId").trim()
                val editionName = item.getString("editionName").trim()
                val text = item.getString("translationText").trim()
                val license = item.getString("licenseId").trim()
                val attribution = item.getString("attribution").trim()
                require(key.matches(Regex("\\d{1,3}:\\d{1,3}"))) { "Invalid verseKey at translation row $i." }
                require(language.isNotEmpty() && editionId.isNotEmpty()) { "Translation identity missing at row $i." }
                require(editionName.isNotEmpty() && text.isNotEmpty()) { "Translation text missing at row $i." }
                require(license.isNotEmpty() && attribution.isNotEmpty()) { "Translation license/attribution missing at row $i." }
                add(
                    TranslationEntity(
                        verseKey = key,
                        languageCode = language,
                        editionId = editionId,
                        editionName = editionName,
                        translationText = text,
                        licenseId = license,
                        attribution = attribution
                    )
                )
            }
        }

        require(verses.isNotEmpty() || translations.isNotEmpty()) { "Content bundle is empty." }
        QuranDatabase.getInstance(context).quranDao().importContent(verses, translations)
        preferences.edit().putString("imported_bundle", preferenceKey).apply()
        return Result(verses.size, translations.size, source, version)
    }

    private fun JSONObject.takeNullableInt(name: String): Int? =
        if (isNull(name) || !has(name)) null else optInt(name)

    companion object {
        const val ASSET_NAME = "quran-content.json"
    }
}
