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
 * Main game scene where the poker action happens.
 */
class GameScene(
    private val rootService: RootService
) : BoardGameScene(1200, 700), Refreshable {

    private val cardImages: CardImageLoader = CardImageLoader()
    private val tableGreen: ColorVisual = ColorVisual(46, 125, 50)
    
    private val bg: Label = Label(0, 0, 1200, 700, "")

    // Top information bars
    private val roundField: Label = Label(20, 20, 150, 40, "ROUND: 1/1")
    private val actionsField: Label = Label(1030, 20, 150, 40, "Actions: 2/2")
    private val currentPlayerLabel: Label = Label(400, 190, 400, 50, "Current Player: -")

    // Log display labels
    private val logTitle: Label = Label(40, 480, 320, 30, "LOG")
    private val logBG: Label = Label(40, 510, 320, 140, "")
    private val log1: Label = Label(40, 515, 320, 25, "")
    private val log2: Label = Label(40, 540, 320, 25, "")
    private val log3: Label = Label(40, 565, 320, 25, "")
    private val log4: Label = Label(40, 590, 320, 25, "")
    private val log5: Label = Label(40, 615, 320, 25, "")
    
    private val logLines: MutableList<String> = mutableListOf<String>()

    // Card views for the bottom player (Player 0 / You)
    private val bH1: CardView = createCard(420, 560)
    private val bH2: CardView = createCard(500, 560)
    private val bO1: CardView = createCard(600, 560)
    private val bO2: CardView = createCard(680, 560)
    private val bO3: CardView = createCard(760, 560)
    private val bName: Label = createPlayerLabel(630, 530, 0.0)
    
    // Card views for the left side player
    private val lH1: CardView = createCard(70, 70, 90.0)
    private val lH2: CardView = createCard(70, 160, 90.0)
    private val lO1: CardView = createCard(70, 250, 90.0)
    private val lO2: CardView = createCard(70, 340, 90.0)
    private val lO3: CardView = createCard(70, 430, 90.0)
    private val lName: Label = createPlayerLabel(185, 250, 90.0)
    
    // Card views for the top player
    private val tO1: CardView = createCard(420, 30, 180.0)
    private val tO2: CardView = createCard(500, 30, 180.0)
    private val tO3: CardView = createCard(580, 30, 180.0)
    private val tH1: CardView = createCard(680, 30, 180.0)
    private val tH2: CardView = createCard(760, 30, 180.0)
    private val tName: Label = createPlayerLabel(630, 155, 0.0) 
    
    // Card views for the right side player
    private val rO1: CardView = createCard(1055, 70, 270.0)
    private val rO2: CardView = createCard(1055, 160, 270.0)
    private val rO3: CardView = createCard(1055, 250, 270.0)
    private val rH1: CardView = createCard(1055, 340, 270.0)
    private val rH2: CardView = createCard(1055, 430, 270.0)
    private val rName: Label = createPlayerLabel(940, 250, 270.0)

    private val center0: CardView = createCard(485, 310)
    private val center1: CardView = createCard(565, 310)
    private val center2: CardView = createCard(645, 310)
    
    private val drawStackView: CardView = CardView(posX = 350, posY = 310, width = 75, height = 110, front = cardImages.backImage, back = cardImages.backImage)
    private val discardView: CardView = createCard(790, 310)
    
    private val drawStackLabel: Label = Label(350, 310, 75, 110, "DRAW STACK")
    private val discardLabel: Label = Label(790, 310, 75, 110, "DISCARD STACK")

    // Buttons for interacting with the deck stacks
    private val leftArrow: Button = Button(435, 345, 40, 40, "<")
    private val rightArrow: Button = Button(735, 345, 40, 40, ">")

    // Management buttons for turn and card switching
    private val switchActionBtn: Button = Button(1000, 480, 160, 40, "Switch")
    private val switchAllBtn: Button = Button(1000, 530, 160, 40, "Switch All")
    private val endTurnBtn: Button = Button(1000, 630, 160, 50, "End Turn")

    private var selectedOpenIdx: Int? = null
    private var selectedCenterIdx: Int? = null

    init {
        rootService.addRefreshable(this)
        
        // Background and style setup
        bg.visual = tableGreen
        
        val barVisual: ColorVisual = ColorVisual(180, 220, 180)
        roundField.visual = barVisual
        roundField.font = Font(fontWeight = Font.FontWeight.BOLD)
        actionsField.visual = barVisual
        actionsField.font = Font(fontWeight = Font.FontWeight.BOLD)
        
        currentPlayerLabel.font = Font(size = 24, fontWeight = Font.FontWeight.BOLD, color = Color(255, 255, 255))
        currentPlayerLabel.visual = ColorVisual(0, 0, 0, 80)
        
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
        
        val stackFont: Font = Font(size = 12, color = Color.WHITE, fontWeight = Font.FontWeight.BOLD)
        drawStackLabel.font = stackFont
        discardLabel.font = Font(size = 10, color = Color.BLACK, fontWeight = Font.FontWeight.BOLD)
        
        val arrowVisual: ColorVisual = ColorVisual(255, 150, 0)
        val arrowFont: Font = Font(size = 24, fontWeight = Font.FontWeight.BOLD)
        leftArrow.visual = arrowVisual
        leftArrow.font = arrowFont
        rightArrow.visual = arrowVisual
        rightArrow.font = arrowFont
        
        switchActionBtn.visual = arrowVisual
        switchAllBtn.visual = arrowVisual
        endTurnBtn.visual = arrowVisual
        endTurnBtn.font = Font(fontWeight = Font.FontWeight.BOLD)

        addComponents(
            bg, roundField, actionsField, currentPlayerLabel,
            logTitle, logBG, log1, log2, log3, log4, log5,
            drawStackView, drawStackLabel, discardView, discardLabel,
            center0, center1, center2,
            leftArrow, rightArrow,
            switchActionBtn, switchAllBtn, endTurnBtn
        )
        
        addComponents(bH1, bH2, bO1, bO2, bO3, bName)
        addComponents(lH1, lH2, lO1, lO2, lO3, lName)
        addComponents(tO1, tO2, tO3, tH1, tH2, tName)
        addComponents(rO1, rO2, rO3, rH1, rH2, rName)

        // Register mouse clicks for selecting cards from the player hand
        bO1.onMouseClicked = { selectedOpenIdx = if (selectedOpenIdx == 0) null else 0; updateUI() }
        bO2.onMouseClicked = { selectedOpenIdx = if (selectedOpenIdx == 1) null else 1; updateUI() }
        bO3.onMouseClicked = { selectedOpenIdx = if (selectedOpenIdx == 2) null else 2; updateUI() }

        // Register mouse clicks for selecting center cards
        center0.onMouseClicked = { selectedCenterIdx = if (selectedCenterIdx == 0) null else 0; updateUI() }
        center1.onMouseClicked = { selectedCenterIdx = if (selectedCenterIdx == 1) null else 1; updateUI() }
        center2.onMouseClicked = { selectedCenterIdx = if (selectedCenterIdx == 2) null else 2; updateUI() }

        // Action buttons
        leftArrow.onMouseClicked = { triggerAction { rootService.playerActionService.pushLeft() } }
        rightArrow.onMouseClicked = { triggerAction { rootService.playerActionService.pushRight() } }
        switchAllBtn.onMouseClicked = { triggerAction { rootService.playerActionService.switchAll() } }
        
        switchActionBtn.onMouseClicked = {
            if (selectedOpenIdx != null && selectedCenterIdx != null) {
                val openIdx: Int = selectedOpenIdx!!
                val centerIdx: Int = selectedCenterIdx!!
                triggerAction { rootService.playerActionService.switchOne(openIdx, centerIdx) }
                selectedOpenIdx = null
                selectedCenterIdx = null
            } else {
                refreshLog("Select 1 hand + 1 center card!")
            }
        }
        
        endTurnBtn.onMouseClicked = {
            triggerAction { rootService.gameService.endTurn() }
        }
    }

    /**
     * Helper to run service actions and catch errors to show in the log.
     */
    private fun triggerAction(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            val message: String = e.message ?: "Unknown error"
            refreshLog("Error: " + message)
        }
    }

    private fun createCard(x: Int, y: Int, rot: Double = 0.0): CardView {
        val view: CardView = CardView(posX = x, posY = y, width = 75, height = 110, front = cardImages.blankImage, back = cardImages.backImage)
        view.rotation = rot
        return view
    }
    
    private fun createPlayerLabel(cx: Int, cy: Int, rot: Double): Label {
        val label: Label = Label(posX = cx - 100, posY = cy - 15, width = 200, height = 30, "")
        label.font = Font(size = 20, fontWeight = Font.FontWeight.BOLD)
        label.rotation = rot
        return label
    }

    override fun refreshAfterStartNewGame() { updateUI() }
    
    override fun refreshAfterTurnStart() {
        selectedOpenIdx = null
        selectedCenterIdx = null
        updateUI()
    }
    
    override fun refreshAfterTurnEnd() { updateUI() }
    override fun refreshAfterPushLeft(newCard: Card) { updateUI() }
    override fun refreshAfterPushRight(newCard: Card) { updateUI() }
    override fun refreshAfterSwitch() { updateUI() }
    override fun refreshAfterError(message: String) { refreshLog("Error: " + message) }
    
    override fun refreshLog(message: String) {
        logLines.add(message)
        if (logLines.size > 5) {
            logLines.removeAt(0)
        }
        
        var text1: String = ""
        if (logLines.size > 0) { text1 = logLines[0] }
        log1.text = text1
        
        var text2: String = ""
        if (logLines.size > 1) { text2 = logLines[1] }
        log2.text = text2
        
        var text3: String = ""
        if (logLines.size > 2) { text3 = logLines[2] }
        log3.text = text3
        
        var text4: String = ""
        if (logLines.size > 3) { text4 = logLines[3] }
        log4.text = text4
        
        var text5: String = ""
        if (logLines.size > 4) { text5 = logLines[4] }
        log5.text = text5
    }

    /**
     * Main update function to synchronize the visual components with the game state.
     */
    private fun updateUI() {
        val game: entity.Game? = rootService.currentGame
        if (game == null) {
            return
        }
        
        val player: entity.Player = game.players[game.currentPlayerIndex]

        roundField.text = "ROUND: " + game.currentRound + "/" + game.totalRounds
        actionsField.text = "Actions: " + player.actionsLeft + "/2"
        currentPlayerLabel.text = "Current Player: " + player.name

        // Center card positions and visuals
        val centerViews: List<CardView> = listOf(center0, center1, center2)
        for (i in 0 until centerViews.size) {
            val view: CardView = centerViews[i]
            val card: entity.Card? = if (i < game.centerCards.size) game.centerCards[i] else null
            setCard(view, card)
            
            if (selectedCenterIdx == i) {
                view.posY = 290.0
            } else {
                view.posY = 310.0
            }
        }
        
        // Discard stack display
        if (game.discardStack.isNotEmpty()) {
            val topDiscard: entity.Card = game.discardStack.peek()
            setCard(discardView, topDiscard)
        } else {
            setCard(discardView, null)
        }

        // Hide all labels and cards before re-distributing
        val playersViews: List<List<tools.aqua.bgw.components.ComponentView>> = listOf(
            listOf(bH1, bH2, bO1, bO2, bO3, bName),
            listOf(lH1, lH2, lO1, lO2, lO3, lName),
            listOf(tO1, tO2, tO3, tH1, tH2, tName),
            listOf(rO1, rO2, rO3, rH1, rH2, rName)
        )
        for (i in 0 until playersViews.size) {
            val views: List<tools.aqua.bgw.components.ComponentView> = playersViews[i]
            for (j in 0 until views.size) {
                views[j].isVisible = false
            }
        }

        // Place the 2 to 4 players in the correct slots
        for (i in 0 until game.players.size) {
            val p: entity.Player = game.players[i]
            val relativeIndex: Int = (i - game.currentPlayerIndex + game.players.size) % game.players.size
            
            var targetArea: Int = 0
            if (game.players.size == 2) {
                if (relativeIndex == 0) { targetArea = 0 } else { targetArea = 2 }
            } else if (game.players.size == 3) {
                if (relativeIndex == 0) { targetArea = 0 } else if (relativeIndex == 1) { targetArea = 1 } else { targetArea = 3 }
            } else {
                if (relativeIndex == 0) { targetArea = 0 } else if (relativeIndex == 1) { targetArea = 1 } else if (relativeIndex == 2) { targetArea = 2 } else { targetArea = 3 }
            }
            
            val h1: CardView = if (targetArea == 0) bH1 else if (targetArea == 1) lH1 else if (targetArea == 2) tH1 else rH1
            val h2: CardView = if (targetArea == 0) bH2 else if (targetArea == 1) lH2 else if (targetArea == 2) tH2 else rH2
            val o1: CardView = if (targetArea == 0) bO1 else if (targetArea == 1) lO1 else if (targetArea == 2) tO1 else rO1
            val o2: CardView = if (targetArea == 0) bO2 else if (targetArea == 1) lO2 else if (targetArea == 2) tO2 else rO2
            val o3: CardView = if (targetArea == 0) bO3 else if (targetArea == 1) lO3 else if (targetArea == 2) tO3 else rO3
            val nameLabel: Label = if (targetArea == 0) bName else if (targetArea == 1) lName else if (targetArea == 2) tName else rName
            
            h1.isVisible = true
            h2.isVisible = true
            o1.isVisible = true
            o2.isVisible = true
            o3.isVisible = true
            nameLabel.isVisible = true
            nameLabel.text = p.name
            
            // Only show hidden cards for the person currently sitting at the screen
            if (targetArea == 0) {
                val p0Cards: entity.Player = game.players[game.currentPlayerIndex]
                if (0 < p0Cards.hiddenCards.size) { setCard(h1, p0Cards.hiddenCards[0]) }
                if (1 < p0Cards.hiddenCards.size) { setCard(h2, p0Cards.hiddenCards[1]) }
                
                val hand: List<CardView> = listOf(o1, o2, o3)
                for (idx in 0 until hand.size) {
                    val cardView: CardView = hand[idx]
                    val playerCard: entity.Card? = if (idx < p0Cards.openCards.size) p0Cards.openCards[idx] else null
                    setCard(cardView, playerCard)
                    
                    if (selectedOpenIdx == idx) {
                        cardView.posY = 540.0
                    } else {
                        cardView.posY = 560.0
                    }
                }
                bH1.posY = 560.0
                bH2.posY = 560.0
            } else {
                h1.showBack()
                h2.showBack()
                setCard(o1, if (0 < p.openCards.size) p.openCards[0] else null)
                setCard(o2, if (1 < p.openCards.size) p.openCards[1] else null)
                setCard(o3, if (2 < p.openCards.size) p.openCards[2] else null)
            }
        }
    }

    private fun setCard(view: CardView, card: Card?) {
        if (card == null) {
            view.frontVisual = cardImages.blankImage
        } else {
            view.frontVisual = cardImages.frontImageFor(card.suit, card.value)
        }
        view.showFront()
    }
}