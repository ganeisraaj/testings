package entity

/**
 * Represents a single playing card in the Push Poker game.
 *
 * A card consists of a suit and a value. It is part of the game state and
 * may be located in a player's hand, the draw stack, or the discard stack.
 *
 * Cards are immutable after creation. The suit and value uniquely define
 * the identity of a card.
 *
 * @property suit The suit of the card (e.g., HEARTS, SPADES).
 * @property value The rank/value of the card (e.g., SEVEN, JACK).
 *
 * @constructor Creates a card with the given suit and value.
 */
data class Card(
    val suit: CardSuit,
    val value: CardValue
)