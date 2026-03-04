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
 * This is the main menu where players enter their names and pick how many rounds to play.
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

    // One text field for each player slot
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
        // Try to load a background image, use plain green if it fails
        try {
            val bgImage: ImageVisual = ImageVisual("menu_bg.jpg")
            bg.visual = bgImage
        } catch (e: Exception) {
            val greenColor: ColorVisual = ColorVisual(20, 80, 20)
            bg.visual = greenColor
        }

        // Style the title label
        val titleFont: Font = Font(size = 72, color = Color(255, 215, 0), fontWeight = Font.FontWeight.BOLD)
        title.font = titleFont

        // Style the panel behind the input fields
        panel.visual = panelVisual

        // Style the add player button
        val btnAddVisual: ColorVisual = ColorVisual(100, 180, 255)
        addBtn.visual = btnAddVisual

        // Style all four player name input fields
        val pFont: Font = Font(size = 18)

        p1.font = pFont
        p1.prompt = "Player 1 Name"

        p2.font = pFont
        p2.prompt = "Player 2 Name"

        p3.font = pFont
        p3.prompt = "Player 3 Name"

        p4.font = pFont
        p4.prompt = "Player 4 Name"

        // White background behind the player list
        val listBGVisual: ColorVisual = ColorVisual(255, 255, 255)
        playerListBG.visual = listBGVisual

        // Style the plus and minus buttons
        val whiteVisual: ColorVisual = ColorVisual(255, 255, 255)
        minusBtn.visual = whiteVisual
        plusBtn.visual = whiteVisual

        // Small font for the rounds hint text
        roundsSubtitle.font = Font(size = 12)

        // Style the error label in red
        val errorFont: Font = Font(size = 14, color = Color(200, 0, 0), fontWeight = Font.FontWeight.BOLD)
        errorLabel.font = errorFont

        // Style the start and quit buttons
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

        // When the add player button is clicked
        addBtn.onMouseClicked = {
            errorLabel.text = ""
            val nameText: String = nameField.text.trim()

            if (nameText.length == 0) {
                errorLabel.text = "Error: name cannot be blank."
            } else {
                // Check if the name is already taken in one of the four slots
                var alreadyExists: Boolean = false

                val p1Text: String = p1.text.trim()
                if (p1Text == nameText) {
                    alreadyExists = true
                }

                val p2Text: String = p2.text.trim()
                if (p2Text == nameText) {
                    alreadyExists = true
                }

                val p3Text: String = p3.text.trim()
                if (p3Text == nameText) {
                    alreadyExists = true
                }

                val p4Text: String = p4.text.trim()
                if (p4Text == nameText) {
                    alreadyExists = true
                }

                if (alreadyExists == true) {
                    errorLabel.text = "Error: name already taken."
                } else {
                    // Try to find an empty slot and fill it
                    var foundSpot: Boolean = false

                    if (foundSpot == false) {
                        val slot1Text: String = p1.text.trim()
                        if (slot1Text.length == 0) {
                            p1.text = nameText
                            nameField.text = ""
                            foundSpot = true
                        }
                    }

                    if (foundSpot == false) {
                        val slot2Text: String = p2.text.trim()
                        if (slot2Text.length == 0) {
                            p2.text = nameText
                            nameField.text = ""
                            foundSpot = true
                        }
                    }

                    if (foundSpot == false) {
                        val slot3Text: String = p3.text.trim()
                        if (slot3Text.length == 0) {
                            p3.text = nameText
                            nameField.text = ""
                            foundSpot = true
                        }
                    }

                    if (foundSpot == false) {
                        val slot4Text: String = p4.text.trim()
                        if (slot4Text.length == 0) {
                            p4.text = nameText
                            nameField.text = ""
                            foundSpot = true
                        }
                    }

                    if (foundSpot == false) {
                        errorLabel.text = "Error: maximum 4 players allowed."
                    }
                }
            }
        }

        // When the minus button is clicked, decrease the round count
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

        // When the plus button is clicked, increase the round count
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

        // When the start button is clicked, collect all names and start the game
        startBtn.onMouseClicked = {
            errorLabel.text = ""

            val roundsText: String = roundsField.text
            val rounds: Int? = roundsText.toIntOrNull()

            // Collect player names manually from each field
            val names: MutableList<String> = mutableListOf<String>()

            val name1: String = p1.text.trim()
            if (name1.length > 0) {
                names.add(name1)
            }

            val name2: String = p2.text.trim()
            if (name2.length > 0) {
                names.add(name2)
            }

            val name3: String = p3.text.trim()
            if (name3.length > 0) {
                names.add(name3)
            }

            val name4: String = p4.text.trim()
            if (name4.length > 0) {
                names.add(name4)
            }

            // Validate before starting
            if (names.size < 2) {
                errorLabel.text = "Error: at least 2 players required."
            } else if (rounds == null) {
                errorLabel.text = "Error: rounds must be between 2 and 7."
            } else if (rounds < 2) {
                errorLabel.text = "Error: rounds must be between 2 and 7."
            } else if (rounds > 7) {
                errorLabel.text = "Error: rounds must be between 2 and 7."
            } else {
                rootService.gameService.startNewGame(names, rounds)
            }
        }

        // Quit the application when quit is clicked
        quitBtn.onMouseClicked = {
            application.exit()
        }
    }

    override fun refreshAfterStartNewGame() {
        val app: SopraApplication = application as SopraApplication
        app.showNextPlayerScene()
    }

}