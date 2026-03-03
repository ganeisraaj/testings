package service

import entity.Card
import entity.Game

/**
 * This service class is where I put all the player moves like pushing cards
 * and swapping them. I tried to keep it very close to the UML chart.
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game exists.")

    /**
     * This pushes a new card into the center from the left side. 
     * The card that was on the right gets pushed out into the discard pile.
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
     * Same as pushLeft but from the other side. A new card comes in from the 
     * right and the leftmost one is removed.
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
     * Swaps one card from the player's hand with one from the middle. 
     * I check the indices first to make sure everything is in range.
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

    /**
     * Swaps all three open cards with the three center cards at once.
     */
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
     * This method allows players to pass if they don't want to swap anything.
     * // I had to check the rules again: this still counts as one of the 2 actions!
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
     * Keeps track of how many moves the player has left in their turn.
     * If they run out, it throws an error.
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
