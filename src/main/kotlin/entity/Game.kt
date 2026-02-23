package entity

import java.util.Stack

data class Game(
    var nRounds: Int,
    var curPlayerIdx: Int,
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