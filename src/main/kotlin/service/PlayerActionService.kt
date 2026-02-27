package service

import entity.Card

/**
 * Service responsible for handling player actions during the current turn.
 *
 * This class belongs to the service layer and therefore contains game logic.
 * It operates on the currently active game stored in [RootService.currentGame].
 *
 * UML public methods:
 * - [pushLeft]
 * - [pushRight]
 * - [switchOne]
 * - [switchAll]
 *
 * UML private method:
 * - reduceAction()
 *
 * @property rootService Reference to the central [RootService].
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Pushes the center cards to the left.
     *
     * Preconditions:
     * - A game must be active.
     * - The current player must have at least one action left.
     *
     * Postconditions:
     * - The current player's actionsLeft is decreased by 1.
     * - If drawStack is empty: [Refreshable.refreshAfterError] is triggered and the method returns.
     * - Otherwise: a new card is drawn into the center and one card is moved to discardStack.
     * - [Refreshable.refreshAfterPush] is triggered with direction = -1.
     *
     * @throws IllegalArgumentException if no game is active or the current player has no actions left.
     */
    fun pushLeft() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        reduceAction()

        if (game.drawStack.isEmpty()) {
            onAllRefreshables { refreshAfterError("Draw stack is empty.") }
            return
        }

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
     *
     * Postconditions:
     * - The current player's actionsLeft is decreased by 1.
     * - If drawStack is empty: [Refreshable.refreshAfterError] is triggered and the method returns.
     * - Otherwise: a new card is drawn into the center and one card is moved to discardStack.
     * - [Refreshable.refreshAfterPush] is triggered with direction = +1.
     *
     * @throws IllegalArgumentException if no game is active or the current player has no actions left.
     */
    fun pushRight() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        reduceAction()

        if (game.drawStack.isEmpty()) {
            onAllRefreshables { refreshAfterError("Draw stack is empty.") }
            return
        }

        val newCard = game.drawStack.pop()
        if (game.centerCards.isNotEmpty()) {
            game.discardStack.push(game.centerCards.removeAt(game.centerCards.lastIndex))
        }
        game.centerCards.add(0, newCard)

        onAllRefreshables { refreshAfterPush(newCard, +1) }
    }

    /**
     * Switches one open card of the current player with one center card.
     *
     * Preconditions:
     * - A game must be active.
     * - The current player must have at least one action left.
     * - Indices must be within bounds.
     *
     * Postconditions:
     * - The selected open card and center card are swapped.
     * - The current player's actionsLeft is decreased by 1.
     * - [Refreshable.refreshAfterSwitch] is triggered.
     *
     * @param openCardIndex Index of the open card in the current player's openCards list.
     * @param centerCardIndex Index of the card in the game's centerCards list.
     *
     * @throws IllegalArgumentException if no game is active or the current player has no actions left.
     * @throws IndexOutOfBoundsException if an index is invalid.
     */
    fun switchOne(openCardIndex: Int, centerCardIndex: Int) {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        val player = game.players[game.currentPlayerIndex]
        reduceAction()

        val temp: Card = player.openCards[openCardIndex]
        player.openCards[openCardIndex] = game.centerCards[centerCardIndex]
        game.centerCards[centerCardIndex] = temp

        onAllRefreshables { refreshAfterSwitch() }
    }

    /**
     * Switches all open cards of the current player with the center cards.
     *
     * Preconditions:
     * - A game must be active.
     * - The current player must have at least one action left.
     *
     * Postconditions:
     * - Cards are swapped pairwise up to min(openCards.size, centerCards.size).
     * - The current player's actionsLeft is decreased by 1.
     * - [Refreshable.refreshAfterSwitch] is triggered.
     *
     * @throws IllegalArgumentException if no game is active or the current player has no actions left.
     */
    fun switchAll() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        val player = game.players[game.currentPlayerIndex]
        reduceAction()

        val n = minOf(player.openCards.size, game.centerCards.size)
        for (i in 0 until n) {
            val temp = player.openCards[i]
            player.openCards[i] = game.centerCards[i]
            game.centerCards[i] = temp
        }

        onAllRefreshables { refreshAfterSwitch() }
    }

    /**
     * Reduces the remaining actions of the current player by one.
     *
     * Preconditions:
     * - A game must be active.
     * - actionsLeft > 0
     *
     * Postconditions:
     * - actionsLeft is decreased by 1.
     *
     * @throws IllegalArgumentException if no game is active or no actions are left.
     */
    private fun reduceAction() {
        val game = rootService.currentGame ?: throw IllegalArgumentException("No active game.")
        val player = game.players[game.currentPlayerIndex]
        require(player.actionsLeft > 0) { "No actions left for current player." }
        player.actionsLeft -= 1
    }
}