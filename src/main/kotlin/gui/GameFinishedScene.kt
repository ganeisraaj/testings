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

    private val tableGreen = ColorVisual(46, 125, 50)
    private val overlayVisual = ColorVisual(0, 0, 0, 160)

    private val bg = Label(0, 0, 1200, 700, "").apply { visual = tableGreen }
    private val overlay = Label(0, 0, 1200, 700, "").apply { visual = overlayVisual }

    private val panel = Label(300, 150, 600, 400, "").apply {
        visual = ColorVisual(245, 245, 245)
    }

    private val title = Label(300, 180, 600, 60, "GAME OVER").apply {
        font = Font(size = 42, fontWeight = Font.FontWeight.BOLD)
    }

    private val rank1 = Label(300, 250, 600, 40, "").apply { font = Font(size = 24, fontWeight = Font.FontWeight.BOLD) }
    private val rank2 = Label(300, 300, 600, 40, "").apply { font = Font(size = 22) }
    private val rank3 = Label(300, 350, 600, 40, "").apply { font = Font(size = 22) }
    private val rank4 = Label(300, 400, 600, 40, "").apply { font = Font(size = 22) }

    private val backBtn = Button(500, 480, 200, 50, "Back to Menu").apply {
        visual = ColorVisual(80, 80, 200)
        font = Font(size = 18, color = Color(255, 255, 255), fontWeight = Font.FontWeight.BOLD)
    }

    init {
        rootService.addRefreshable(this)
        addComponents(bg, overlay, panel, title, rank1, rank2, rank3, rank4, backBtn)

        backBtn.onMouseClicked = click@{
            (application as SopraApplication).showMainMenu()
            return@click
        }
    }

    override fun refreshAfterGameEnd(ranking: List<Player>) {
        val labels = listOf(rank1, rank2, rank3, rank4)
        labels.forEach { it.text = "" }
        
        var lastScore: entity.ScoreTable? = null
        var lastRank = 1
        
        ranking.withIndex().forEach { (i, p) ->
            if (i < labels.size) {
                if (p.score != lastScore) {
                    lastRank = i + 1
                    lastScore = p.score
                }
                
                val pos = when(lastRank) {
                    1 -> "1st Place"
                    2 -> "2nd Place"
                    3 -> "3rd Place"
                    else -> "${lastRank}th Place"
                }
                labels[i].text = "$pos: ${p.name.uppercase()} (${p.score})"
            }
        }
        (application as SopraApplication).showGameFinishedScene()
    }
}