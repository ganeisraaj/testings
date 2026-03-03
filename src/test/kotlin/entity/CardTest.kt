package entity

import kotlin.test.*

/**
 * Tests for the Card entity.
 */
class CardTest {

    /** Checks if a card is created with the right info. */
    @Test
    fun testCardCreation() {
        val card = Card(CardSuit.HEARTS, CardValue.ACE)

        assertEquals(CardSuit.HEARTS, card.suit)
        assertEquals(CardValue.ACE, card.value)
    }

    /** Checks if cards with same suit and value are equal. */
    @Test
    fun testCardEquality() {
        val card1 = Card(CardSuit.SPADES, CardValue.KING)
        val card2 = Card(CardSuit.SPADES, CardValue.KING)

        assertEquals(card1, card2)
    }
}