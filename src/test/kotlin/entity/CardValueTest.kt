package entity

import kotlin.test.*

/**
 * Tests for card values.
 */
class CardValueTest {

    /** Checks if we have all 13 card values. */
    @Test
    fun testEnumValuesExist() {
        assertEquals(13, CardValue.entries.size)
        assertTrue(CardValue.entries.contains(CardValue.ACE))
        assertTrue(CardValue.entries.contains(CardValue.TWO))
    }

    /** Checks if card values show up as the right strings. */
    @Test
    fun testToString() {
        assertEquals("A", CardValue.ACE.toString())
        assertEquals("10", CardValue.TEN.toString())
        assertEquals("J", CardValue.JACK.toString())
    }

    /** Checks if the 32-card deck set is correct. */
    @Test
    fun testShortDeck() {
        val shortDeck = CardValue.shortDeck()

        assertTrue(shortDeck.contains(CardValue.ACE))
        assertFalse(shortDeck.contains(CardValue.TWO))
        assertEquals(8, shortDeck.size)
    }
}