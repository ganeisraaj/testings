package service

import entity.Game

/**
 * Main access point for the service layer.
 */
class RootService {

    /** The current game state. */
    var currentGame: Game? = null

    /** Service for game flow. */
    val gameService: GameService = GameService(this)

    /** Service for player moves. */
    val playerActionService: PlayerActionService = PlayerActionService(this)

    /**
     * Registers a refreshable to all services.
     */
    fun addRefreshable(refreshable: Refreshable) {
        gameService.addRefreshable(refreshable)
        playerActionService.addRefreshable(refreshable)
    }
}