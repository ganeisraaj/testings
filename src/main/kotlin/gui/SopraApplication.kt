package gui

import entity.Player
import service.Refreshable
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/**
 * This is the main app class that holds all the scenes together.
 */
class SopraApplication : BoardGameApplication("Push Poker"), Refreshable {

    // The root service connects everything together
    private val rootService: RootService = RootService()

    // Create all the scenes here so they are ready to use
    private val mainMenuScene: MainMenuScene = MainMenuScene(rootService, this)
    private val gameNextPlayerScene: GameNextPlayerScene = GameNextPlayerScene(rootService, this)
    private val gameScene: GameScene = GameScene(rootService)
    private val gameFinishedScene: GameFinishedScene = GameFinishedScene(rootService, this)

    init {
        // Register this app as a refreshable so it gets notified too
        rootService.addRefreshable(this)

        // Start by showing the main menu
        showMainMenu()
    }

    /** Go to the main menu screen. */
    fun showMainMenu() {
        this.showGameScene(mainMenuScene)
    }

    /** Go to the main game screen. */
    fun showGameScene() {
        this.showGameScene(gameScene)
    }

    /** Go to the handover screen between turns. */
    fun showNextPlayerScene() {
        this.showGameScene(gameNextPlayerScene)
    }

    /** Go to the game over screen. */
    fun showGameFinishedScene() {
        this.showGameScene(gameFinishedScene)
    }

    override fun refreshAfterGameEnd(ranking: List<Player>) {
        // When the game ends, switch to the finished scene
        showGameFinishedScene()
    }

}