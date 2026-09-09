package com.islamic.bangla.data.local.seed

import org.junit.Assert.assertEquals
import org.junit.Test

class DuaCategoriesTest {

    @Test
    fun ordered_putsKnownCategoriesFirstAndUnknownLast() {
        val result = DuaCategories.ordered(listOf("zzz-unknown", "sleep", "prayer"))
        assertEquals(listOf("sleep", "prayer", "zzz-unknown"), result)
    }

    @Test
    fun banglaName_returnsLabelOrFallsBackToKey() {
        assertEquals("ঘুম", DuaCategories.banglaName("sleep"))
        assertEquals("nope", DuaCategories.banglaName("nope"))
    }
}
