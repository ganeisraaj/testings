package service

import entity.Card
import entity.Game

/**
 * Service responsible for handling player actions during the current turn.
 *
 * This service contains the game logic related to actions a player can perform
 * during their turn. It operates on the currently active game stored in
 * [RootService.currentGame].
 *
 * Public UML methods:
 * - [pushLeft]
 * - [pushRight]
 * - [switchOne]
 * - [switchAll]
 *
 * @property rootService Reference to the central [RootService].
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Returns the currently active game.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game.")

    /**
     * Returns the current player of the given game.
     */
    private fun currentPlayer(game: Game) =
        game.players[game.currentPlayerIndex]

    /**
     * Pushes the center cards to the left.
     *
     * Preconditions:
     * - A game must be active.
     * - The current player must have at least one action left.
     * - The draw stack must not be empty.
     *
     * Postconditions:
     * - One card is removed from the left side of the center (if available)
     *   and placed on the discard stack.
     * - A new card is drawn from the draw stack and added to the center.
     * - The current player's remaining actions are reduced by one.
     * - If no actions remain afterwards, the turn ends automatically.
     * - [Refreshable.refreshAfterPush] is triggered.
     *
     * If the draw stack is empty, the method triggers
     * [Refreshable.refreshAfterError] and returns without consuming an action.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun pushLeft() {
        val game = requireGame()

        if (game.drawStack.isEmpty()) {
            onAllRefreshables { refreshAfterError("Draw stack is empty.") }
            return
        }

        reduceAction(game)

        val newCard = game.drawStack.pop()
        if (game.centerCards.isNotEmpty()) {
            game.discardStack.push(game.centerCards.removeAt(0))
        }
        game.centerCards.add(newCard)

        onAllRefreshables { refreshAfterPush(newCard, -1) }
    }

    /**
     * Pushes the center cards to the right.
     *
     * Preconditions:
     * - A game must be active.
     * - The current player must have at least one action left.
     * - The draw stack must not be empty.
     *
     * Postconditions:
     * - One card is removed from the right side of the center (if available)
     *   and placed on the discard stack.
     * - A new card is drawn from the draw stack and inserted at the beginning
     *   of the center.
     * - The current player's remaining actions are reduced by one.
     * - If no actions remain afterwards, the turn ends automatically.
     * - [Refreshable.refreshAfterPush] is triggered.
     *
     * If the draw stack is empty, the method triggers
     * [Refreshable.refreshAfterError] and returns without consuming an action.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun pushRight() {
        val game = requireGame()

        if (game.drawStack.isEmpty()) {
            onAllRefreshables { refreshAfterError("Draw stack is empty.") }
            return
        }

        reduceAction(game)

        val newCard = game.drawStack.pop()
        if (game.centerCards.isNotEmpty()) {
            game.discardStack.push(game.centerCards.removeAt(game.centerCards.lastIndex))
        }
        game.centerCards.add(0, newCard)

        onAllRefreshables { refreshAfterPush(newCard, +1) }
    }

    /**
     * Switches one open card of the current player with a center card.
     *
     * Preconditions:
     * - A game must be active.
     * - The current player must have at least one action left.
     * - Both indices must be within valid bounds.
     *
     * Postconditions:
     * - The selected open card and center card are swapped.
     * - The current player's remaining actions are reduced by one.
     * - If no actions remain afterwards, the turn ends automatically.
     * - [Refreshable.refreshAfterSwitch] is triggered.
     *
     * @param openCardIndex Index of the player's open card.
     * @param centerCardIndex Index of the center card.
     *
     * @throws IllegalArgumentException if no game is active.
     * @throws IndexOutOfBoundsException if an index is invalid.
     */
    fun switchOne(openCardIndex: Int, centerCardIndex: Int) {
        val game = requireGame()
        val player = currentPlayer(game)

        if (openCardIndex !in player.openCards.indices) {
            throw IndexOutOfBoundsException("openCardIndex out of bounds: $openCardIndex")
        }
        if (centerCardIndex !in game.centerCards.indices) {
            throw IndexOutOfBoundsException("centerCardIndex out of bounds: $centerCardIndex")
        }

        reduceAction(game)

        val temp: Card = player.openCards[openCardIndex]
        player.openCards[openCardIndex] = game.centerCards[centerCardIndex]
        game.centerCards[centerCardIndex] = temp

        onAllRefreshables { refreshAfterSwitch() }
    }

    /**
     * Switches all open cards of the current player with the center cards.
     *
     * Cards are swapped pairwise up to the minimum size of both lists.
     *
     * Preconditions:
     * - A game must be active.
     * - The current player must have at least one action left.
     *
     * Postconditions:
     * - Cards are exchanged pairwise.
     * - The current player's remaining actions are reduced by one.
     * - If no actions remain afterwards, the turn ends automatically.
     * - [Refreshable.refreshAfterSwitch] is triggered.
     *
     * @throws IllegalArgumentException if no game is active.
     */
    fun switchAll() {
        val game = requireGame()
        val player = currentPlayer(game)

        reduceAction(game)

        val n = minOf(player.openCards.size, game.centerCards.size)
        for (i in 0 until n) {
            val temp = player.openCards[i]
            player.openCards[i] = game.centerCards[i]
            game.centerCards[i] = temp
        }

        onAllRefreshables { refreshAfterSwitch() }
    }

    /**
     * Decreases the remaining actions of the current player by one.
     *
     * If the player has no actions left afterwards, the turn is ended automatically.
     *
     * @throws IllegalArgumentException if no game is active.
     * @throws IllegalArgumentException if the player has no actions left.
     */
    private fun reduceAction(game: Game) {
        val player = currentPlayer(game)
        require(player.actionsLeft > 0) { "No actions left for current player." }

        player.actionsLeft -= 1

        if (player.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }
}