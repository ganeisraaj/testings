package gui

import entity.CardSuit
import entity.CardValue
import tools.aqua.bgw.visual.ImageVisual

/**
 * Loads card images from the card_deck.png sheet.
 * This class handles finding the correct portion of the image for each card.
 */
class CardImageLoader {

    private val cardsFile: String = "card_deck.png"
    private val imgHeight: Int = 200
    private val imgWidth: Int = 130

    /** Visual representaton of an empty card slot. */
    val blankImage: ImageVisual = getImageByCoordinates(0, 4)

    /** Visual representation of the back of a card. */
    val backImage: ImageVisual = getImageByCoordinates(2, 4)

    /**
     * Gets the front side image for a specific card.
     */
    fun frontImageFor(suit: CardSuit, value: CardValue): ImageVisual {
        val col: Int = getColumn(value)
        val row: Int = getRow(suit)
        val result: ImageVisual = getImageByCoordinates(col, row)
        return result
    }

    /**
     * Extracts a single card image from the master sheet.
     */
    private fun getImageByCoordinates(x: Int, y: Int): ImageVisual {
        val posX: Int = x * imgWidth
        val posY: Int = y * imgHeight
        val visual: ImageVisual = ImageVisual(
            cardsFile,
            imgWidth,
            imgHeight,
            posX,
            posY
        )
        return visual
    }

    /**
     * Finds the vertical row in the image sheet based on the suit.
     */
    private fun getRow(suit: CardSuit): Int {
        if (suit == CardSuit.CLUBS) return 0
        if (suit == CardSuit.DIAMONDS) return 1
        if (suit == CardSuit.HEARTS) return 2
        if (suit == CardSuit.SPADES) return 3
        return 0
    }

    /**
     * Finds the horizontal column in the image sheet based on the value.
     */
    private fun getColumn(value: CardValue): Int {
        if (value == CardValue.ACE) return 0
        if (value == CardValue.TWO) return 1
        if (value == CardValue.THREE) return 2
        if (value == CardValue.FOUR) return 3
        if (value == CardValue.FIVE) return 4
        if (value == CardValue.SIX) return 5
        if (value == CardValue.SEVEN) return 6
        if (value == CardValue.EIGHT) return 7
        if (value == CardValue.NINE) return 8
        if (value == CardValue.TEN) return 9
        if (value == CardValue.JACK) return 10
        if (value == CardValue.QUEEN) return 11
        if (value == CardValue.KING) return 12
        return 0
    }
}
