package entity

import java.util.Stack

/**
 * Stores the entire state of a Push Poker game.
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
     * Adds a message to the game log.
     */
    fun addLog(message: String) {
        log.add(message)
    }
}