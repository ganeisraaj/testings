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
 * This screen shows up between turns so the next player can sit down and get ready.
 */
class GameNextPlayerScene(
    private val rootService: RootService,
    private val application: BoardGameApplication
) : BoardGameScene(1200, 700), Refreshable {

    private val tableGreen: ColorVisual = ColorVisual(46, 125, 50)
    private val overlayVisual: ColorVisual = ColorVisual(0, 0, 0, 180)

    private val bg: Label = Label(0, 0, 1200, 700, "")
    private val overlay: Label = Label(0, 0, 1200, 700, "")

    // Top bars to show round and action info, same style as the main game screen
    private val roundField: Label = Label(20, 20, 150, 40, "ROUND: 1/1")
    private val actionsField: Label = Label(1030, 20, 150, 40, "Actions: 2/2")

    // Panel behind the player name and button
    private val panel: Label = Label(300, 200, 600, 300, "")

    // Shows the name of the player whose turn is next
    private val infoLabel: Label = Label(310, 250, 580, 60, "Player 1")

    // Small hint text below the name
    private val subLabel: Label = Label(310, 320, 580, 40, "It's your turn!")

    // Button to confirm and go to the game screen
    private val startTurnBtn: Button = Button(500, 400, 200, 60, "START TURN")

    init {
        // Set up background and dark overlay
        bg.visual = tableGreen
        overlay.visual = overlayVisual

        // Style the round and actions labels at the top
        val roundVisual: ColorVisual = ColorVisual(180, 220, 180)
        roundField.visual = roundVisual
        roundField.font = Font(fontWeight = Font.FontWeight.BOLD)

        actionsField.visual = roundVisual
        actionsField.font = Font(fontWeight = Font.FontWeight.BOLD)

        // Style the panel in the center
        val panelVisual: ColorVisual = ColorVisual(230, 230, 255, 200)
        panel.visual = panelVisual

        // Big bold font for the player name
        val infoFont: Font = Font(size = 48, fontWeight = Font.FontWeight.BOLD)
        infoLabel.font = infoFont

        // Smaller font for the subtitle
        val subFont: Font = Font(size = 24)
        subLabel.font = subFont

        // Orange button with white text
        val startBtnVisual: ColorVisual = ColorVisual(255, 150, 0)
        startTurnBtn.visual = startBtnVisual
        val startBtnFont: Font = Font(size = 20, color = Color(255, 255, 255), fontWeight = Font.FontWeight.BOLD)
        startTurnBtn.font = startBtnFont

        rootService.addRefreshable(this)

        addComponents(bg, overlay, roundField, actionsField, panel, infoLabel, subLabel, startTurnBtn)

        // When the player clicks start turn, go to the main game screen
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

    /**
     * Updates the labels to show the current player name and round info.
     */
    private fun updateMsg() {
        val game: entity.Game? = rootService.currentGame

        if (game == null) {
            return
        }

        val currentPlayerIdx: Int = game.currentPlayerIndex
        val player: entity.Player = game.players[currentPlayerIdx]

        val roundText: String = "ROUND: " + game.currentRound + "/" + game.totalRounds
        roundField.text = roundText

        val actionsText: String = "Actions: " + player.actionsLeft + "/2"
        actionsField.text = actionsText

        infoLabel.text = player.name
    }

}