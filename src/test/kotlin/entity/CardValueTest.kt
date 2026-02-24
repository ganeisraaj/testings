package entity

import kotlin.test.*

/**
 * Test class for the [CardValue] enum.
 *
 * This class verifies the completeness of the defined enum values,
 * the correctness of the string representation, and the behavior
 * of the utility function shortDeck().
 */
class CardValueTest {

    /**
     * Verifies that exactly thirteen card values are defined
     * and that specific expected values exist.
     *
     * Expected behavior:
     * - The enum contains exactly 13 entries.
     * - ACE and TWO are part of the enum values.
     */
    @Test
    fun testEnumValuesExist() {
        assertEquals(13, CardValue.entries.size)
        assertTrue(CardValue.entries.contains(CardValue.ACE))
        assertTrue(CardValue.entries.contains(CardValue.TWO))
    }

    /**
     * Verifies that the overridden toString() method returns
     * the correct textual representation for selected values.
     *
     * Expected behavior:
     * - ACE is represented as "A".
     * - TEN is represented as "10".
     * - JACK is represented as "J".
     */
    @Test
    fun testToString() {
        assertEquals("A", CardValue.ACE.toString())
        assertEquals("10", CardValue.TEN.toString())
        assertEquals("J", CardValue.JACK.toString())
    }

    /**
     * Verifies that the shortDeck() function returns the correct
     * reduced set of card values for a 32-card deck variant.
     *
     * Expected behavior:
     * - The returned set contains ACE.
     * - The returned set does not contain TWO.
     * - The set contains exactly 8 values.
     */
    @Test
    fun testShortDeck() {
        val shortDeck = CardValue.shortDeck()

        assertTrue(shortDeck.contains(CardValue.ACE))
        assertFalse(shortDeck.contains(CardValue.TWO))
        assertEquals(8, shortDeck.size)
    }
}