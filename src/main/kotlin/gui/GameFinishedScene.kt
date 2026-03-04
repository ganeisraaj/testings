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
 * This screen shows the final results when the game is over.
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

    // One label for each possible player rank
    private val rank1: Label = Label(300, 250, 600, 40, "")
    private val rank2: Label = Label(300, 300, 600, 40, "")
    private val rank3: Label = Label(300, 350, 600, 40, "")
    private val rank4: Label = Label(300, 400, 600, 40, "")

    private val backBtn: Button = Button(500, 480, 200, 50, "Back to Menu")

    init {
        rootService.addRefreshable(this)

        // Set up background and overlay
        bg.visual = tableGreen
        overlay.visual = overlayVisual

        // Style the result panel
        val panelColor: ColorVisual = ColorVisual(245, 245, 245)
        panel.visual = panelColor

        // Style the game over title
        val titleFont: Font = Font(size = 42, fontWeight = Font.FontWeight.BOLD)
        title.font = titleFont

        // First place gets a bigger font
        val rank1Font: Font = Font(size = 24, fontWeight = Font.FontWeight.BOLD)
        rank1.font = rank1Font

        // Other places get a slightly smaller font
        val otherRankFont: Font = Font(size = 22)
        rank2.font = otherRankFont
        rank3.font = otherRankFont
        rank4.font = otherRankFont

        // Style the back button
        val backBtnVisual: ColorVisual = ColorVisual(80, 80, 200)
        backBtn.visual = backBtnVisual
        val backBtnFont: Font = Font(size = 18, color = Color(255, 255, 255), fontWeight = Font.FontWeight.BOLD)
        backBtn.font = backBtnFont

        addComponents(bg, overlay, panel, title, rank1, rank2, rank3, rank4, backBtn)

        // Go back to the main menu when the button is clicked
        backBtn.onMouseClicked = {
            val app: SopraApplication = application as SopraApplication
            app.showMainMenu()
        }
    }

    override fun refreshAfterGameEnd(ranking: List<Player>) {
        // Clear all rank labels first
        rank1.text = ""
        rank2.text = ""
        rank3.text = ""
        rank4.text = ""

        var lastScore: entity.ScoreTable? = null
        var lastRank: Int = 1

        // Fill in rank label 1 if there is a first player
        if (ranking.size > 0) {
            val player0: Player = ranking[0]

            if (player0.score != lastScore) {
                lastRank = 1
                lastScore = player0.score
            }

            var positionText0: String = ""
            if (lastRank == 1) {
                positionText0 = "1st Place"
            } else if (lastRank == 2) {
                positionText0 = "2nd Place"
            } else if (lastRank == 3) {
                positionText0 = "3rd Place"
            } else {
                positionText0 = lastRank.toString() + "th Place"
            }

            val playerName0: String = player0.name.uppercase()
            val playerScore0: String = player0.score.toString()
            rank1.text = positionText0 + ": " + playerName0 + " (" + playerScore0 + ")"
        }

        // Fill in rank label 2 if there is a second player
        if (ranking.size > 1) {
            val player1: Player = ranking[1]

            if (player1.score != lastScore) {
                lastRank = 2
                lastScore = player1.score
            }

            var positionText1: String = ""
            if (lastRank == 1) {
                positionText1 = "1st Place"
            } else if (lastRank == 2) {
                positionText1 = "2nd Place"
            } else if (lastRank == 3) {
                positionText1 = "3rd Place"
            } else {
                positionText1 = lastRank.toString() + "th Place"
            }

            val playerName1: String = player1.name.uppercase()
            val playerScore1: String = player1.score.toString()
            rank2.text = positionText1 + ": " + playerName1 + " (" + playerScore1 + ")"
        }

        // Fill in rank label 3 if there is a third player
        if (ranking.size > 2) {
            val player2: Player = ranking[2]

            if (player2.score != lastScore) {
                lastRank = 3
                lastScore = player2.score
            }

            var positionText2: String = ""
            if (lastRank == 1) {
                positionText2 = "1st Place"
            } else if (lastRank == 2) {
                positionText2 = "2nd Place"
            } else if (lastRank == 3) {
                positionText2 = "3rd Place"
            } else {
                positionText2 = lastRank.toString() + "th Place"
            }

            val playerName2: String = player2.name.uppercase()
            val playerScore2: String = player2.score.toString()
            rank3.text = positionText2 + ": " + playerName2 + " (" + playerScore2 + ")"
        }

        // Fill in rank label 4 if there is a fourth player
        if (ranking.size > 3) {
            val player3: Player = ranking[3]

            if (player3.score != lastScore) {
                lastRank = 4
                lastScore = player3.score
            }

            var positionText3: String = ""
            if (lastRank == 1) {
                positionText3 = "1st Place"
            } else if (lastRank == 2) {
                positionText3 = "2nd Place"
            } else if (lastRank == 3) {
                positionText3 = "3rd Place"
            } else {
                positionText3 = lastRank.toString() + "th Place"
            }

            val playerName3: String = player3.name.uppercase()
            val playerScore3: String = player3.score.toString()
            rank4.text = positionText3 + ": " + playerName3 + " (" + playerScore3 + ")"
        }

        // Switch to the finished scene
        val app: SopraApplication = application as SopraApplication
        app.showGameFinishedScene()
    }

}