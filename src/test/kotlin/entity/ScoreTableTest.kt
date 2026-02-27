package entity

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Test class for the [ScoreTable] enum.
 *
 * This class verifies that the defined score categories exist
 * and are correctly accessible via the enum entries.
 */
class ScoreTableTest {

    /**
     * Verifies that important score categories are part of the enum,
     * including the special value NONE.
     *
     * Expected behavior:
     * - The enum contains NONE.
     * - The enum contains HIGHCARD.
     * - The enum contains ROYALFLUSH.
     */
    @Test
    fun testEnumValuesExist() {
        assertTrue(ScoreTable.entries.contains(ScoreTable.NONE))
        assertTrue(ScoreTable.entries.contains(ScoreTable.HIGHCARD))
        assertTrue(ScoreTable.entries.contains(ScoreTable.ROYALFLUSH))
    }
}