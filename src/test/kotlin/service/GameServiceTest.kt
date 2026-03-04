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
 * Tests for GameService logic.
 */
class GameServiceTest {

    private lateinit var rootService: RootService
    private lateinit var spy: RefreshSpy

    /**
     * Spy to check if refresh methods are called.
     */
    private class RefreshSpy : Refreshable {
        var startCalls = 0
        var endGameCalls = 0
        var turnStartCalls = 0
        var turnEndCalls = 0
        var switchCalls = 0
        var pushLeftCalls = 0
        var pushRightCalls = 0
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

        override fun refreshAfterPushLeft(newCard: Card) {
            pushLeftCalls++
        }

        override fun refreshAfterPushRight(newCard: Card) {
            pushRightCalls++
        }

        override fun refreshAfterError(message: String) {
            errorCalls++
        }

        override fun refreshLog(message: String) {
            logs += message
        }
    }

    @BeforeTest
    fun setUp() {
        rootService = RootService()
        spy = RefreshSpy()
        rootService.addRefreshable(spy)
    }

    /** Helper for creating cards. */
    private fun c(suit: CardSuit, value: CardValue) = Card(suit, value)

    /** Helper to set player cards. */
    private fun setHand(player: Player, cards: List<Card>) {
        require(cards.size == 5)
        player.hiddenCards.clear()
        player.openCards.clear()
        player.hiddenCards.addAll(cards.take(2))
        player.openCards.addAll(cards.drop(2))
    }

    /** Checks if a game starts correctly. */
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

    /** Checks boundary case for 2 players. */
    @Test
    fun testStartNewGameBoundaryTwoPlayers() {
        rootService.gameService.startNewGame(mutableListOf("A", "B"), totalRounds = 1)
        val game = rootService.currentGame
        assertNotNull(game)
        assertEquals(2, game.players.size)
    }

    /** Checks boundary case for 4 players. */
    @Test
    fun testStartNewGameBoundaryFourPlayers() {
        rootService.gameService.startNewGame(mutableListOf("A", "B", "C", "D"), totalRounds = 1)
        val game = rootService.currentGame
        assertNotNull(game)
        assertEquals(4, game.players.size)
    }

    /** Validates player count minimum. */
    @Test
    fun testStartNewGameRejectsTooFewPlayers() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice"), totalRounds = 3)
        }
    }

    /** Validates player count maximum. */
    @Test
    fun testStartNewGameRejectsTooManyPlayers() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("A", "B", "C", "D", "E"), totalRounds = 3)
        }
    }

    /** Rejects blank names. */
    @Test
    fun testStartNewGameRejectsBlankName() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", ""), totalRounds = 3)
        }
    }

    /** Rejects 0 rounds. */
    @Test
    fun testStartNewGameRejectsNonPositiveRoundsZero() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 0)
        }
    }

    /** Rejects negative rounds. */
    @Test
    fun testStartNewGameRejectsNegativeRounds() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = -1)
        }
    }

    /** Validates log updates. */
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

    /** Fails updateLog without game. */
    @Test
    fun testUpdateLogFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.updateLog("x")
        }
    }

    /** Checks stack refilling logic. */
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

    /** Fails refill if discard is empty. */
    @Test
    fun testRefillDrawStackFailsWhenDiscardEmpty() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val game = rootService.currentGame!!
        game.discardStack.clear()

        assertFailsWith<IllegalStateException> {
            rootService.gameService.refillDrawStack()
        }
    }

    /** Rejects refill without game. */
    @Test
    fun testRefillDrawStackFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.refillDrawStack()
        }
    }

    /** Handles empty hands in evaluation. */
    @Test
    fun testEvaluateCardsNone() {
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
        val player = rootService.currentGame!!.players[0]

        player.hiddenCards.clear()
        player.openCards.clear()

        rootService.gameService.evaluateCards(player)
        val score = player.score

        assertEquals(ScoreTable.NONE, score)
    }

    /** Evaluates High Card. */
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
        val score = player.score

        assertEquals(ScoreTable.HIGHCARD, score)
    }

    /** Evaluates Pair. */
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
        val score = player.score
        assertEquals(ScoreTable.PAIR, score)
    }

    /** Evaluates Two Pair. */
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
        val score = player.score
        assertEquals(ScoreTable.TWOPAIR, score)
    }

    /** Evaluates Set. */
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
        val score = player.score
        assertEquals(ScoreTable.SET, score)
    }

    /** Evaluates Straight. */
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
        val score = player.score
        assertEquals(ScoreTable.STRAIGHT, score)
    }

    /** Evaluates Flush. */
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
        val score = player.score
        assertEquals(ScoreTable.FLUSH, score)
    }

    /** Evaluates Full House. */
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
        val score = player.score
        assertEquals(ScoreTable.FULLHOUSE, score)
    }

    /** Evaluates Four of a Kind. */
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
        val score = player.score
        assertEquals(ScoreTable.FOUROFAKIND, score)
    }

    /** Evaluates Straight Flush. */
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
        val score = player.score
        assertEquals(ScoreTable.STRAIGHTFLUSH, score)
    }

    /** Evaluates Royal Flush. */
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
        val score = player.score
        assertEquals(ScoreTable.ROYALFLUSH, score)
    }

    /** Rejects evaluation without game. */
    @Test
    fun testEvaluateCardsFailsWithoutGame() {
        val p = Player("X")
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.evaluateCards(p)
        }
    }

    /** Tests endTurn logic. */
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

    /** Checks round increments. */
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

    /** Checks automatic game end. */
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

    /** Fails endTurn without game. */
    @Test
    fun testEndTurnFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.endTurn()
        }
    }

    /** Checks endGame logic. */
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

    /** Fails endGame without game. */
    @Test
    fun testEndGameFailsWithoutGame() {
        assertFailsWith<IllegalArgumentException> {
            rootService.gameService.endGame()
        }
    }
}