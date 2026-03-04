package service

import entity.Game

/**
 * This class connects the GUI to the service layer.
 * All services can be reached through here.
 */
class RootService {

    // Holds the current game, or null if no game is running
    var currentGame: Game? = null

    // The service that handles game flow like starting and ending
    val gameService: GameService = GameService(this)

    // The service that handles what a player can do on their turn
    val playerActionService: PlayerActionService = PlayerActionService(this)

    /**
     * Adds a refreshable to both services so it gets all updates.
     */
    fun addRefreshable(refreshable: Refreshable) {
        // Add to game service
        gameService.addRefreshable(refreshable)

        // Add to player action service too
        playerActionService.addRefreshable(refreshable)
    }

}