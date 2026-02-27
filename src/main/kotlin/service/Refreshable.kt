package service

import entity.Card
import entity.Player

/**
 * Mechanism for the service layer to notify the view layer (GUI) about state changes.
 *
 * All methods provide default (empty) implementations so that implementing GUI classes
 * only need to override callbacks relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {

    /** Called after a new game has been started. */
    fun refreshAfterStartNewGame() {}

    /**
     * Called after the game ended.
     *
     * @param ranking Players ordered by their final placement (highest first).
     */
    fun refreshAfterGameEnd(ranking: List<Player>) {}

    /** Called after a new player's turn starts. */
    fun refreshAfterTurnStart() {}

    /** Called after a player's turn ends. */
    fun refreshAfterTurnEnd() {}

    /** Called after a switch action was executed. */
    fun refreshAfterSwitch() {}

    /**
     * Called after a push action was executed.
     *
     * @param newCard The new card that was pushed into the center.
     * @param direction The push direction (e.g. -1 left, +1 right).
     */
    fun refreshAfterPush(newCard: Card, direction: Int) {}

    /**
     * Called when an error occurred in the service layer.
     *
     * @param message Human-readable error message.
     */
    fun refreshAfterError(message: String) {}

    /**
     * Called when a log entry was added.
     *
     * @param message The new log message.
     */
    fun refreshLog(message: String) {}
}