package entity

import kotlin.test.*

/**
 * Test class for [CardValue].
 */
class CardValueTest {

    /**
     * Tests that all enum values exist.
     */
    @Test
    fun testEnumValuesExist() {
        assertEquals(13, CardValue.entries.size)
        assertTrue(CardValue.entries.contains(CardValue.ACE))
        assertTrue(CardValue.entries.contains(CardValue.TWO))
    }

    /**
     * Tests the toString method.
     */
    @Test
    fun testToString() {
        assertEquals("A", CardValue.ACE.toString())
        assertEquals("10", CardValue.TEN.toString())
        assertEquals("J", CardValue.JACK.toString())
    }

    /**
     * Tests shortDeck function.
     */
    @Test
    fun testShortDeck() {
        val shortDeck = CardValue.shortDeck()
        assertTrue(shortDeck.contains(CardValue.ACE))
        assertFalse(shortDeck.contains(CardValue.TWO))
        assertEquals(8, shortDeck.size)
    }
}
