package entity

import kotlin.test.*

/**
 * Tests for card suits.
 */
class CardSuitTest {

    /** Checks if we have all 4 card suits. */
    @Test
    fun testEnumValuesExist() {
        assertEquals(4, CardSuit.entries.size)
        assertTrue(CardSuit.entries.contains(CardSuit.CLUBS))
    }

    /** Checks if suits show up as symbols. */
    @Test
    fun testToString() {
        assertEquals("♣", CardSuit.CLUBS.toString())
        assertEquals("♦", CardSuit.DIAMONDS.toString())
    }
}