package service

import entity.Card
import entity.Player

/**
 * Interface for the UI to receive updates from the service layer.
 */
interface Refreshable {

    /** Called after a new game starts. */
    fun refreshAfterStartNewGame() {}

    /** Called after the game ends. */
    fun refreshAfterGameEnd(ranking: List<Player>) {}

    /** Called after a turn starts. */
    fun refreshAfterTurnStart() {}

    /** Called after a turn ends. */
    fun refreshAfterTurnEnd() {}

    /** Called after a swap action. */
    fun refreshAfterSwitch() {}

    /** Called after a push left. */
    fun refreshAfterPushLeft(newCard: Card) {}

    /** Called after a push right. */
    fun refreshAfterPushRight(newCard: Card) {}

    /** Called when an error occurs. */
    fun refreshAfterError(message: String) {}

    /** Called when a new log entry is added. */
    fun refreshLog(message: String) {}
}