package entity

/**
 * The four suits of a deck of cards.
 */
enum class CardSuit {

    CLUBS,
    SPADES,
    HEARTS,
    DIAMONDS;

    /**
     * Shows the suit as a symbol.
     */
    override fun toString() = when (this) {
        CLUBS -> "♣"
        SPADES -> "♠"
        HEARTS -> "♥"
        DIAMONDS -> "♦"
    }
}