package entity

/**
 * Represents a playing card with a suit and a value.
 */
data class Card(
    val suit: CardSuit,
    val value: CardValue
)