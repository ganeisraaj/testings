package gui

import entity.Card
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * Main game scene for Push Poker.
 * Layout based on storyboard requirements.
 */
class GameScene(
    private val rootService: RootService
) : BoardGameScene(1200, 700), Refreshable {

    private val cardImages = CardImageLoader()
    private val tableGreen = ColorVisual(46, 125, 50)
    private val panelLight = ColorVisual(245, 245, 245, 180)

    private val bg = Label(0, 0, 1200, 700, "").apply {
        visual = tableGreen
    }

    // Top bars
    private val roundField = Label(20, 20, 150, 40, "ROUND: 1/1").apply { 
        visual = ColorVisual(180, 220, 180)
        font = Font(fontWeight = Font.FontWeight.BOLD)
    }
    private val actionsField = Label(1030, 20, 150, 40, "Actions: 2/2").apply { 
        visual = ColorVisual(180, 220, 180)
        font = Font(fontWeight = Font.FontWeight.BOLD)
    }
    private val currentPlayerLabel = Label(400, 20, 400, 40, "Player: -").apply {
        font = Font(size = 20, fontWeight = Font.FontWeight.BOLD)
    }

    // Log
    private val logTitle = Label(40, 460, 320, 30, "LOG")
    private val log1 = Label(40, 495, 320, 25, "").apply { visual = ColorVisual(255,255,255,100) }
    private val log2 = Label(40, 520, 320, 25, "").apply { visual = ColorVisual(255,255,255,100) }
    private val log3 = Label(40, 545, 320, 25, "").apply { visual = ColorVisual(255,255,255,100) }
    private val log4 = Label(40, 570, 320, 25, "").apply { visual = ColorVisual(255,255,255,100) }
    private val log5 = Label(40, 595, 320, 25, "").apply { visual = ColorVisual(255,255,255,100) }
    
    private val logBG = Label(40, 490, 320, 140, "").apply { visual = panelLight }

    // Helper constructors
    private fun createCard(x: Int, y: Int, rot: Double = 0.0) = CardView(posX = x, posY = y, width = 75, height = 110, front = cardImages.blankImage, back = cardImages.backImage).apply { rotation = rot }
    private fun createPlayerLabel(cx: Int, cy: Int, rot: Double) = Label(posX = cx - 100, posY = cy - 15, width = 200, height = 30, "").apply { font = Font(size = 20, fontWeight = Font.FontWeight.BOLD); rotation = rot; text = "test" }

    // Bottom (Player 0 / Current)
    private val bH1 = createCard(350, 520)
    private val bH2 = createCard(430, 520)
    private val bO1 = createCard(540, 520)
    private val bO2 = createCard(620, 520)
    private val bO3 = createCard(700, 520)
    private val bName = createPlayerLabel(560, 490, 0.0)
    
    // Left
    private val lH1 = createCard(60, 180, 90.0)
    private val lH2 = createCard(60, 260, 90.0)
    private val lO1 = createCard(60, 360, 90.0)
    private val lO2 = createCard(60, 440, 90.0)
    private val lO3 = createCard(60, 520, 90.0)
    private val lName = createPlayerLabel(160, 350, 90.0)
    
    // Top
    private val tO1 = createCard(350, 50, 180.0)
    private val tO2 = createCard(430, 50, 180.0)
    private val tO3 = createCard(510, 50, 180.0)
    private val tH1 = createCard(620, 50, 180.0)
    private val tH2 = createCard(700, 50, 180.0)
    private val tName = createPlayerLabel(560, 190, 180.0)
    
    // Right
    private val rO1 = createCard(1060, 150, 270.0)
    private val rO2 = createCard(1060, 230, 270.0)
    private val rO3 = createCard(1060, 310, 270.0)
    private val rH1 = createCard(1060, 410, 270.0)
    private val rH2 = createCard(1060, 490, 270.0)
    private val rName = createPlayerLabel(960, 350, 270.0)

    private val allPlayerCards = listOf(
        bH1, bH2, bO1, bO2, bO3, bName,
        tO1, tO2, tO3, tH1, tH2, tName,
        lH1, lH2, lO1, lO2, lO3, lName,
        rO1, rO2, rO3, rH1, rH2, rName
    )
    
    private val center0 = CardView(posX = 485, posY = 295, width = 75, height = 110, front = cardImages.blankImage, back = cardImages.backImage)
    private val center1 = CardView(posX = 565, posY = 295, width = 75, height = 110, front = cardImages.blankImage, back = cardImages.backImage)
    private val center2 = CardView(posX = 645, posY = 295, width = 75, height = 110, front = cardImages.blankImage, back = cardImages.backImage)

    private val drawStackView = CardView(posX = 350, posY = 295, width = 75, height = 110, front = cardImages.backImage, back = cardImages.backImage)
    private val discardView = CardView(posX = 790, posY = 295, width = 75, height = 110, front = cardImages.blankImage, back = cardImages.backImage)
    
    private val drawStackLabel = Label(350, 295, 75, 110, "DrawStack").apply { font = Font(size = 12, color = Color(255, 255, 255)) }
    private val discardLabel = Label(790, 295, 75, 110, "Discard").apply { font = Font(size = 12) }

    // Arrows
    private val leftArrow = Button(435, 330, 40, 40, "<").apply { visual = ColorVisual(255, 150, 0); font = Font(size = 24, fontWeight = Font.FontWeight.BOLD) }
    private val rightArrow = Button(735, 330, 40, 40, ">").apply { visual = ColorVisual(255, 150, 0); font = Font(size = 24, fontWeight = Font.FontWeight.BOLD) }

    // Controls
    private val switchActionBtn = Button(1020, 500, 160, 40, "Switch One").apply { visual = ColorVisual(255, 150, 0) }
    private val switchAllBtn = Button(1020, 550, 160, 40, "Switch All").apply { visual = ColorVisual(255, 150, 0) }
    
    private val oIdxLabel = Label(1020, 595, 75, 20, "Hand(0-2)")
    private val cIdxLabel = Label(1105, 595, 75, 20, "Center(0-2)")
    private val oIdxInput = TextField(1020, 615, 75, 35).apply { prompt = "0-2" }
    private val cIdxInput = TextField(1105, 615, 75, 35).apply { prompt = "0-2" }
    
    private val endTurnBtn = Button(850, 640, 150, 50, "End Turn").apply {
        visual = ColorVisual(255, 150, 0)
        font = Font(fontWeight = Font.FontWeight.BOLD)
    }

    init {
        rootService.addRefreshable(this)
        addComponents(
            bg, roundField, actionsField, currentPlayerLabel,
            logTitle, logBG, log1, log2, log3, log4, log5,
            drawStackView, drawStackLabel, discardView, discardLabel,
            center0, center1, center2,
            leftArrow, rightArrow,
            oIdxLabel, cIdxLabel, oIdxInput, cIdxInput, switchActionBtn, switchAllBtn, endTurnBtn
        )
        
        allPlayerCards.forEach { addComponents(it) }

        leftArrow.onMouseClicked = click@{ wrapAction { rootService.playerActionService.pushLeft() }; return@click }
        rightArrow.onMouseClicked = click@{ wrapAction { rootService.playerActionService.pushRight() }; return@click }
        switchAllBtn.onMouseClicked = click@{ wrapAction { rootService.playerActionService.switchAll() }; return@click }
        switchActionBtn.onMouseClicked = click@{
            val o = oIdxInput.text.toIntOrNull()
            val c = cIdxInput.text.toIntOrNull()
            if (o != null && c != null) {
                wrapAction { rootService.playerActionService.switchOne(o, c) }
            }
            return@click
        }
        endTurnBtn.onMouseClicked = click@{ wrapAction { rootService.gameService.endTurn() }; return@click }
    }

    private fun wrapAction(block: () -> Unit) {
        try { block() } catch (e: Exception) { refreshLog("Error: ${e.message}") }
    }

    override fun refreshAfterStartNewGame() = updateUI()
    override fun refreshAfterTurnStart() = updateUI()
    override fun refreshAfterTurnEnd() = updateUI()
    override fun refreshAfterPush(newCard: Card, direction: Int) = updateUI()
    override fun refreshAfterSwitch() = updateUI()
    override fun refreshAfterError(message: String) = refreshLog("Error: $message")
    
    private val logLines = mutableListOf<String>()
    override fun refreshLog(message: String) {
        logLines.add(message)
        if (logLines.size > 5) logLines.removeAt(0)
        
        log1.text = logLines.getOrNull(0) ?: ""
        log2.text = logLines.getOrNull(1) ?: ""
        log3.text = logLines.getOrNull(2) ?: ""
        log4.text = logLines.getOrNull(3) ?: ""
        log5.text = logLines.getOrNull(4) ?: ""
    }

    private fun updateUI() {
        val game = rootService.currentGame ?: return
        val player = game.players[game.currentPlayerIndex]

        roundField.text = "ROUND: ${game.currentRound}/${game.totalRounds}"
        actionsField.text = "Actions: ${player.actionsLeft}/2"
        currentPlayerLabel.text = "Player: ${player.name}"

        // Update center cards
        setCard(center0, game.centerCards.getOrNull(0))
        setCard(center1, game.centerCards.getOrNull(1))
        setCard(center2, game.centerCards.getOrNull(2))

        // Reset all player cards
        allPlayerCards.forEach { it.isVisible = false }

        // Setup Player UI areas
        for (i in game.players.indices) {
            val p = game.players[i]
            val rel = (i - game.currentPlayerIndex + game.players.size) % game.players.size
            
            val target = when(game.players.size) {
                2 -> if (rel == 0) 0 else 2 // 0=Bottom, 2=Top
                3 -> when (rel) { 0 -> 0; 1 -> 1; else -> 3 } // Bottom, Left, Right
                else -> when (rel) { 0 -> 0; 1 -> 1; 2 -> 2; else -> 3 }
            }
            
            val h1 = when(target) { 0 -> bH1; 1 -> lH1; 2 -> tH1; else -> rH1 }
            val h2 = when(target) { 0 -> bH2; 1 -> lH2; 2 -> tH2; else -> rH2 }
            val o1 = when(target) { 0 -> bO1; 1 -> lO1; 2 -> tO1; else -> rO1 }
            val o2 = when(target) { 0 -> bO2; 1 -> lO2; 2 -> tO2; else -> rO2 }
            val o3 = when(target) { 0 -> bO3; 1 -> lO3; 2 -> tO3; else -> rO3 }
            val nameLabel = when(target) { 0 -> bName; 1 -> lName; 2 -> tName; else -> rName }
            
            h1.isVisible = true; h2.isVisible = true
            o1.isVisible = true; o2.isVisible = true; o3.isVisible = true
            nameLabel.isVisible = true
            nameLabel.text = p.name
            
            // Only show hidden card fronts for the current player
            if (target == 0) {
                setCard(h1, p.hiddenCards.getOrNull(0))
                setCard(h2, p.hiddenCards.getOrNull(1))
            } else {
                h1.showBack()
                h2.showBack()
            }
            
            setCard(o1, p.openCards.getOrNull(0))
            setCard(o2, p.openCards.getOrNull(1))
            setCard(o3, p.openCards.getOrNull(2))
        }
    }

    private fun setCard(view: CardView, card: Card?) {
        view.frontVisual = if (card == null) cardImages.blankImage else cardImages.frontImageFor(card.suit, card.value)
        view.showFront()
    }
}