package entity

/**
 * Represents a player participating in a Push Poker game.
 *
 * A Player stores all relevant player-specific data, including the
 * player name, remaining actions in the current turn, the current
 * score category, and the cards assigned to the player.
 *
 * This class is part of the entity layer and therefore only stores
 * data. All game logic modifying a player's state must be handled
 * through the service layer.
 *
 * @property name The unique name identifying the player.
 * @property actionsLeft The number of actions the player may still
 * perform during the current turn.
 * @property score The current score classification of the player.
 * @property hiddenCards The list of cards currently hidden from other players.
 * @property openCards The list of cards currently visible to other players.
 *
 * @constructor Creates a new player with the specified name and optional
 * initial values for actions, score, and card collections.
 */
data class Player(
    val name: String,
    var actionsLeft: Int = 2,
    var score: ScoreTable = ScoreTable.HIGHCARD,
    val hiddenCards: MutableList<Card> = mutableListOf(),
    val openCards: MutableList<Card> = mutableListOf()
)