package entity

import java.util.Stack

/**
 * Represents the complete state of an ongoing Push Poker game.
 *
 * The [Game] class serves as the root element of the entity layer and
 * stores all relevant game data, including players, card stacks,
 * the current player index, round tracking, and the game log.
 *
 * It does not contain complex game logic. All state modifications
 * beyond simple data updates must be performed via the service layer.
 *
 * @property totalRounds The total number of rounds selected for this game.
 * @property roundsLeft The number of rounds remaining until the game ends.
 * @property currentRound The current round number (starting at 1).
 * @property currentPlayerIndex The index of the player whose turn it currently is.
 * @property log A chronological list of textual log messages describing game events.
 * @property players The list of participating players (2..4).
 * @property drawStack The stack of cards from which new center cards are drawn.
 * @property discardStack The stack of cards that have been discarded/played.
 * @property centerCards The three cards currently displayed in the center.
 *
 * @constructor Creates a new game instance with optional initial values.
 */
data class Game(
    var totalRounds: Int = 0,
    var currentRound: Int = 1,
    var currentPlayerIndex: Int = 0,
    val log: MutableList<String> = mutableListOf(),
    val players: MutableList<Player> = mutableListOf(),
    val drawStack: Stack<Card> = Stack(),
    val discardStack: Stack<Card> = Stack(),
    val centerCards: MutableList<Card> = mutableListOf()
) {

    /**
     * Appends a new message to the game log.
     *
     * The log is used to document important game events such as
     * turn changes, pushes, switches, or round updates.
     *
     * @param message The message to be added to the log.
     */
    fun addLog(message: String) {
        log.add(message)
    }
}