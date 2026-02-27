package service

import entity.Game
import entity.Player
import entity.ScoreTable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test class for [GameService].
 *
 * This suite aims for full coverage of:
 * - all public methods
 * - all require/throw branches
 * - both branches in evaluateCards()
 * - both paths in endTurn() (wrap / non-wrap)
 * - refresh callback lines via a [Refreshable] spy
 */
class GameServiceTest {

    private lateinit var rootService: RootService
    private lateinit var spy: RefreshSpy

    /**
     * Captures refresh callback invocations to ensure refresh lines are executed.
     */
    private class RefreshSpy : Refreshable {
        var startCalls = 0
        var endGameCalls = 0
        var turnStartCalls = 0
        var turnEndCalls = 0
        val logs = mutableListOf<String>()
        var lastRankingSize: Int? = null

        override fun refreshAfterStartNewGame() {
            startCalls++
        }

        override fun refreshAfterGameEnd(ranking: List<Player>) {
            endGameCalls++
            lastRankingSize = ranking.size
        }

        override fun refreshAfterTurnStart() {
            turnStartCalls++
        }

        override fun refreshAfterTurnEnd() {
            turnEndCalls++
        }

        override fun refreshLog(message: String) {
            logs += message
        }
    }

    /**
     * Creates a fresh [RootService] and registers a [Refreshable] spy before each test.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        spy = RefreshSpy()
        rootService.addRefreshable(spy)
    }

    /**
     * Verifies startNewGame initializes state correctly and triggers refresh + log.
     */
    @Test
    fun testStartNewGameHappyPath() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)

        val game = rootService.currentGame
        assertNotNull(game)

        assertEquals(2, game.players.size)
        assertEquals(3, game.totalRounds)
        assertEquals(1, game.currentRound)
        assertEquals(0, game.currentPlayerIndex)

        // After setup: 3 center cards
        assertEquals(3, game.centerCards.size)

        // Start refresh callback executed
        assertEquals(1, spy.startCalls)

        // updateLog called inside startNewGame => refreshLog called
        assertTrue(spy.logs.any { it.contains("New game started") })
    }

    /**
     * Verifies startNewGame rejects too few players.
     */
    @Test
    fun testStartNewGameRejectsTooFewPlayers() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice"), totalRounds = 3)
        }
    }

    /**
     * Verifies startNewGame rejects too many players.
     */
    @Test
    fun testStartNewGameRejectsTooManyPlayers() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("A", "B", "C", "D", "E"), totalRounds = 3)
        }
    }

    /**
     * Verifies startNewGame rejects blank names.
     */
    @Test
    fun testStartNewGameRejectsBlankName() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", ""), totalRounds = 3)
        }
    }

    /**
     * Verifies startNewGame rejects non-positive totalRounds.
     */
    @Test
    fun testStartNewGameRejectsNonPositiveRounds() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 0)
        }
    }

    /**
     * Verifies updateLog appends log and triggers refreshLog.
     */
    @Test
    fun testUpdateLogHappyPath() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!
        val before = game.log.size

        rootService.gameService.updateLog("hello")

        assertEquals(before + 1, game.log.size)
        assertEquals("hello", game.log.last())
        assertTrue(spy.logs.contains("hello"))
    }

    /**
     * Verifies updateLog fails without active game.
     */
    @Test
    fun testUpdateLogFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.updateLog("x")
        }
    }

    /**
     * Verifies refillDrawStack moves cards from discardStack into drawStack.
     */
    @Test
    fun testRefillDrawStackHappyPath() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!

        // put two cards into discard
        val c1 = game.drawStack.pop()
        val c2 = game.drawStack.pop()
        game.discardStack.push(c1)
        game.discardStack.push(c2)

        val beforeDraw = game.drawStack.size
        val beforeDiscard = game.discardStack.size

        rootService.gameService.refillDrawStack()

        assertEquals(0, game.discardStack.size)
        assertEquals(beforeDraw + beforeDiscard, game.drawStack.size)
    }

    /**
     * Verifies refillDrawStack fails when discardStack is empty.
     */
    @Test
    fun testRefillDrawStackFailsWhenDiscardEmpty() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!
        game.discardStack.clear()

        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.refillDrawStack()
        }
    }

    /**
     * Verifies refillDrawStack fails without active game.
     */
    @Test
    fun testRefillDrawStackFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.refillDrawStack()
        }
    }

    /**
     * Verifies evaluateCards assigns NONE if player has no cards.
     */
    @Test
    fun testEvaluateCardsNoneBranch() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!
        val player = game.players[0]

        player.hiddenCards.clear()
        player.openCards.clear()

        rootService.gameService.evaluateCards(player)

        assertEquals(ScoreTable.NONE, player.score)
    }

    /**
     * Verifies evaluateCards assigns HIGHCARD if player has at least one card.
     */
    @Test
    fun testEvaluateCardsHighCardBranch() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!
        val player = game.players[0]

        // ensure at least one card exists
        if (player.hiddenCards.isEmpty()) {
            player.hiddenCards.add(game.drawStack.pop())
        }
        player.openCards.clear()

        rootService.gameService.evaluateCards(player)

        assertEquals(ScoreTable.HIGHCARD, player.score)
    }

    /**
     * Verifies evaluateCards fails without active game.
     */
    @Test
    fun testEvaluateCardsFailsWithoutGame() {
        val p = Player("X")
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.evaluateCards(p)
        }
    }

    /**
     * Verifies endTurn non-wrap (0->1) does not increment round and resets actionsLeft of next player.
     */
    @Test
    fun testEndTurnNonWrap() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!

        game.currentRound = 1
        game.currentPlayerIndex = 0
        game.players[1].actionsLeft = 0

        rootService.gameService.endTurn()

        assertEquals(1, game.currentPlayerIndex)
        assertEquals(1, game.currentRound)
        assertEquals(2, game.players[1].actionsLeft)
        assertTrue(spy.turnEndCalls >= 1)
        assertTrue(spy.turnStartCalls >= 1)
    }

    /**
     * Verifies endTurn wrap-around (1->0) increments round.
     */
    @Test
    fun testEndTurnWrapIncrementsRound() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!

        game.currentRound = 1
        game.currentPlayerIndex = 1

        rootService.gameService.endTurn()

        assertEquals(0, game.currentPlayerIndex)
        assertEquals(2, game.currentRound)
    }

    /**
     * Verifies endTurn fails with invalid player count (<2).
     */
    @Test
    fun testEndTurnFailsInvalidPlayerCount() {
        rootService.currentGame = Game(
            totalRounds = 1,
            currentRound = 1,
            currentPlayerIndex = 0,
            players = mutableListOf(Player("Solo"))
        )

        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Verifies endTurn fails without active game.
     */
    @Test
    fun testEndTurnFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Verifies endGame clears currentGame and triggers refreshAfterGameEnd.
     */
    @Test
    fun testEndGameHappyPath() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!

        // ensure deterministic ranking: Bob wins
        game.players[0].score = ScoreTable.NONE
        game.players[1].score = ScoreTable.ROYALFLUSH

        rootService.gameService.endGame()

        assertNull(rootService.currentGame)
        assertEquals(1, spy.endGameCalls)
        assertEquals(2, spy.lastRankingSize)
    }

    /**
     * Verifies endGame fails without active game.
     */
    @Test
    fun testEndGameFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.endGame()
        }
    }
}