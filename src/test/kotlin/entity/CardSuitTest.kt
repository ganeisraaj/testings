package entity

import kotlin.test.*

/**
 * Test class for the [CardSuit] enum.
 *
 * This class verifies the correctness of the defined enum values
 * and their string representation.
 */
class CardSuitTest {

    /**
     * Verifies that exactly four suit values are defined
     * and that specific expected values exist.
     *
     * Expected behavior:
     * - The enum contains exactly four entries.
     * - The value CLUBS is part of the enum.
     */
    @Test
    fun testEnumValuesExist() {
        assertEquals(4, CardSuit.entries.size)
        assertTrue(CardSuit.entries.contains(CardSuit.CLUBS))
    }

    /**
     * Verifies that the overridden toString() method
     * returns the correct Unicode symbol for each suit.
     *
     * Expected behavior:
     * - CLUBS is represented by "♣".
     * - DIAMONDS is represented by "♦".
     */
    @Test
    fun testToString() {
        assertEquals("♣", CardSuit.CLUBS.toString())
        assertEquals("♦", CardSuit.DIAMONDS.toString())
    }
}