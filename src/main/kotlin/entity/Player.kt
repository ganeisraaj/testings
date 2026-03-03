package entity

/**
 * Represents a player in the game.
 */
data class Player(
    val name: String,
    var actionsLeft: Int = 2,
    var score: ScoreTable = ScoreTable.NONE,
    val hiddenCards: MutableList<Card> = mutableListOf(),
    val openCards: MutableList<Card> = mutableListOf()
)