package gui

import entity.Player
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
 * Final scene showing the game results and the ranking.
 */
class GameFinishedScene(
    private val rootService: RootService,
    private val application: BoardGameApplication
) : BoardGameScene(1200, 700), Refreshable {

    private val tableGreen: ColorVisual = ColorVisual(46, 125, 50)
    private val overlayVisual: ColorVisual = ColorVisual(0, 0, 0, 160)

    private val bg: Label = Label(0, 0, 1200, 700, "")
    private val overlay: Label = Label(0, 0, 1200, 700, "")

    private val panel: Label = Label(300, 150, 600, 400, "")

    private val title: Label = Label(300, 180, 600, 60, "GAME OVER")

    private val rank1: Label = Label(300, 250, 600, 40, "")
    private val rank2: Label = Label(300, 300, 600, 40, "")
    private val rank3: Label = Label(300, 350, 600, 40, "")
    private val rank4: Label = Label(300, 400, 600, 40, "")

    private val backBtn: Button = Button(500, 480, 200, 50, "Back to Menu")

    init {
        rootService.addRefreshable(this)
        
        bg.visual = tableGreen
        overlay.visual = overlayVisual
        
        val panelColor: ColorVisual = ColorVisual(245, 245, 245)
        panel.visual = panelColor
        
        val titleFont: Font = Font(size = 42, fontWeight = Font.FontWeight.BOLD)
        title.font = titleFont

        val rank1Font: Font = Font(size = 24, fontWeight = Font.FontWeight.BOLD)
        rank1.font = rank1Font
        
        val otherRankFont: Font = Font(size = 22)
        rank2.font = otherRankFont
        rank3.font = otherRankFont
        rank4.font = otherRankFont

        backBtn.visual = ColorVisual(80, 80, 200)
        backBtn.font = Font(size = 18, color = Color(255, 255, 255), fontWeight = Font.FontWeight.BOLD)

        addComponents(bg, overlay, panel, title, rank1, rank2, rank3, rank4, backBtn)

        backBtn.onMouseClicked = {
            val app: SopraApplication = application as SopraApplication
            app.showMainMenu()
        }
    }

    override fun refreshAfterGameEnd(ranking: List<Player>) {
        val labels: List<Label> = listOf(rank1, rank2, rank3, rank4)
        
        // Reset labels
        for (i in 0 until labels.size) {
            labels[i].text = ""
        }
        
        var lastScore: entity.ScoreTable? = null
        var lastRank: Int = 1
        
        for (i in 0 until ranking.size) {
            if (i < labels.size) {
                val player: Player = ranking[i]
                
                if (player.score != lastScore) {
                    lastRank = i + 1
                    lastScore = player.score
                }
                
                var positionText: String = ""
                if (lastRank == 1) {
                    positionText = "1st Place"
                } else if (lastRank == 2) {
                    positionText = "2nd Place"
                } else if (lastRank == 3) {
                    positionText = "3rd Place"
                } else {
                    positionText = lastRank.toString() + "th Place"
                }
                
                val playerName: String = player.name.uppercase()
                val playerScore: String = player.score.toString()
                labels[i].text = positionText + ": " + playerName + " (" + playerScore + ")"
            }
        }
        
        val app: SopraApplication = application as SopraApplication
        app.showGameFinishedScene()
    }
}