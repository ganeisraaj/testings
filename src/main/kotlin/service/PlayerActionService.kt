package service

import entity.Card
import entity.Game

class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    private fun requireGame(): Game =
        rootService.currentGame ?: throw IllegalArgumentException("No active game.")

    private fun currentPlayer(game: Game) =
        game.players[game.currentPlayerIndex]

    fun pushLeft() {
        val game = requireGame()
        val player = currentPlayer(game)

        try {
            ensureDrawStackAvailable(game, player)
        } catch (e: IllegalStateException) {
            onAllRefreshables { refreshAfterError(e.message ?: "Error") }
            return
        }

        reduceAction(game)

        val newCard = game.drawStack.pop()
        if (game.centerCards.isNotEmpty()) {
            game.discardStack.push(game.centerCards.removeAt(0))
        }
        game.centerCards.add(newCard)

        rootService.gameService.updateLog("${player.name} pushed left.")
        onAllRefreshables { refreshAfterPush(newCard, -1) }

        if (currentPlayer(game).actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    fun pushRight() {
        val game = requireGame()
        val player = currentPlayer(game)

        try {
            ensureDrawStackAvailable(game, player)
        } catch (e: IllegalStateException) {
            onAllRefreshables { refreshAfterError(e.message ?: "Error") }
            return
        }

        reduceAction(game)

        val newCard = game.drawStack.pop()
        if (game.centerCards.isNotEmpty()) {
            game.discardStack.push(game.centerCards.removeAt(game.centerCards.lastIndex))
        }
        game.centerCards.add(0, newCard)

        rootService.gameService.updateLog("${player.name} pushed right.")
        onAllRefreshables { refreshAfterPush(newCard, +1) }

        if (currentPlayer(game).actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

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

        val openPos = posText(openCardIndex)
        val centerPos = posText(centerCardIndex)

        rootService.gameService.updateLog(
            "${player.name} swapped their $openPos open card with the $centerPos center card."
        )

        onAllRefreshables { refreshAfterSwitch() }

        if (currentPlayer(game).actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

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

        rootService.gameService.updateLog(
            "${player.name} swapped all open cards with the center cards."
        )

        onAllRefreshables { refreshAfterSwitch() }

        if (currentPlayer(game).actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Skips an action (Skip Swap).
     */
    fun skip() {
        val game = requireGame()
        val player = currentPlayer(game)
        
        reduceAction(game)
        
        rootService.gameService.updateLog("${player.name} skipped an action.")
        onAllRefreshables { refreshAfterSwitch() }

        if (currentPlayer(game).actionsLeft == 0) {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Ensures draw stack is available.
     * If empty → refill from discard stack (spec rule).
     * If still empty → error.
     */
    private fun ensureDrawStackAvailable(game: Game, player: entity.Player) {
        if (game.drawStack.isEmpty()) {

            if (game.discardStack.isNotEmpty()) {
                rootService.gameService.refillDrawStack()
                rootService.gameService.updateLog("Draw stack was empty. Discard stack was reshuffled.")
            }

            if (game.drawStack.isEmpty()) {
                throw IllegalStateException("No cards available to draw.")
            }
        }
    }

    private fun reduceAction(game: Game) {
        val player = currentPlayer(game)
        require(player.actionsLeft > 0) { "No actions left for current player." }
        player.actionsLeft -= 1
    }

    private fun posText(index: Int): String = when (index) {
        0 -> "left"
        1 -> "middle"
        2 -> "right"
        else -> "?"
    }
}
