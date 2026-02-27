package service

import entity.Game

/**
 * Central access point for the service layer and the current entity state.
 */
class RootService {

    /** The currently active game, or null if no game has been started yet. */
    var currentGame: Game? = null

    /** Service responsible for game setup and turn flow. */
    val gameService: GameService = GameService(this)

    /** Service responsible for player actions during a turn. */
    val playerActionService: PlayerActionService = PlayerActionService(this)

    /**
     * Registers a [Refreshable] at all services that can trigger UI updates.
     */
    fun addRefreshable(refreshable: Refreshable) {
        gameService.addRefreshable(refreshable)
        playerActionService.addRefreshable(refreshable)
    }
}