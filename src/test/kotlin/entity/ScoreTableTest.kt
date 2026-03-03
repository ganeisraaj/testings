package entity

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for the ScoreTable enum.
 */
class ScoreTableTest {

    /** Checks if key poker hands exist. */
    @Test
    fun testEnumValuesExist() {
        assertTrue(ScoreTable.entries.contains(ScoreTable.NONE))
        assertTrue(ScoreTable.entries.contains(ScoreTable.HIGHCARD))
        assertTrue(ScoreTable.entries.contains(ScoreTable.ROYALFLUSH))
    }
}