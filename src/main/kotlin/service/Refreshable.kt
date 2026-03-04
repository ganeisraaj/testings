package service

import entity.Card
import entity.Player

/**
 * Any class that wants to get updates from the service layer implements this.
 * All functions have empty default bodies so you only override what you need.
 */
interface Refreshable {

    // called when a new game is started
    fun refreshAfterStartNewGame() {}

    // called when the game is completely over
    fun refreshAfterGameEnd(ranking: List<Player>) {}

    // called at the start of a new turn
    fun refreshAfterTurnStart() {}

    // called when a turn ends
    fun refreshAfterTurnEnd() {}

    // called when a card swap happens
    fun refreshAfterSwitch() {}

    // called when a card is pushed in from the left
    fun refreshAfterPushLeft(newCard: Card) {}

    // called when a card is pushed in from the right
    fun refreshAfterPushRight(newCard: Card) {}

    // called when something goes wrong
    fun refreshAfterError(message: String) {}

    // called when a new message is added to the log
    fun refreshLog(message: String) {}

}