package service

import entity.Card
import entity.CardSuit
import entity.CardValue
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
 * This suite tests:
 * - input validation (require/throw branches)
 * - state initialization in [GameService.startNewGame]
 * - log writing via [GameService.updateLog]
 * - draw/discard transfer via [GameService.refillDrawStack]
 * - full hand ranking via [GameService.evaluateCards]
 * - turn switching and round increment via [GameService.endTurn]
 * - automatic game termination via [GameService.endTurn] when rounds are exceeded
 * - explicit termination via [GameService.endGame]
 *
 * A [Refreshable] spy is used to ensure that refresh callbacks are executed.
 */
class GameServiceTest {

    private lateinit var rootService: RootService
    private lateinit var spy: RefreshSpy

    /**
     * Refresh spy capturing callback invocations to ensure refresh lines are executed.
     */
    private class RefreshSpy : Refreshable {
        var startCalls = 0
        var endGameCalls = 0
        var turnStartCalls = 0
        var turnEndCalls = 0
        var switchCalls = 0
        var pushCalls = 0
        var errorCalls = 0

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

        override fun refreshAfterSwitch() {
            switchCalls++
        }

        override fun refreshAfterPush(newCard: Card, direction: Int) {
            pushCalls++
        }

        override fun refreshAfterError(message: String) {
            errorCalls++
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
     * Helper to create a [Card].
     *
     * @param suit Card suit.
     * @param value Card value.
     */
    private fun c(suit: CardSuit, value: CardValue) = Card(suit, value)

    /**
     * Helper to set a 5-card hand for a player using the modelled distribution:
     * - hiddenCards: 2 cards
     * - openCards: 3 cards
     *
     * @param player Player whose cards are overwritten.
     * @param cards Exactly 5 cards.
     */
    private fun setHand(player: Player, cards: List<Card>) {
        require(cards.size == 5)
        player.hiddenCards.clear()
        player.openCards.clear()
        player.hiddenCards.addAll(cards.take(2))
        player.openCards.addAll(cards.drop(2))
    }

    /**
     * Verifies [GameService.startNewGame] initializes state correctly and triggers refresh + log.
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

        assertEquals(3, game.centerCards.size)
        assertEquals(1, spy.startCalls)
        assertTrue(spy.logs.any { it.contains("New game started") })
    }

    /**
     * Verifies [GameService.startNewGame] accepts the lower bound of 2 players.
     */
    @Test
    fun testStartNewGameBoundaryTwoPlayers() {
        rootService.gameService.startNewGame(mutableListOf("A", "B"), totalRounds = 1)
        val game = rootService.currentGame
        assertNotNull(game)
        assertEquals(2, game.players.size)
    }

    /**
     * Verifies [GameService.startNewGame] accepts the upper bound of 4 players.
     */
    @Test
    fun testStartNewGameBoundaryFourPlayers() {
        rootService.gameService.startNewGame(mutableListOf("A", "B", "C", "D"), totalRounds = 1)
        val game = rootService.currentGame
        assertNotNull(game)
        assertEquals(4, game.players.size)
    }

    /**
     * Verifies [GameService.startNewGame] rejects too few players.
     */
    @Test
    fun testStartNewGameRejectsTooFewPlayers() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice"), totalRounds = 3)
        }
    }

