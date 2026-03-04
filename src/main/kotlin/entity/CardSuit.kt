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
    override fun toString(): String {
        if (this == CLUBS) return "♣"
        if (this == SPADES) return "♠"
        if (this == HEARTS) return "♥"
        if (this == DIAMONDS) return "♦"
        return ""
    }
}