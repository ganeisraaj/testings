package entity

/**
 * Represents the four possible suits of a French-suited playing card deck.
 *
 * The suit defines the category of a card and is used in game logic to
 * determine whether a card can legally be played. Each suit can be
 * represented by a corresponding Unicode symbol.
 *
 * The available suits are:
 * - CLUBS
 * - SPADES
 * - HEARTS
 * - DIAMONDS
 */
enum class CardSuit {

    CLUBS,
    SPADES,
    HEARTS,
    DIAMONDS;

    /**
     * Returns the Unicode symbol representing this suit.
     *
     * The returned symbol is used for visual representation in the GUI
     * or console output.
     *
     * @return A single-character string representing the suit
     * (♣ for CLUBS, ♠ for SPADES, ♥ for HEARTS, ♦ for DIAMONDS).
     */
    override fun toString() = when (this) {
        CLUBS -> "♣"
        SPADES -> "♠"
        HEARTS -> "♥"
        DIAMONDS -> "♦"
    }
}