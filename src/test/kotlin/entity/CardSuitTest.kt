package entity

import kotlin.test.*

/**
 * Test class for [CardSuit].
 */
class CardSuitTest {

    @Test
    fun testEnumValuesExist() {
        assertEquals(4, CardSuit.entries.size)
        assertTrue(CardSuit.entries.contains(CardSuit.CLUBS))
    }

    @Test
    fun testToString() {
        assertEquals("♣", CardSuit.CLUBS.toString())
        assertEquals("♦", CardSuit.DIAMONDS.toString())
    }
}
