package gui

import entity.Card
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * This is the main game screen where players take their turns.
 */
class GameScene(
    private val rootService: RootService
) : BoardGameScene(1200, 700), Refreshable {

    private val cardImages: CardImageLoader = CardImageLoader()
    private val tableGreen: ColorVisual = ColorVisual(46, 125, 50)

    private val bg: Label = Label(0, 0, 1200, 700, "")

    // Labels at the top to show round and actions
    private val roundField: Label = Label(20, 20, 150, 40, "ROUND: 1/1")
    private val actionsField: Label = Label(1030, 20, 150, 40, "Actions: 2/2")
    private val currentPlayerLabel: Label = Label(400, 190, 400, 50, "Current Player: -")

    // Log area labels
    private val logTitle: Label = Label(40, 480, 320, 30, "LOG")
    private val logBG: Label = Label(40, 510, 320, 140, "")
    private val log1: Label = Label(40, 515, 320, 25, "")
    private val log2: Label = Label(40, 540, 320, 25, "")
    private val log3: Label = Label(40, 565, 320, 25, "")
    private val log4: Label = Label(40, 590, 320, 25, "")
    private val log5: Label = Label(40, 615, 320, 25, "")

    private val logLines: MutableList<String> = mutableListOf<String>()

    // Cards for the bottom player (the current player)
    private val bH1: CardView = createCard(420, 560)
    private val bH2: CardView = createCard(500, 560)
    private val bO1: CardView = createCard(600, 560)
    private val bO2: CardView = createCard(680, 560)
    private val bO3: CardView = createCard(760, 560)
    private val bName: Label = createPlayerLabel(630, 530, 0.0)

    // Cards for the left player
    private val lH1: CardView = createCard(70, 70, 90.0)
    private val lH2: CardView = createCard(70, 160, 90.0)
    private val lO1: CardView = createCard(70, 250, 90.0)
    private val lO2: CardView = createCard(70, 340, 90.0)
    private val lO3: CardView = createCard(70, 430, 90.0)
    private val lName: Label = createPlayerLabel(185, 250, 90.0)

    // Cards for the top player
    private val tO1: CardView = createCard(420, 30, 180.0)
    private val tO2: CardView = createCard(500, 30, 180.0)
    private val tO3: CardView = createCard(580, 30, 180.0)
    private val tH1: CardView = createCard(680, 30, 180.0)
    private val tH2: CardView = createCard(760, 30, 180.0)
    private val tName: Label = createPlayerLabel(630, 155, 0.0)

    // Cards for the right player
    private val rO1: CardView = createCard(1055, 35, 270.0)
    private val rO2: CardView = createCard(1055, 125, 270.0)
    private val rO3: CardView = createCard(1055, 215, 270.0)
    private val rH1: CardView = createCard(1055, 305, 270.0)
    private val rH2: CardView = createCard(1055, 395, 270.0)
    private val rName: Label = createPlayerLabel(940, 240, 270.0)

    // Center cards in the middle of the table
    private val center0: CardView = createCard(485, 310)
    private val center1: CardView = createCard(565, 310)
    private val center2: CardView = createCard(645, 310)

    // The draw stack shown on the left side
    private val drawStackView: CardView = CardView(
        posX = 350,
        posY = 310,
        width = 75,
        height = 110,
        front = cardImages.backImage,
        back = cardImages.backImage
    )

    // The discard pile shown on the right side
    private val discardView: CardView = createCard(790, 310)

    // Labels below the draw and discard piles
    private val drawLabel: Label = Label(340, 425, 95, 24, "DRAW")
    private val discardLabel: Label = Label(775, 425, 105, 24, "DISCARD")

    // Arrow buttons to push cards from draw stack into center
    private val leftArrow: Button = Button(435, 345, 40, 40, "<")
    private val rightArrow: Button = Button(735, 345, 40, 40, ">")

    // Action buttons on the right side
    private val switchActionBtn: Button = Button(1000, 500, 160, 40, "Switch")
    private val switchAllBtn: Button = Button(1000, 550, 160, 40, "Switch All")
    private val endTurnBtn: Button = Button(1000, 620, 160, 55, "End Turn")

    // Tracks which open card and center card the player has selected
    private var selectedOpenIdx: Int? = null
    private var selectedCenterIdx: Int? = null

    init {
        rootService.addRefreshable(this)

        // Set background color to green
        bg.visual = tableGreen

        // Style the top info bars
        val barVisual: ColorVisual = ColorVisual(180, 220, 180)
        roundField.visual = barVisual
        roundField.font = Font(fontWeight = Font.FontWeight.BOLD)

        actionsField.visual = barVisual
        actionsField.font = Font(fontWeight = Font.FontWeight.BOLD)

        // Style the current player label
        val playerLabelFont: Font = Font(size = 24, fontWeight = Font.FontWeight.BOLD, color = Color(255, 255, 255))
        currentPlayerLabel.font = playerLabelFont
        currentPlayerLabel.visual = ColorVisual(0, 0, 0, 80)

        // Style the log section
        logTitle.visual = ColorVisual(120, 120, 120)
        logBG.visual = ColorVisual(150, 150, 150, 180)

        val logFont: Font = Font(size = 12)
        val logLineVisual: ColorVisual = ColorVisual(255, 255, 255, 100)

        log1.visual = logLineVisual
        log1.font = logFont

        log2.visual = logLineVisual
        log2.font = logFont

        log3.visual = logLineVisual
        log3.font = logFont

        log4.visual = logLineVisual
        log4.font = logFont

        log5.visual = logLineVisual
        log5.font = logFont

        // Style the stack labels
        val stackLabelVisual: ColorVisual = ColorVisual(0, 0, 0, 120)
        val stackLabelFont: Font = Font(size = 14, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE)

        drawLabel.visual = stackLabelVisual
        drawLabel.font = stackLabelFont

        discardLabel.visual = stackLabelVisual
        discardLabel.font = stackLabelFont

        // Style the arrow buttons
        val arrowVisual: ColorVisual = ColorVisual(255, 150, 0)
        val arrowFont: Font = Font(size = 24, fontWeight = Font.FontWeight.BOLD)

        leftArrow.visual = arrowVisual
        leftArrow.font = arrowFont

        rightArrow.visual = arrowVisual
        rightArrow.font = arrowFont

        // Style the action buttons
        switchActionBtn.visual = arrowVisual
        switchAllBtn.visual = arrowVisual
        endTurnBtn.visual = arrowVisual
        endTurnBtn.font = Font(fontWeight = Font.FontWeight.BOLD)

        // Add all components to the scene
        addComponents(
            bg, roundField, actionsField, currentPlayerLabel,
            logTitle, logBG, log1, log2, log3, log4, log5,
            drawStackView, discardView, drawLabel, discardLabel,
            center0, center1, center2,
            leftArrow, rightArrow,
            switchActionBtn, switchAllBtn, endTurnBtn
        )

        addComponents(bH1, bH2, bO1, bO2, bO3, bName)
        addComponents(lH1, lH2, lO1, lO2, lO3, lName)
        addComponents(tO1, tO2, tO3, tH1, tH2, tName)
        addComponents(rO1, rO2, rO3, rH1, rH2, rName)

        // Click on first open card to select or deselect it
        bO1.onMouseClicked = {
            if (selectedOpenIdx == 0) {
                selectedOpenIdx = null
            } else {
                selectedOpenIdx = 0
            }
            updateUI()
        }

        // Click on second open card to select or deselect it
        bO2.onMouseClicked = {
            if (selectedOpenIdx == 1) {
                selectedOpenIdx = null
            } else {
                selectedOpenIdx = 1
            }
            updateUI()
        }

        // Click on third open card to select or deselect it
        bO3.onMouseClicked = {
            if (selectedOpenIdx == 2) {
                selectedOpenIdx = null
            } else {
                selectedOpenIdx = 2
            }
            updateUI()
        }

        // Click on first center card to select or deselect it
        center0.onMouseClicked = {
            if (selectedCenterIdx == 0) {
                selectedCenterIdx = null
            } else {
                selectedCenterIdx = 0
            }
            updateUI()
        }

        // Click on second center card to select or deselect it
        center1.onMouseClicked = {
            if (selectedCenterIdx == 1) {
                selectedCenterIdx = null
            } else {
                selectedCenterIdx = 1
            }
            updateUI()
        }

        // Click on third center card to select or deselect it
        center2.onMouseClicked = {
            if (selectedCenterIdx == 2) {
                selectedCenterIdx = null
            } else {
                selectedCenterIdx = 2
            }
            updateUI()
        }

        // Push a card into the left side of the center
        leftArrow.onMouseClicked = {
            triggerAction { rootService.playerActionService.pushLeft() }
        }

        // Push a card into the right side of the center
        rightArrow.onMouseClicked = {
            triggerAction { rootService.playerActionService.pushRight() }
        }

        // Swap all open cards with center cards
        switchAllBtn.onMouseClicked = {
            triggerAction { rootService.playerActionService.switchAll() }
        }

        // Swap one selected open card with one selected center card
        switchActionBtn.onMouseClicked = {
            if (selectedOpenIdx != null && selectedCenterIdx != null) {
                val openCardIndex: Int = selectedOpenIdx!!
                val centerCardIndex: Int = selectedCenterIdx!!
                triggerAction { rootService.playerActionService.switchOne(openCardIndex, centerCardIndex) }
                selectedOpenIdx = null
                selectedCenterIdx = null
            } else {
                refreshLog("Select 1 hand + 1 center card!")
            }
        }

        // End the current player's turn
        endTurnBtn.onMouseClicked = {
            triggerAction { rootService.gameService.endTurn() }
        }
    }

    /**
     * Runs a service call and shows any error in the log.
     */
    private fun triggerAction(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            val message: String = e.message ?: "Unknown error"
            refreshLog("Error: " + message)
        }
    }

    /**
     * Creates a card view at the given position with optional rotation.
     */
    private fun createCard(x: Int, y: Int, rot: Double = 0.0): CardView {
        val view: CardView = CardView(
            posX = x,
            posY = y,
            width = 75,
            height = 110,
            front = cardImages.blankImage,
            back = cardImages.backImage
        )
        view.rotation = rot
        return view
    }

    /**
     * Creates a name label for a player area.
     */
    private fun createPlayerLabel(cx: Int, cy: Int, rot: Double): Label {
        val labelPosX: Int = cx - 100
        val labelPosY: Int = cy - 15
        val label: Label = Label(posX = labelPosX, posY = labelPosY, width = 200, height = 30, "")
        val labelFont: Font = Font(size = 20, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE)
        label.font = labelFont
        label.visual = ColorVisual(0, 0, 0, 80)
        label.rotation = rot
        return label
    }

    override fun refreshAfterStartNewGame() {
        updateUI()
    }

    override fun refreshAfterTurnStart() {
        selectedOpenIdx = null
        selectedCenterIdx = null
        updateUI()
    }

    override fun refreshAfterTurnEnd() {
        updateUI()
    }

    override fun refreshAfterPushLeft(newCard: Card) {
        updateUI()
    }

    override fun refreshAfterPushRight(newCard: Card) {
        updateUI()
    }

    override fun refreshAfterSwitch() {
        updateUI()
    }

    override fun refreshAfterError(message: String) {
        refreshLog("Error: " + message)
    }

    override fun refreshLog(message: String) {
        logLines.add(message)

        // Keep only the last 5 messages
        if (logLines.size > 5) {
            logLines.removeAt(0)
        }

        // Update each log label manually
        if (logLines.size > 0) {
            log1.text = logLines[0]
        } else {
            log1.text = ""
        }

        if (logLines.size > 1) {
            log2.text = logLines[1]
        } else {
            log2.text = ""
        }

        if (logLines.size > 2) {
            log3.text = logLines[2]
        } else {
            log3.text = ""
        }

        if (logLines.size > 3) {
            log4.text = logLines[3]
        } else {
            log4.text = ""
        }

        if (logLines.size > 4) {
            log5.text = logLines[4]
        } else {
            log5.text = ""
        }
    }

    /**
     * Updates all visual components to match the current game state.
     */
    private fun updateUI() {
        val game: entity.Game? = rootService.currentGame
        if (game == null) {
            return
        }

        val player: entity.Player = game.players[game.currentPlayerIndex]

        // Update top bar texts using string concatenation
        roundField.text = "ROUND: " + game.currentRound + "/" + game.totalRounds
        actionsField.text = "Actions: " + player.actionsLeft + "/2"
        currentPlayerLabel.text = "Current Player: " + player.name

        // Update center card 0
        val centerCard0: entity.Card? = if (game.centerCards.size > 0) game.centerCards[0] else null
        setCard(center0, centerCard0)
        if (selectedCenterIdx == 0) {
            center0.posY = 290.0
        } else {
            center0.posY = 310.0
        }

        // Update center card 1
        val centerCard1: entity.Card? = if (game.centerCards.size > 1) game.centerCards[1] else null
        setCard(center1, centerCard1)
        if (selectedCenterIdx == 1) {
            center1.posY = 290.0
        } else {
            center1.posY = 310.0
        }

        // Update center card 2
        val centerCard2: entity.Card? = if (game.centerCards.size > 2) game.centerCards[2] else null
        setCard(center2, centerCard2)
        if (selectedCenterIdx == 2) {
            center2.posY = 290.0
        } else {
            center2.posY = 310.0
        }

        // Update the discard pile display
        if (game.discardStack.isNotEmpty()) {
            val topDiscard: entity.Card = game.discardStack.peek()
            setCard(discardView, topDiscard)
        } else {
            setCard(discardView, null)
        }

        // Hide all player card views and name labels before redrawing
        bH1.isVisible = false
        bH2.isVisible = false
        bO1.isVisible = false
        bO2.isVisible = false
        bO3.isVisible = false
        bName.isVisible = false

        lH1.isVisible = false
        lH2.isVisible = false
        lO1.isVisible = false
        lO2.isVisible = false
        lO3.isVisible = false
        lName.isVisible = false

        tO1.isVisible = false
        tO2.isVisible = false
        tO3.isVisible = false
        tH1.isVisible = false
        tH2.isVisible = false
        tName.isVisible = false

        rO1.isVisible = false
        rO2.isVisible = false
        rO3.isVisible = false
        rH1.isVisible = false
        rH2.isVisible = false
        rName.isVisible = false

        // Loop through every player and place them in the correct screen area
        for (i in 0 until game.players.size) {
            val p: entity.Player = game.players[i]

            // Calculate the relative position of this player to the current player
            val relativeIndex: Int = (i - game.currentPlayerIndex + game.players.size) % game.players.size

            // Decide which screen area to place this player in
            var targetArea: Int = 0
            if (game.players.size == 2) {
                if (relativeIndex == 0) {
                    targetArea = 0
                } else {
                    targetArea = 2
                }
            } else if (game.players.size == 3) {
                if (relativeIndex == 0) {
                    targetArea = 0
                } else if (relativeIndex == 1) {
                    targetArea = 1
                } else {
                    targetArea = 3
                }
            } else {
                if (relativeIndex == 0) {
                    targetArea = 0
                } else if (relativeIndex == 1) {
                    targetArea = 1
                } else if (relativeIndex == 2) {
                    targetArea = 2
                } else {
                    targetArea = 3
                }
            }

            // Pick the correct card views for this area
            val h1: CardView
            val h2: CardView
            val o1: CardView
            val o2: CardView
            val o3: CardView
            val nameLabel: Label

            if (targetArea == 0) {
                h1 = bH1
                h2 = bH2
                o1 = bO1
                o2 = bO2
                o3 = bO3
                nameLabel = bName
            } else if (targetArea == 1) {
                h1 = lH1
                h2 = lH2
                o1 = lO1
                o2 = lO2
                o3 = lO3
                nameLabel = lName
            } else if (targetArea == 2) {
                h1 = tH1
                h2 = tH2
                o1 = tO1
                o2 = tO2
                o3 = tO3
                nameLabel = tName
            } else {
                h1 = rH1
                h2 = rH2
                o1 = rO1
                o2 = rO2
                o3 = rO3
                nameLabel = rName
            }

            // Make all views for this player visible
            h1.isVisible = true
            h2.isVisible = true
            o1.isVisible = true
            o2.isVisible = true
            o3.isVisible = true
            nameLabel.isVisible = true
            nameLabel.text = p.name

            // Bottom area is the current player - show their hidden cards too
            if (targetArea == 0) {
                val currentPlayer: entity.Player = game.players[game.currentPlayerIndex]

                // Show hidden card 1
                if (currentPlayer.hiddenCards.size > 0) {
                    setCard(h1, currentPlayer.hiddenCards[0])
                }
                // Show hidden card 2
                if (currentPlayer.hiddenCards.size > 1) {
                    setCard(h2, currentPlayer.hiddenCards[1])
                }

                // Show open card 1 with selection highlight
                val openCard0: entity.Card? = if (currentPlayer.openCards.size > 0) currentPlayer.openCards[0] else null
                setCard(o1, openCard0)
                if (selectedOpenIdx == 0) {
                    o1.posY = 540.0
                } else {
                    o1.posY = 560.0
                }

                // Show open card 2 with selection highlight
                val openCard1: entity.Card? = if (currentPlayer.openCards.size > 1) currentPlayer.openCards[1] else null
                setCard(o2, openCard1)
                if (selectedOpenIdx == 1) {
                    o2.posY = 540.0
                } else {
                    o2.posY = 560.0
                }

                // Show open card 3 with selection highlight
                val openCard2: entity.Card? = if (currentPlayer.openCards.size > 2) currentPlayer.openCards[2] else null
                setCard(o3, openCard2)
                if (selectedOpenIdx == 2) {
                    o3.posY = 540.0
                } else {
                    o3.posY = 560.0
                }

                // Hidden cards always stay at the same Y position
                bH1.posY = 560.0
                bH2.posY = 560.0

            } else {
                // For other players just show the back of hidden cards
                h1.showBack()
                h2.showBack()

                // Show their open cards face up
                if (p.openCards.size > 0) {
                    setCard(o1, p.openCards[0])
                } else {
                    setCard(o1, null)
                }

                if (p.openCards.size > 1) {
                    setCard(o2, p.openCards[1])
                } else {
                    setCard(o2, null)
                }

                if (p.openCards.size > 2) {
                    setCard(o3, p.openCards[2])
                } else {
                    setCard(o3, null)
                }
            }
        }
    }

    /**
     * Sets the image on a card view, or shows a blank if no card given.
     */
    private fun setCard(view: CardView, card: Card?) {
        if (card == null) {
            view.frontVisual = cardImages.blankImage
        } else {
            view.frontVisual = cardImages.frontImageFor(card.suit, card.value)
        }
        view.showFront()
    }

}