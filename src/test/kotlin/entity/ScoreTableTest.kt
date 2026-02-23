package entity

import kotlin.test.*

/**
 * Test class for [ScoreTable].
 */
class ScoreTableTest {

    @Test
    fun testEnumValuesExist() {
        assertTrue(ScoreTable.entries.contains(ScoreTable.HIGHCARD))
        assertTrue(ScoreTable.entries.contains(ScoreTable.ROYALFLUSH))
    }
}
