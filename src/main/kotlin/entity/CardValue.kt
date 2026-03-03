package entity

import java.util.EnumSet

/**
 * The 13 values of a deck, from 2 to Ace.
 */
enum class CardValue {

    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE;

    /**
     * Shows the card value as a string.
     */
    override fun toString() =
        when (this) {
            TWO -> "2"
            THREE -> "3"
            FOUR -> "4"
            FIVE -> "5"
            SIX -> "6"
            SEVEN -> "7"
            EIGHT -> "8"
            NINE -> "9"
            TEN -> "10"
            JACK -> "J"
            QUEEN -> "Q"
            KING -> "K"
            ACE -> "A"
        }

    /**
     * Utility for card values.
     */
    companion object {

        /**
         * The standard 32-card deck starting from 7.
         */
        fun shortDeck(): Set<CardValue> {
            return EnumSet.of(ACE, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING)
        }
    }
}