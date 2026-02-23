package entity

import java.util.Stack

data class Game(
    var nRounds: Int = 0,
    var curPlayerIdx: Int = 0,
    val log: MutableList<String> = mutableListOf(),
    val players: MutableList<Player> = mutableListOf(),
    val drawStack: Stack<Card> = Stack(),
    val playStack: Stack<Card> = Stack(),
    val centerCards: MutableList<Card> = mutableListOf()
) {
    fun addLog(message: String) {
        log.add(message)
    }
}