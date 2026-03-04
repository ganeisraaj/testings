package entity

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
    override fun toString(): String {
        if (this == TWO) return "2"
        if (this == THREE) return "3"
        if (this == FOUR) return "4"
        if (this == FIVE) return "5"
        if (this == SIX) return "6"
        if (this == SEVEN) return "7"
        if (this == EIGHT) return "8"
        if (this == NINE) return "9"
        if (this == TEN) return "10"
        if (this == JACK) return "J"
        if (this == QUEEN) return "Q"
        if (this == KING) return "K"
        if (this == ACE) return "A"
        return ""
    }
}