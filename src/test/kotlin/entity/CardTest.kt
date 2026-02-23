package entity

import kotlin.test.*

/**
 * Test class for [Card].
 */
class CardTest {

    @Test
    fun testCardCreation() {
        val card = Card(CardSuit.HEARTS, CardValue.ACE)

        assertEquals(CardSuit.HEARTS, card.suit)
        assertEquals(CardValue.ACE, card.value)
    }

    @Test
    fun testCardEquality() {
        val card1 = Card(CardSuit.SPADES, CardValue.KING)
        val card2 = Card(CardSuit.SPADES, CardValue.KING)

        assertEquals(card1, card2)
    }
}
