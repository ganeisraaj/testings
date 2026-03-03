package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * Interstitial scene for hotseat mode.
 * Asking the next player to take the device.
 */
class GameNextPlayerScene(
    private val rootService: RootService,
    private val application: BoardGameApplication
) : BoardGameScene(1200, 700), Refreshable {

    private val tableGreen = ColorVisual(46, 125, 50)
    private val overlayVisual = ColorVisual(0, 0, 0, 200)

    private val bg = Label(0, 0, 1200, 700, "").apply { visual = tableGreen }
    private val overlay = Label(0, 0, 1200, 700, "").apply { visual = overlayVisual }

    private val panel = Label(300, 200, 600, 300, "").apply {
        visual = ColorVisual(230, 230, 255)
    }

    private val infoLabel = Label(310, 220, 580, 160, "Next Player").apply {
        font = Font(size = 22, fontWeight = Font.FontWeight.BOLD)
    }

    private val startTurnBtn = Button(500, 400, 200, 60, "START TURN").apply {
        visual = ColorVisual(0, 180, 0)
        font = Font(size = 20, color = Color(255, 255, 255), fontWeight = Font.FontWeight.BOLD)
    }

    init {
        rootService.addRefreshable(this)
        addComponents(bg, overlay, panel, infoLabel, startTurnBtn)

        startTurnBtn.onMouseClicked = click@{
            (application as SopraApplication).showGameScene()
            return@click
        }
    }

    override fun refreshAfterStartNewGame() = updateMsg()
    override fun refreshAfterTurnEnd() = updateMsg()

    private fun updateMsg() {
        val game = rootService.currentGame ?: return
        val player = game.players[game.currentPlayerIndex]
        infoLabel.text = "It's ${player.name}'s turn!\nTake the device and press START TURN."
        // Directly showing GameScene to skip handover as per request in MainMenuScene/GameNextPlayerScene
        (application as SopraApplication).showGameScene()
    }
}