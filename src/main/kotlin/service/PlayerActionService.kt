package service

import entity.Card
import entity.Game
import entity.Player

/**
 * Handles the moves that a player can make on their turn.
 * This includes sliding cards from the deck and swapping with the middle.
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Checks if the game is currently running.
     * Throws an error if no game is found.
     */
    private fun requireGame(): Game {
        val currentGame: Game? = rootService.currentGame
        if (currentGame == null) {
            throw IllegalArgumentException("No active game was found.")
        }
        return currentGame
    }

    /**
     * Pushes a card from the deck into the left slot of the middle area.
     * The rightmost card of the middle area is discarded.
     */
    fun pushLeft() {
        val currentGame: Game = requireGame()
        val pIndex: Int = currentGame.currentPlayerIndex
        val currentPlayer: Player = currentGame.players[pIndex]

        // If the draw stack is empty, we must refill it from the discard pile
        if (currentGame.drawStack.isEmpty()) {
            rootService.gameService.refillDrawStack()
        }

        // We use up one action point
        reduceAction()

        // Take a new card from the top of the deck
        val newCard: Card = currentGame.drawStack.pop()
        
        // Slide the center cards to the right
        // The card at index 2 (right) goes to the discard pile
        val cardForDiscard: Card = currentGame.centerCards[2]
        currentGame.discardStack.push(cardForDiscard)
        
        // Manual movement of elements to avoid complex list functions
        currentGame.centerCards[2] = currentGame.centerCards[1]
        currentGame.centerCards[1] = currentGame.centerCards[0]
        
        // Put the new card at index 0 (left)
        currentGame.centerCards[0] = newCard

        val logMsg: String = currentPlayer.name + " pushed a card in from the left."
        rootService.gameService.updateLog(logMsg)
        
        // Notification for the user interface
        onAllRefreshables { refreshAfterPushLeft(newCard) }

        // If no actions are left, the turn must end
        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Pushes a card from the deck into the right slot of the middle area.
     * The leftmost card of the middle area is discarded.
     */
    fun pushRight() {
        val currentGame: Game = requireGame()
        val pIndex: Int = currentGame.currentPlayerIndex
        val currentPlayer: Player = currentGame.players[pIndex]

        // Check for empty deck
        if (currentGame.drawStack.isEmpty()) {
            rootService.gameService.refillDrawStack()
        }

        // Subtract action point
        reduceAction()

        // Get the new card
        val newCard: Card = currentGame.drawStack.pop()
        
        // Slide the center cards to the left
        // The card at index 0 (left) goes to the discard pile
        val cardForDiscard: Card = currentGame.centerCards[0]
        currentGame.discardStack.push(cardForDiscard)
        
        // Shift middle and right card to the left
        currentGame.centerCards[0] = currentGame.centerCards[1]
        currentGame.centerCards[1] = currentGame.centerCards[2]
        
        // Put the new card at index 2 (right)
        currentGame.centerCards[2] = newCard

        val logMsg: String = currentPlayer.name + " pushed a card in from the right."
        rootService.gameService.updateLog(logMsg)
        
        // Sync with UI
        onAllRefreshables { refreshAfterPushRight(newCard) }

        // Turn management
        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Swaps exactly one card from the player's visible hand with one card from the center.
     */
    fun switchOne(openCardIndex: Int, centerCardIndex: Int) {
        val currentGame: Game = requireGame()
        val pIndex: Int = currentGame.currentPlayerIndex
        val currentPlayer: Player = currentGame.players[pIndex]

        // Validate that the provided numbers are inside the correct range (0 to 2)
        if (openCardIndex < 0 || openCardIndex > 2) {
             throw IllegalArgumentException("The hand index is invalid.")
        }
        if (centerCardIndex < 0 || centerCardIndex > 2) {
             throw IllegalArgumentException("The center index is invalid.")
        }

        reduceAction()

        // Perform the numerical swap
        val playerCard: Card = currentPlayer.openCards[openCardIndex]
        val centerCard: Card = currentGame.centerCards[centerCardIndex]

        currentPlayer.openCards[openCardIndex] = centerCard
        currentGame.centerCards[centerCardIndex] = playerCard

        val logMsg: String = currentPlayer.name + " swapped a card with the center."
        rootService.gameService.updateLog(logMsg)
        
        onAllRefreshables { refreshAfterSwitch() }

        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /** 
     * Swaps all three visible player cards with all three center cards.
     */
    fun switchAll() {
        val currentGame: Game = requireGame()
        val pIndex: Int = currentGame.currentPlayerIndex
        val currentPlayer: Player = currentGame.players[pIndex]

        reduceAction()

        // Loop through all three positions and swap the contents
        for (i in 0 until 3) {
            val pCard: Card = currentPlayer.openCards[i]
            val cCard: Card = currentGame.centerCards[i]
            
            currentPlayer.openCards[i] = cCard
            currentGame.centerCards[i] = pCard
        }

        val logMsg: String = currentPlayer.name + " swapped all cards with the center."
        rootService.gameService.updateLog(logMsg)
        
        onAllRefreshables { refreshAfterSwitch() }

        if (currentPlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Checks if the player has actions left and removes one point.
     */
    fun reduceAction() {
        val currentGame: Game = requireGame()
        val pIndex: Int = currentGame.currentPlayerIndex
        val currentPlayer: Player = currentGame.players[pIndex]
        
        // Make sure we are allowed to take an action
        if (currentPlayer.actionsLeft <= 0) {
            throw IllegalStateException("No action points left for this player.")
        }
        
        // Simple subtraction
        val oldActions: Int = currentPlayer.actionsLeft
        val newActions: Int = oldActions - 1
        currentPlayer.actionsLeft = newActions
    }

}
