package entity

import kotlin.test.*

/**
 * Test class for the [Card] data class.
 *
 * This class verifies the correct initialization of card properties
 * and the equality behavior of the data class implementation.
 */
class CardTest {

    /**
     * Verifies that a Card instance is correctly created
     * with the specified suit and value.
     *
     * Expected behavior:
     * - The suit property matches the constructor argument.
     * - The value property matches the constructor argument.
     */
    @Test
    fun testCardCreation() {
        val card = Card(CardSuit.HEARTS, CardValue.ACE)

        assertEquals(CardSuit.HEARTS, card.suit)
        assertEquals(CardValue.ACE, card.value)
    }

    /**
     * Verifies that two Card instances with identical suit and value
     * are considered equal.
     *
     * Expected behavior:
     * - Two cards with the same suit and value are equal,
     *   as defined by the automatically generated equals() method
     *   of the data class.
     */
    @Test
    fun testCardEquality() {
        val card1 = Card(CardSuit.SPADES, CardValue.KING)
        val card2 = Card(CardSuit.SPADES, CardValue.KING)

        assertEquals(card1, card2)
    }
}