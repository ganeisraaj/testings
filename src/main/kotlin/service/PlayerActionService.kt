package service

import entity.Card
import entity.Game

/**
 * Handles all player moves like pushing and switching cards.
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game exists.")

    /**
     * Pushes from the left and discards the right card.
     */
    fun pushLeft() {
        val currentGame = requireGame()
        val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]

        // 1. Check for cards
        if (currentGame.drawStack.isEmpty()) {
            rootService.gameService.refillDrawStack()
        }

        reduceAction()

        // 2. Perform logic
        val newlyDrawnCard = currentGame.drawStack.pop()
        
        // Card leaving from the right (index 2)
        val cardToDiscard = currentGame.centerCards.removeAt(2)
        currentGame.discardStack.push(cardToDiscard)
        
        // New card arriving at the left
        currentGame.centerCards.add(0, newlyDrawnCard)

        rootService.gameService.updateLogMessage("${currentPlayer.name} pushed left and drew a new card.")
        
        // 3. UI Sync
        onAllRefreshables { it.refreshAfterPushLeft(newlyDrawnCard) }

        // Step end check
        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Pushes from the right and discards the left card.
     */
    fun pushRight() {
        val currentGame = requireGame()
        val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]

        // 1. Check for cards
        if (currentGame.drawStack.isEmpty()) {
            rootService.gameService.refillDrawStack()
        }

        reduceAction()

        // 2. Perform logic
        val newlyDrawnCard = currentGame.drawStack.pop()
        
        // Card leaving from the left (index 0)
        val cardToDiscard = currentGame.centerCards.removeAt(0)
        currentGame.discardStack.push(cardToDiscard)
        
        // New card arriving at the right
        currentGame.centerCards.add(newlyDrawnCard)

        rootService.gameService.updateLogMessage("${currentPlayer.name} pushed right and drew a new card.")
        
        // 3. UI Sync
        onAllRefreshables { it.refreshAfterPushRight(newlyDrawnCard) }

        // Step end check
        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Swaps one card from the player hand with a center card.
     */
    fun switchOne(openCardIndex: Int, centerCardIndex: Int) {
        val currentGame = requireGame()
        val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]

        if (openCardIndex !in 0..2 || centerCardIndex !in 0..2) {
            throw IllegalArgumentException("Invalid switch indices.")
        }

        reduceAction()

        // Swap without sorting
        val oldOpenCard = currentPlayer.openCards[openCardIndex]
        currentPlayer.openCards[openCardIndex] = currentGame.centerCards[centerCardIndex]
        currentGame.centerCards[centerCardIndex] = oldOpenCard

        rootService.gameService.updateLogMessage("${currentPlayer.name} swapped card $openCardIndex with center $centerCardIndex.")
        
        onAllRefreshables { it.refreshAfterSwitch() }

        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /** Swaps all three player cards with the center. */
    fun switchAll() {
        val currentGame = requireGame()
        val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]

        reduceAction()

        // Three individual swaps to maintain fixed positions
        for (i in 0..2) {
            val playerCard = currentPlayer.openCards[i]
            val centerCard = currentGame.centerCards[i]
            
            currentPlayer.openCards[i] = centerCard
            currentGame.centerCards[i] = playerCard
        }

        rootService.gameService.updateLogMessage("${currentPlayer.name} initiated a full swap.")
        
        onAllRefreshables { it.refreshAfterSwitch() }

        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Skips the swap. // Uses one action point.
     */
    fun skip() {
        val currentGame = requireGame()
        val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]
        
        reduceAction()
        
        rootService.gameService.updateLogMessage("${currentPlayer.name} skipped an action.")
        onAllRefreshables { it.refreshAfterSwitch() }

        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Reduces the number of actions left for the player.
     */
    fun reduceAction() {
        val currentGame = requireGame()
        val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]
        
        if (currentPlayer.actionsLeft <= 0) {
            throw IllegalStateException("No actions remaining.")
        }
        
        currentPlayer.actionsLeft -= 1
    }
}
