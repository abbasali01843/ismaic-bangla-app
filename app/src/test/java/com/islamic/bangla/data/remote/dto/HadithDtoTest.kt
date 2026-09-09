package com.islamic.bangla.data.remote.dto

import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The hadith-api curates each book separately, so numeric fields arrive with
 * inconsistent JSON types. Parsing must stay lenient: one odd entry must
 * never fail a whole edition download.
 */
class HadithDtoTest {

    @Test
    fun number_readsIntStringAndGarbage() {
        assertEquals(7, HadithDto(hadithnumber = JsonPrimitive(7)).number())
        assertEquals(12, HadithDto(hadithnumber = JsonPrimitive("12")).number())
        assertEquals(801, HadithDto(hadithnumber = JsonPrimitive("8.01")).number())
        assertEquals(0, HadithDto(hadithnumber = JsonPrimitive("n/a")).number())
        assertEquals(0, HadithDto(hadithnumber = JsonNull.INSTANCE).number())
        assertEquals(0, HadithDto(hadithnumber = null).number())
    }

    @Test
    fun reference_readsIntAndStringBookNumbers() {
        assertEquals(
            3,
            HadithReferenceDto(book = JsonPrimitive(3)).bookNumber()
        )
        assertEquals(
            5,
            HadithReferenceDto(book = JsonPrimitive("Book 5")).bookNumber()
        )
        assertEquals(0, HadithReferenceDto().bookNumber())
        assertEquals(9, HadithReferenceDto(hadith = JsonPrimitive(9)).hadithNumber())
    }

    @Test
    fun allSections_prefersFullEditionMap() {
        val dto = HadithMetadataDto(
            section = mapOf("1" to JsonPrimitive("Single")),
            sections = mapOf("1" to JsonPrimitive("Full"))
        )
        assertEquals("Full", dto.allSections()["1"])
    }

    @Test
    fun allSections_toleratesNonStringValues() {
        val dto = HadithMetadataDto(sections = mapOf("1" to JsonPrimitive(42)))
        assertEquals("42", dto.allSections()["1"])
        assertEquals("", HadithMetadataDto().allSections()["1"].orEmpty())
    }
}
