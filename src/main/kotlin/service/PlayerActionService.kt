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
        } else {
            return currentGame
        }
    }

    /**
     * Pushes a card from the deck into the left slot of the middle area.
     * The rightmost card of the middle area is discarded.
     */
    fun pushLeft() {
        val gameInstance: Game = requireGame()
        val currentIndex: Int = gameInstance.currentPlayerIndex
        val activePlayer: Player = gameInstance.players[currentIndex]

        // Check if we need to refill the deck from the discard pile
        val stackSize: Int = gameInstance.drawStack.size
        if (stackSize == 0) {
            rootService.gameService.refillDrawStack()
        }

        // We use up one action point for this move
        reduceAction()

        // Pull a new card from the draw stack
        val newlyDrawn: Card = gameInstance.drawStack.pop()
        
        // Grab the center cards list
        val center: MutableList<Card> = gameInstance.centerCards
        
        // Discard the card that is at the far right (index 2)
        val discardCard: Card = center[2]
        gameInstance.discardStack.push(discardCard)
        
        // Manually move the other two cards to the right
        val middleCard: Card = center[1]
        center[2] = middleCard
        
        val leftCard: Card = center[0]
        center[1] = leftCard
        
        // Put the freshly drawn card in the left spot (index 0)
        center[0] = newlyDrawn

        val logText: String = activePlayer.name + " pushed a card in from the left."
        rootService.gameService.updateLog(logText)
        
        // Notify the user interface of the change
        onAllRefreshables { refreshAfterPushLeft(newlyDrawn) }

        // Check if the current player should end their turn
        val leftActions: Int = activePlayer.actionsLeft
        if (leftActions == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Pushes a card from the deck into the right slot of the middle area.
     * The leftmost card of the middle area is discarded.
     */
    fun pushRight() {
        val gameInstance: Game = requireGame()
        val currentIndex: Int = gameInstance.currentPlayerIndex
        val activePlayer: Player = gameInstance.players[currentIndex]

        // Refill the deck if it's empty
        if (gameInstance.drawStack.isEmpty()) {
            rootService.gameService.refillDrawStack()
        }

        // Use up one action point
        reduceAction()

        // Take the top card from the deck stack
        val newlyDrawn: Card = gameInstance.drawStack.pop()
        
        // Get the list of cards in the middle
        val centerCardsList: MutableList<Card> = gameInstance.centerCards
        
        // Discard the card that is at the far left (index 0)
        val discardCard: Card = centerCardsList[0]
        gameInstance.discardStack.push(discardCard)
        
        // Shift middle and right cards to the left
        val middleCard: Card = centerCardsList[1]
        centerCardsList[0] = middleCard
        
        val rightCard: Card = centerCardsList[2]
        centerCardsList[1] = rightCard
        
        // Put the drawn card in the right spot (index 2)
        centerCardsList[2] = newlyDrawn

        val logText: String = activePlayer.name + " pushed a card in from the right."
        rootService.gameService.updateLog(logText)
        
        // Synchronize with the UI
        onAllRefreshables { refreshAfterPushRight(newlyDrawn) }

        // Determine if turn is over
        if (activePlayer.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Swaps exactly one card from the player's visible hand with one card from the center.
     */
    fun switchOne(openCardIndex: Int, centerCardIndex: Int) {
        val currentGame: Game = requireGame()
        val activeIdx: Int = currentGame.currentPlayerIndex
        val playerObj: Player = currentGame.players[activeIdx]

        // Validate the indexes using simple if checks
        if (openCardIndex < 0) {
             throw IllegalArgumentException("Index too low.")
        }
        if (openCardIndex > 2) {
             throw IllegalArgumentException("Index too high.")
        }
        if (centerCardIndex < 0) {
             throw IllegalArgumentException("Center index too low.")
        }
        if (centerCardIndex > 2) {
             throw IllegalArgumentException("Center index too high.")
        }

        // Action reduction
        reduceAction()

        // Perform the swap logic
        val cardsInHand: MutableList<Card> = playerObj.openCards
        val cardsInCenter: MutableList<Card> = currentGame.centerCards

        val cardFromPlayer: Card = cardsInHand[openCardIndex]
        val cardFromCenter: Card = cardsInCenter[centerCardIndex]

        cardsInHand[openCardIndex] = cardFromCenter
        cardsInCenter[centerCardIndex] = cardFromPlayer

        val logMsgText: String = playerObj.name + " swapped a card with the middle area."
        rootService.gameService.updateLog(logMsgText)
        
        // Refresh the interface
        onAllRefreshables { refreshAfterSwitch() }

        // End turn if needed
        if (playerObj.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /** 
     * Swaps all three visible player cards with all three center cards.
     */
    fun switchAll() {
        val gameObj: Game = requireGame()
        val activeIdx: Int = gameObj.currentPlayerIndex
        val playerObj: Player = gameObj.players[activeIdx]

        reduceAction()

        // Loop 3 times and swap each pair of cards
        val hand: MutableList<Card> = playerObj.openCards
        val center: MutableList<Card> = gameObj.centerCards
        
        for (i in 0 until 3) {
            val pCard: Card = hand[i]
            val cCard: Card = center[i]
            
            // Swap values
            hand[i] = cCard
            center[i] = pCard
        }

        val logMsgText: String = playerObj.name + " swapped all cards with the center."
        rootService.gameService.updateLog(logMsgText)
        
        onAllRefreshables { refreshAfterSwitch() }

        if (playerObj.actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Checks if the player has actions left and removes one point.
     */
    fun reduceAction() {
        val gameInstance: Game = requireGame()
        val currentIdx: Int = gameInstance.currentPlayerIndex
        val p: Player = gameInstance.players[currentIdx]
        
        // Guard check for actions
        val currentActions: Int = p.actionsLeft
        if (currentActions <= 0) {
            throw IllegalStateException("No action points left for this player.")
        }
        
        // Manual subtraction
        val newVal: Int = currentActions - 1
        p.actionsLeft = newVal
    }

}
