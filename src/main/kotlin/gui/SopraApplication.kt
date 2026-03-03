package gui

import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/**
 * Main application for the Push Poker game.
 */
class SopraApplication : BoardGameApplication("Push Poker") {

    private val rootService = RootService()

    private val mainMenuScene = MainMenuScene(rootService, this)
    private val gameNextPlayerScene = GameNextPlayerScene(rootService, this)
    private val gameScene = GameScene(rootService)
    private val gameFinishedScene = GameFinishedScene(rootService, this)

    init {
        showMainMenu()
    }

    /** Shows the main menu. */
    fun showMainMenu() {
        this.showGameScene(mainMenuScene)
    }

    /** Shows the main game scene. */
    fun showGameScene() {
        this.showGameScene(gameScene)
    }

    /** Shows the handover screen. */
    fun showNextPlayerScene() {
        this.showGameScene(gameNextPlayerScene)
    }

    /** Shows the end screen. */
    fun showGameFinishedScene() {
        this.showGameScene(gameFinishedScene)
    }
}