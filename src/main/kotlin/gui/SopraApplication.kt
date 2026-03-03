package gui

import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/**
 * Main application class for Push Poker.
 * Orchestrates scene switching and holds the RootService.
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

    fun showMainMenu() {
        this.showGameScene(mainMenuScene)
    }

    fun showGameScene() {
        this.showGameScene(gameScene)
    }

    fun showNextPlayerScene() {
        this.showGameScene(gameNextPlayerScene)
    }

    fun showGameFinishedScene() {
        this.showGameScene(gameFinishedScene)
    }
}