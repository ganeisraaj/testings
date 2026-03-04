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

    private val tableGreen: ColorVisual = ColorVisual(46, 125, 50)
    private val overlayVisual: ColorVisual = ColorVisual(0, 0, 0, 180)

    private val bg: Label = Label(0, 0, 1200, 700, "")
    private val overlay: Label = Label(0, 0, 1200, 700, "")

    // Top bars (visual parity with GameScene)
    private val roundField: Label = Label(20, 20, 150, 40, "ROUND: 1/1")
    private val actionsField: Label = Label(1030, 20, 150, 40, "Actions: 2/2")

    private val panel: Label = Label(300, 200, 600, 300, "")

    private val infoLabel: Label = Label(310, 250, 580, 60, "Player 1")
    private val subLabel: Label = Label(310, 320, 580, 40, "It's your turn!")

    private val startTurnBtn: Button = Button(500, 400, 200, 60, "START TURN")

    init {
        bg.visual = tableGreen
        overlay.visual = overlayVisual
        
        val roundVisual: ColorVisual = ColorVisual(180, 220, 180)
        roundField.visual = roundVisual
        roundField.font = Font(fontWeight = Font.FontWeight.BOLD)
        
        actionsField.visual = roundVisual
        actionsField.font = Font(fontWeight = Font.FontWeight.BOLD)
        
        panel.visual = ColorVisual(230, 230, 255, 200)
        
        infoLabel.font = Font(size = 48, fontWeight = Font.FontWeight.BOLD)
        
        subLabel.font = Font(size = 24)
        
        startTurnBtn.visual = ColorVisual(255, 150, 0)
        startTurnBtn.font = Font(size = 20, color = Color(255, 255, 255), fontWeight = Font.FontWeight.BOLD)

        rootService.addRefreshable(this)
        addComponents(bg, overlay, roundField, actionsField, panel, infoLabel, subLabel, startTurnBtn)

        startTurnBtn.onMouseClicked = {
            val app: SopraApplication = application as SopraApplication
            app.showGameScene()
        }
    }

    override fun refreshAfterStartNewGame() {
        updateMsg()
        val app: SopraApplication = application as SopraApplication
        app.showNextPlayerScene()
    }
    
    override fun refreshAfterTurnEnd() {
        updateMsg()
        val app: SopraApplication = application as SopraApplication
        app.showNextPlayerScene()
    }

    private fun updateMsg() {
        val game: entity.Game? = rootService.currentGame
        if (game == null) {
            return
        }
        val currentPlayerIdx: Int = game.currentPlayerIndex
        val player: entity.Player = game.players[currentPlayerIdx]
        
        roundField.text = "ROUND: " + game.currentRound + "/" + game.totalRounds
        actionsField.text = "Actions: " + player.actionsLeft + "/2"
        infoLabel.text = player.name
    }
}