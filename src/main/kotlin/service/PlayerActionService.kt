package service

import entity.Card
import entity.Game
import entity.Player

/**
 * This service handles all the actions a player can do on their turn.
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Gets the current game or throws an error if there is none.
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
     * Takes a card from the draw stack and pushes it into the left side of the center.
     * The card on the far right of the center gets discarded.
     */
    fun pushLeft() {
        val gameInstance: Game = requireGame()
        val currentIndex: Int = gameInstance.currentPlayerIndex
        val activePlayer: Player = gameInstance.players[currentIndex]

        // Check if the draw stack is empty and refill it if needed
        val stackSize: Int = gameInstance.drawStack.size
        if (stackSize == 0) {
            rootService.gameService.refillDrawStack()
        }

        // Use up one action point for this move
        reduceAction()

        // Pull a new card from the top of the draw stack
        val newlyDrawn: Card = gameInstance.drawStack.pop()

        // Get the center cards list
        val center: MutableList<Card> = gameInstance.centerCards

        // Take the rightmost card and put it on the discard pile
        val discardCard: Card = center[2]
        gameInstance.discardStack.push(discardCard)

        // Shift the middle card to the right
        val middleCard: Card = center[1]
        center[2] = middleCard

        // Shift the left card to the middle
        val leftCard: Card = center[0]
        center[1] = leftCard

        // Put the newly drawn card into the left spot
        center[0] = newlyDrawn

        // Write to the log
        val logText: String = activePlayer.name + " pushed a card in from the left."
        rootService.gameService.updateLog(logText)

        // Tell the UI to update
        onAllRefreshables { refreshAfterPushLeft(newlyDrawn) }

        // If no actions are left, end the turn automatically
        val leftActions: Int = activePlayer.actionsLeft
        if (leftActions == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Takes a card from the draw stack and pushes it into the right side of the center.
     * The card on the far left of the center gets discarded.
     */
    fun pushRight() {
        val gameInstance: Game = requireGame()
        val currentIndex: Int = gameInstance.currentPlayerIndex
        val activePlayer: Player = gameInstance.players[currentIndex]

        // Check if the draw stack is empty and refill it if needed
        val stackSizeRight: Int = gameInstance.drawStack.size
        if (stackSizeRight == 0) {
            rootService.gameService.refillDrawStack()
        }

        // Use up one action point for this move
        reduceAction()

        // Pull a new card from the top of the draw stack
        val newlyDrawn: Card = gameInstance.drawStack.pop()

        // Get the center cards list
        val centerCardsList: MutableList<Card> = gameInstance.centerCards

        // Take the leftmost card and put it on the discard pile
        val discardCard: Card = centerCardsList[0]
        gameInstance.discardStack.push(discardCard)

        // Shift the middle card to the left
        val middleCard: Card = centerCardsList[1]
        centerCardsList[0] = middleCard

        // Shift the right card to the middle
        val rightCard: Card = centerCardsList[2]
        centerCardsList[1] = rightCard

        // Put the newly drawn card into the right spot
        centerCardsList[2] = newlyDrawn

        // Write to the log
        val logText: String = activePlayer.name + " pushed a card in from the right."
        rootService.gameService.updateLog(logText)

        // Tell the UI to update
        onAllRefreshables { refreshAfterPushRight(newlyDrawn) }

        // If no actions are left, end the turn automatically
        val rightActions: Int = activePlayer.actionsLeft
        if (rightActions == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Swaps one card from the player hand with one card from the center.
     */
    fun switchOne(openCardIndex: Int, centerCardIndex: Int) {
        val currentGame: Game = requireGame()
        val activeIdx: Int = currentGame.currentPlayerIndex
        val playerObj: Player = currentGame.players[activeIdx]

        // Check that the open card index is valid
        if (openCardIndex < 0) {
            throw IllegalArgumentException("Open card index is too low.")
        }
        if (openCardIndex > 2) {
            throw IllegalArgumentException("Open card index is too high.")
        }

        // Check that the center card index is valid
        if (centerCardIndex < 0) {
            throw IllegalArgumentException("Center card index is too low.")
        }
        if (centerCardIndex > 2) {
            throw IllegalArgumentException("Center card index is too high.")
        }

        // Use up one action point
        reduceAction()

        // Get both card lists
        val cardsInHand: MutableList<Card> = playerObj.openCards
        val cardsInCenter: MutableList<Card> = currentGame.centerCards

        // Store the two cards before swapping
        val cardFromPlayer: Card = cardsInHand[openCardIndex]
        val cardFromCenter: Card = cardsInCenter[centerCardIndex]

        // Do the actual swap
        cardsInHand[openCardIndex] = cardFromCenter
        cardsInCenter[centerCardIndex] = cardFromPlayer

        // Write to the log
        val logMsgText: String = playerObj.name + " swapped a card with the middle area."
        rootService.gameService.updateLog(logMsgText)

        // Tell the UI to update
        onAllRefreshables { refreshAfterSwitch() }

        // If no actions are left, end the turn automatically
        val actionsAfterSwitch: Int = playerObj.actionsLeft
        if (actionsAfterSwitch == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Swaps all three open cards of the player with all three center cards.
     */
    fun switchAll() {
        val gameObj: Game = requireGame()
        val activeIdx: Int = gameObj.currentPlayerIndex
        val playerObj: Player = gameObj.players[activeIdx]

        // Use up one action point
        reduceAction()

        // Get both card lists
        val hand: MutableList<Card> = playerObj.openCards
        val center: MutableList<Card> = gameObj.centerCards

        // Make sure both lists have exactly 3 cards
        if (hand.size < 3) {
            throw IllegalArgumentException("Player does not have 3 open cards.")
        }
        if (center.size < 3) {
            throw IllegalArgumentException("Center does not have 3 cards.")
        }

        // Swap card at index 0
        val pCard0: Card = hand[0]
        val cCard0: Card = center[0]
        hand[0] = cCard0
        center[0] = pCard0

        // Swap card at index 1
        val pCard1: Card = hand[1]
        val cCard1: Card = center[1]
        hand[1] = cCard1
        center[1] = pCard1

        // Swap card at index 2
        val pCard2: Card = hand[2]
        val cCard2: Card = center[2]
        hand[2] = cCard2
        center[2] = pCard2

        // Write to the log
        val logMsgText: String = playerObj.name + " swapped all cards with the center."
        rootService.gameService.updateLog(logMsgText)

        // Tell the UI to update
        onAllRefreshables { refreshAfterSwitch() }

        // If no actions are left, end the turn automatically
        val actionsAfterSwitchAll: Int = playerObj.actionsLeft
        if (actionsAfterSwitchAll == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Removes one action point from the current player.
     * Throws an error if the player has no actions left.
     */
    fun reduceAction() {
        val gameInstance: Game = requireGame()
        val currentIdx: Int = gameInstance.currentPlayerIndex
        val p: Player = gameInstance.players[currentIdx]

        // Check that the player still has actions available
        val currentActions: Int = p.actionsLeft
        if (currentActions <= 0) {
            throw IllegalStateException("No action points left for this player.")
        }

        // Subtract one action point manually
        val newVal: Int = currentActions - 1
        p.actionsLeft = newVal
    }

}