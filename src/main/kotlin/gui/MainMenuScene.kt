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
 * Main menu scene for setting up players and rounds.
 */
class MainMenuScene(
    private val rootService: RootService,
    private val application: BoardGameApplication
) : BoardGameScene(900, 600), Refreshable {

    private val buttonGreen: ColorVisual = ColorVisual(0, 200, 0)
    private val buttonRed: ColorVisual = ColorVisual(200, 0, 0)
    private val panelVisual: ColorVisual = ColorVisual(240, 240, 240)

    private val bg: Label = Label(0, 0, 900, 600, "")
    private val title: Label = Label(150, 40, 600, 100, "PUSH POKER")
    private val panel: Label = Label(250, 160, 400, 280, "")

    private val nameLabel: Label = Label(280, 180, 80, 30, "Name:")
    private val nameField: TextField = TextField(370, 180, 140, 30, "")
    private val addBtn: Button = Button(520, 180, 100, 30, "ADD PLAYER")

    // Vertical list via individual TextFields for editability (Storyboard style)
    private val p1: TextField = TextField(280, 230, 340, 30, "")
    private val p2: TextField = TextField(280, 260, 340, 30, "")
    private val p3: TextField = TextField(280, 290, 340, 30, "")
    private val p4: TextField = TextField(280, 320, 340, 30, "")
    
    private val playerListBG: Label = Label(280, 220, 340, 140, "")

    private val roundsLabel: Label = Label(320, 380, 80, 30, "Rounds:")
    private val minusBtn: Button = Button(410, 380, 30, 30, "-")
    private val roundsField: TextField = TextField(450, 380, 40, 30, "2")
    private val plusBtn: Button = Button(500, 380, 30, 30, "+")

    private val roundsSubtitle: Label = Label(410, 410, 120, 20, "(2-7 rounds)")
    private val errorLabel: Label = Label(250, 460, 400, 30, "")

    private val startBtn: Button = Button(250, 510, 180, 60, "START")
    private val quitBtn: Button = Button(470, 510, 180, 60, "QUIT")

    init {
        // Visual initialization
        try {
            val bgImage: ImageVisual = ImageVisual("menu_bg.jpg")
            bg.visual = bgImage
        } catch (e: Exception) {
            val greenColor: ColorVisual = ColorVisual(20, 80, 20)
            bg.visual = greenColor
        }

        val titleFont: Font = Font(size = 72, color = Color(255, 215, 0), fontWeight = Font.FontWeight.BOLD)
        title.font = titleFont

        panel.visual = panelVisual
        
        val btnAddVisual: ColorVisual = ColorVisual(100, 180, 255)
        addBtn.visual = btnAddVisual
        
        val pFont: Font = Font(size = 18)
        p1.font = pFont
        p1.prompt = "Player 1 Name"
        p2.font = pFont
        p2.prompt = "Player 2 Name"
        p3.font = pFont
        p3.prompt = "Player 3 Name"
        p4.font = pFont
        p4.prompt = "Player 4 Name"
        
        val listBGVisual: ColorVisual = ColorVisual(255, 255, 255)
        playerListBG.visual = listBGVisual
        
        val whiteVisual: ColorVisual = ColorVisual(255, 255, 255)
        minusBtn.visual = whiteVisual
        plusBtn.visual = whiteVisual
        
        roundsSubtitle.font = Font(size = 12)
        
        val errorFont: Font = Font(size = 14, color = Color(200, 0, 0), fontWeight = Font.FontWeight.BOLD)
        errorLabel.font = errorFont
        
        val boldFont: Font = Font(fontWeight = Font.FontWeight.BOLD)
        startBtn.visual = buttonGreen
        startBtn.font = boldFont
        
        quitBtn.visual = buttonRed
        quitBtn.font = boldFont

        rootService.addRefreshable(this)
        addComponents(
            bg, title, panel,
            nameLabel, nameField, addBtn,
            playerListBG, p1, p2, p3, p4,
            roundsLabel, minusBtn, roundsField, plusBtn, roundsSubtitle,
            errorLabel,
            startBtn, quitBtn
        )

        // Events
        addBtn.onMouseClicked = {
            errorLabel.text = ""
            val nameText: String = nameField.text.trim()
            if (nameText.length == 0) {
                errorLabel.text = "Error: name cannot be blank."
            } else {
                val fields: List<TextField> = listOf(p1, p2, p3, p4)
                
                var alreadyExists: Boolean = false
                for (i in 0 until fields.size) {
                    if (fields[i].text.trim() == nameText) {
                        alreadyExists = true
                    }
                }
                
                if (alreadyExists) {
                    errorLabel.text = "Error: name already taken."
                } else {
                    var foundSpot: Boolean = false
                    for (i in 0 until fields.size) {
                        if (fields[i].text.trim().length == 0) {
                            fields[i].text = nameText
                            nameField.text = ""
                            foundSpot = true
                            break
                        }
                    }
                    
                    if (foundSpot == false) {
                        errorLabel.text = "Error: maximum 4 players allowed."
                    }
                }
            }
        }

        minusBtn.onMouseClicked = {
            val roundsText: String = roundsField.text
            val currentRounds: Int? = roundsText.toIntOrNull()
            if (currentRounds != null) {
                if (currentRounds > 2) {
                    val newVal: Int = currentRounds - 1
                    roundsField.text = newVal.toString()
                }
            } else {
                roundsField.text = "2"
            }
        }

        plusBtn.onMouseClicked = {
            val roundsText: String = roundsField.text
            val currentRounds: Int? = roundsText.toIntOrNull()
            if (currentRounds != null) {
                if (currentRounds < 7) {
                    val newVal: Int = currentRounds + 1
                    roundsField.text = newVal.toString()
                }
            } else {
                roundsField.text = "2"
            }
        }

        startBtn.onMouseClicked = {
            errorLabel.text = ""
            val roundsText: String = roundsField.text
            val rounds: Int? = roundsText.toIntOrNull()
            
            val fieldsList: List<TextField> = listOf(p1, p2, p3, p4)
            val names: MutableList<String> = mutableListOf<String>()

            for (i in 0 until fieldsList.size) {
                val field: TextField = fieldsList[i]
                val trimmedName: String = field.text.trim()
                if (trimmedName.length > 0) {
                    names.add(trimmedName)
                }
            }

            if (names.size < 2) {
                errorLabel.text = "Error: at least 2 players required."
            } else if (rounds == null) {
                errorLabel.text = "Error: rounds must be between 2 and 7."
            } else if (rounds < 2 || rounds > 7) {
                errorLabel.text = "Error: rounds must be between 2 and 7."
            } else {
                rootService.gameService.startNewGame(names, rounds)
            }
        }

        quitBtn.onMouseClicked = { application.exit() }
    }

    override fun refreshAfterStartNewGame() {
        val app: SopraApplication = application as SopraApplication
        app.showNextPlayerScene()
    }
}