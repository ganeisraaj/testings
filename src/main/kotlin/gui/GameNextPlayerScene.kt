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
 * Handover screen for switching between players.
 */
class GameNextPlayerScene(
    private val rootService: RootService,
    private val application: BoardGameApplication
) : BoardGameScene(1200, 700), Refreshable {

    private val tableGreen = ColorVisual(46, 125, 50)
    private val overlayVisual = ColorVisual(0, 0, 0, 180)

    private val bg = Label(0, 0, 1200, 700, "").apply { visual = tableGreen }
    private val overlay = Label(0, 0, 1200, 700, "").apply { visual = overlayVisual }

    // Top bars (visual parity with GameScene)
    private val roundField = Label(20, 20, 150, 40, "ROUND: 1/1").apply {
        visual = ColorVisual(180, 220, 180)
        font = Font(fontWeight = Font.FontWeight.BOLD)
    }
    private val actionsField = Label(1030, 20, 150, 40, "Actions: 2/2").apply {
        visual = ColorVisual(180, 220, 180)
        font = Font(fontWeight = Font.FontWeight.BOLD)
    }

    private val panel = Label(300, 200, 600, 300, "").apply {
        visual = ColorVisual(230, 230, 255, 200)
    }

    private val infoLabel = Label(310, 250, 580, 60, "Player 1").apply {
        font = Font(size = 48, fontWeight = Font.FontWeight.BOLD)
    }

    private val subLabel = Label(310, 320, 580, 40, "It's your turn!").apply {
        font = Font(size = 24)
    }

    private val startTurnBtn = Button(500, 400, 200, 60, "START TURN").apply {
        visual = ColorVisual(255, 150, 0)
        font = Font(size = 20, color = Color(255, 255, 255), fontWeight = Font.FontWeight.BOLD)
    }

    init {
        rootService.addRefreshable(this)
        addComponents(bg, overlay, roundField, actionsField, panel, infoLabel, subLabel, startTurnBtn)

        startTurnBtn.onMouseClicked = click@{
            (application as SopraApplication).showGameScene()
            return@click
        }
    }

    override fun refreshAfterStartNewGame() {
        updateMsg()
        (application as SopraApplication).showNextPlayerScene()
    }
    override fun refreshAfterTurnEnd() {
        updateMsg()
        (application as SopraApplication).showNextPlayerScene()
    }

    private fun updateMsg() {
        val game = rootService.currentGame ?: return
        val player = game.players[game.currentPlayerIndex]
        roundField.text = "ROUND: ${game.currentRound}/${game.totalRounds}"
        actionsField.text = "Actions: ${player.actionsLeft}/2"
        infoLabel.text = player.name
    }
}