    /**
     * Verifies [GameService.startNewGame] rejects too many players.
     */
    @Test
    fun testStartNewGameRejectsTooManyPlayers() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("A", "B", "C", "D", "E"), totalRounds = 3)
        }
    }

    /**
     * Verifies [GameService.startNewGame] rejects blank names.
     */
    @Test
    fun testStartNewGameRejectsBlankName() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", ""), totalRounds = 3)
        }
    }

    /**
     * Verifies [GameService.startNewGame] rejects non-positive totalRounds (0).
     */
    @Test
    fun testStartNewGameRejectsNonPositiveRoundsZero() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 0)
        }
    }

    /**
     * Verifies [GameService.startNewGame] rejects negative totalRounds.
     */
    @Test
    fun testStartNewGameRejectsNegativeRounds() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = -1)
        }
    }

    /**
     * Verifies [GameService.updateLog] appends log and triggers refreshLog.
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
     * Verifies [GameService.updateLog] fails without active game.
     */
    @Test
    fun testUpdateLogFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.updateLog("x")
        }
    }

    /**
     * Verifies [GameService.refillDrawStack] moves cards from discardStack into drawStack.
     */
    @Test
    fun testRefillDrawStackHappyPath() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!

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
     * Verifies [GameService.refillDrawStack] fails when discardStack is empty.
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
     * Verifies [GameService.refillDrawStack] fails without active game.
     */
    @Test
    fun testRefillDrawStackFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.refillDrawStack()
        }
    }

    /**
     * Verifies [GameService.evaluateCards] assigns NONE if player has no cards.
     */
    @Test
    fun testEvaluateCardsNone() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        player.hiddenCards.clear()
        player.openCards.clear()

        rootService.gameService.evaluateCards(player)

        assertEquals(ScoreTable.NONE, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] assigns HIGHCARD for a hand that is not any higher ranking.
     */
    @Test
    fun testEvaluateCardsHighCard() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.CLUBS, CardValue.ACE),
                c(CardSuit.DIAMONDS, CardValue.KING),
                c(CardSuit.HEARTS, CardValue.NINE),
                c(CardSuit.SPADES, CardValue.SEVEN),
                c(CardSuit.CLUBS, CardValue.TWO)
            )
        )

        rootService.gameService.evaluateCards(player)

        assertEquals(ScoreTable.HIGHCARD, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects PAIR.
     */
    @Test
    fun testEvaluateCardsPair() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.CLUBS, CardValue.TEN),
                c(CardSuit.DIAMONDS, CardValue.TEN),
                c(CardSuit.HEARTS, CardValue.ACE),
                c(CardSuit.SPADES, CardValue.SEVEN),
                c(CardSuit.CLUBS, CardValue.TWO)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.PAIR, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects TWOPAIR.
     */
    @Test
    fun testEvaluateCardsTwoPair() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.CLUBS, CardValue.TEN),
                c(CardSuit.DIAMONDS, CardValue.TEN),
                c(CardSuit.HEARTS, CardValue.FOUR),
                c(CardSuit.SPADES, CardValue.FOUR),
                c(CardSuit.CLUBS, CardValue.ACE)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.TWOPAIR, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects SET (three of a kind).
     */
    @Test
    fun testEvaluateCardsSet() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.CLUBS, CardValue.SEVEN),
                c(CardSuit.DIAMONDS, CardValue.SEVEN),
                c(CardSuit.HEARTS, CardValue.SEVEN),
                c(CardSuit.SPADES, CardValue.ACE),
                c(CardSuit.CLUBS, CardValue.TWO)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.SET, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects STRAIGHT.
     */
    @Test
    fun testEvaluateCardsStraight() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.CLUBS, CardValue.FIVE),
                c(CardSuit.DIAMONDS, CardValue.SIX),
                c(CardSuit.HEARTS, CardValue.SEVEN),
                c(CardSuit.SPADES, CardValue.EIGHT),
                c(CardSuit.CLUBS, CardValue.NINE)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.STRAIGHT, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects FLUSH.
     */
    @Test
    fun testEvaluateCardsFlush() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.HEARTS, CardValue.TWO),
                c(CardSuit.HEARTS, CardValue.FIVE),
                c(CardSuit.HEARTS, CardValue.SEVEN),
                c(CardSuit.HEARTS, CardValue.JACK),
                c(CardSuit.HEARTS, CardValue.KING)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.FLUSH, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects FULLHOUSE.
     */
    @Test
    fun testEvaluateCardsFullHouse() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.CLUBS, CardValue.NINE),
                c(CardSuit.DIAMONDS, CardValue.NINE),
                c(CardSuit.HEARTS, CardValue.NINE),
                c(CardSuit.SPADES, CardValue.KING),
                c(CardSuit.CLUBS, CardValue.KING)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.FULLHOUSE, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects FOUROFAKIND.
     */
    @Test
    fun testEvaluateCardsFourOfAKind() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.CLUBS, CardValue.THREE),
                c(CardSuit.DIAMONDS, CardValue.THREE),
                c(CardSuit.HEARTS, CardValue.THREE),
                c(CardSuit.SPADES, CardValue.THREE),
                c(CardSuit.CLUBS, CardValue.ACE)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.FOUROFAKIND, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects STRAIGHTFLUSH.
     */
    @Test
    fun testEvaluateCardsStraightFlush() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.SPADES, CardValue.FIVE),
                c(CardSuit.SPADES, CardValue.SIX),
                c(CardSuit.SPADES, CardValue.SEVEN),
                c(CardSuit.SPADES, CardValue.EIGHT),
                c(CardSuit.SPADES, CardValue.NINE)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.STRAIGHTFLUSH, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] detects ROYALFLUSH.
     */
    @Test
    fun testEvaluateCardsRoyalFlush() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        setHand(
            player,
            listOf(
                c(CardSuit.HEARTS, CardValue.TEN),
                c(CardSuit.HEARTS, CardValue.JACK),
                c(CardSuit.HEARTS, CardValue.QUEEN),
                c(CardSuit.HEARTS, CardValue.KING),
                c(CardSuit.HEARTS, CardValue.ACE)
            )
        )

        rootService.gameService.evaluateCards(player)
        assertEquals(ScoreTable.ROYALFLUSH, player.score)
    }

    /**
     * Verifies [GameService.evaluateCards] fails without active game.
     */
    @Test
    fun testEvaluateCardsFailsWithoutGame() {
        val p = Player("X")
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.evaluateCards(p)
        }
    }

    /**
     * Verifies [GameService.endTurn] non-wrap (0->1) does not increment round and resets actionsLeft.
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
     * Verifies [GameService.endTurn] wrap-around (1->0) increments round.
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
     * Verifies [GameService.endTurn] ends the game automatically when rounds are exceeded.
     */
    @Test
    fun testEndTurnTriggersEndGameWhenRoundsExceeded() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 1)
        val game = rootService.currentGame!!

        game.currentRound = 1
        game.currentPlayerIndex = 1

        rootService.gameService.endTurn()

        assertNull(rootService.currentGame)
        assertEquals(1, spy.endGameCalls)
        assertEquals(2, spy.lastRankingSize)
    }

    /**
     * Verifies [GameService.endTurn] fails with invalid player count (<2).
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
     * Verifies [GameService.endTurn] fails without active game.
     */
    @Test
    fun testEndTurnFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Verifies [GameService.endGame] clears currentGame and triggers refreshAfterGameEnd.
     */
    @Test
    fun testEndGameHappyPath() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!

        game.players[0].score = ScoreTable.NONE
        game.players[1].score = ScoreTable.ROYALFLUSH

        rootService.gameService.endGame()

        assertNull(rootService.currentGame)
        assertEquals(1, spy.endGameCalls)
        assertEquals(2, spy.lastRankingSize)
    }

    /**
     * Verifies [GameService.endGame] fails without active game.
     */
    @Test
    fun testEndGameFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.endGame()
        }
    }
}