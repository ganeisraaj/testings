package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual

/**
 * Main menu scene for Push Poker.
 * Handles player setup and game initialization.
 */
class MainMenuScene(
    private val rootService: RootService,
    private val application: BoardGameApplication
) : BoardGameScene(900, 600), Refreshable {

    private val buttonGreen = ColorVisual(0, 200, 0)
    private val buttonRed = ColorVisual(200, 0, 0)
    private val panelVisual = ColorVisual(240, 240, 240)

    private val bg = Label(0, 0, 900, 600, "").apply {
        visual = try {
            ImageVisual("menu_bg.jpg")
        } catch (_: Exception) {
            ColorVisual(20, 80, 20) // Deep poker green
        }
    }

    private val title = Label(150, 40, 600, 100, "PUSH POKER").apply {
        font = Font(size = 72, color = Color(255, 215, 0), fontWeight = Font.FontWeight.BOLD) // Gold color
    }

    private val panel = Label(250, 160, 400, 280, "").apply {
        visual = panelVisual
    }

    private val nameLabel = Label(280, 180, 80, 30, "Name:")
    private val nameField = TextField(370, 180, 140, 30)
    private val addBtn = Button(520, 180, 100, 30, "ADD PLAYER").apply {
        visual = ColorVisual(100, 180, 255)
    }

    // Vertical list via individual TextFields for editability (Storyboard style)
    private val p1 = TextField(280, 230, 340, 30).apply { font = Font(size = 18); prompt = "Player 1 Name" }
    private val p2 = TextField(280, 260, 340, 30).apply { font = Font(size = 18); prompt = "Player 2 Name" }
    private val p3 = TextField(280, 290, 340, 30).apply { font = Font(size = 18); prompt = "Player 3 Name" }
    private val p4 = TextField(280, 320, 340, 30).apply { font = Font(size = 18); prompt = "Player 4 Name" }
    
    private val playerListBG = Label(280, 220, 340, 140, "").apply {
        visual = ColorVisual(255, 255, 255)
    }

    private val roundsLabel = Label(320, 380, 80, 30, "Rounds:")
    private val minusBtn = Button(410, 380, 30, 30, "-").apply { visual = ColorVisual(255, 255, 255) }
    private val roundsField = TextField(450, 380, 40, 30, "2")
    private val plusBtn = Button(500, 380, 30, 30, "+").apply { visual = ColorVisual(255, 255, 255) }

    private val roundsSubtitle = Label(410, 410, 120, 20, "(2-7 rounds)").apply {
        font = Font(size = 12)
    }

    private val errorLabel = Label(250, 460, 400, 30, "").apply {
        font = Font(size = 14, color = Color(200, 0, 0), fontWeight = Font.FontWeight.BOLD)
    }

    private val startBtn = Button(250, 510, 180, 60, "START").apply {
        visual = buttonGreen
        font = Font(fontWeight = Font.FontWeight.BOLD)
    }

    private val quitBtn = Button(470, 510, 180, 60, "QUIT").apply {
        visual = buttonRed
        font = Font(fontWeight = Font.FontWeight.BOLD)
    }

    init {
        rootService.addRefreshable(this)
        addComponents(
            bg, title, panel,
            nameLabel, nameField, addBtn,
            playerListBG, p1, p2, p3, p4,
            roundsLabel, minusBtn, roundsField, plusBtn, roundsSubtitle,
            errorLabel,
            startBtn, quitBtn
        )

        addBtn.onMouseClicked = click@{
            errorLabel.text = ""
            val name = nameField.text.trim()
            if (name.isBlank()) {
                errorLabel.text = "Error: name cannot be blank."
                return@click
            }
            
            val fields = listOf(p1, p2, p3, p4)
            val firstEmpty = fields.find { it.text.isBlank() }
            
            if (fields.any { it.text.trim() == name }) {
                errorLabel.text = "Error: name already taken."
                return@click
            }
            
            if (firstEmpty == null) {
                errorLabel.text = "Error: maximum 4 players allowed."
                return@click
            }
            
            firstEmpty.text = name
            nameField.text = ""
        }

        // Backspace functionality is native to TextField

        minusBtn.onMouseClicked = {
            val cur = roundsField.text.toIntOrNull() ?: 2
            if (cur > 2) roundsField.text = (cur - 1).toString()
        }

        plusBtn.onMouseClicked = {
            val cur = roundsField.text.toIntOrNull() ?: 2
            if (cur < 7) roundsField.text = (cur + 1).toString()
        }

        startBtn.onMouseClicked = click@{
            errorLabel.text = ""
            val rounds = roundsField.text.toIntOrNull()
            
            val names = listOf(p1, p2, p3, p4).map { it.text.trim() }.filter { it.isNotEmpty() }.toMutableList()

            if (names.size < 2) {
                errorLabel.text = "Error: at least 2 players required."
                return@click
            }
            if (rounds == null || rounds !in 2..7) {
                errorLabel.text = "Error: rounds must be between 2 and 7."
                return@click
            }
            rootService.gameService.startNewGame(names, rounds)
        }

        quitBtn.onMouseClicked = { application.exit() }
    }

    override fun refreshAfterStartNewGame() {
        (application as SopraApplication).showNextPlayerScene()
    }
}