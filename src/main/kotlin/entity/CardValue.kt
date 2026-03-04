package entity

import java.util.EnumSet

/**
 * Represents the thirteen possible card values in a French-suited deck.
 *
 * The values range from TWO to ACE and are ordered according to their
 * natural ascending rank:
 *
 * TWO < THREE < ... < TEN < JACK < QUEEN < KING < ACE
 *
 * The ordering may be relevant for comparisons depending on the
 * implemented game logic.
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
     * Returns the textual representation of this card value.
     *
     * The returned string is typically used for display purposes in
     * the GUI or console output.
     *
     * @return A string representing the card value
     * (e.g., "2", "10", "J", "Q", "K", "A").
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
     * Provides utility functions for working with [CardValue].
     */
    companion object {

        /**
         * Returns the reduced 32-card deck values (short deck variant).
         *
         * The short deck starts at SEVEN and includes:
         * SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE.
         *
         * @return A set containing the card values used in a 32-card deck.
         */
        fun shortDeck(): Set<CardValue> {
            return EnumSet.of(ACE, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING)
        }
    }
}