package service

import entity.Card
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for PlayerActionService logic.
 */
class PlayerActionServiceTest {

    private lateinit var rootService: RootService
    private lateinit var spy: RefreshSpy

    /**
     * Spy to track UI refresh calls.
     */
    private class RefreshSpy : Refreshable {
        var turnStartCalls = 0
        var turnEndCalls = 0
        var pushLeftCalls = 0
        var pushRightCalls = 0
        var switchCalls = 0
        var errorCalls = 0
        val logs = mutableListOf<String>()

        override fun refreshAfterTurnStart() {
            turnStartCalls++
        }

        override fun refreshAfterTurnEnd() {
            turnEndCalls++
        }

        override fun refreshAfterPushLeft(newCard: Card) {
            pushLeftCalls++
        }

        override fun refreshAfterPushRight(newCard: Card) {
            pushRightCalls++
        }

        override fun refreshAfterSwitch() {
            switchCalls++
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
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
    }

    /** Tests successful push left. */
    @Test
    fun testPushLeftSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        val beforeCenter = game.centerCards.toList()

        rootService.playerActionService.pushLeft()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.pushLeftCalls)
        assertTrue(game.centerCards != beforeCenter || game.discardStack.isNotEmpty())
    }

    /** Tests successful push right. */
    @Test
    fun testPushRightSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        rootService.playerActionService.pushRight()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.pushRightCalls)
    }

    /** Tests push left when card deck is empty. */
    @Test
    fun testPushLeftEmptyDrawStackReshuffle() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        // Move all to discard except one card in center
        while (game.drawStack.isNotEmpty()) {
            game.discardStack.push(game.drawStack.pop())
        }

        rootService.playerActionService.pushLeft()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.pushLeftCalls)
        assertTrue(game.drawStack.isNotEmpty())
    }

    /** Tests successful single card switch. */
    @Test
    fun testSwitchOneSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        val beforeOpen = player.openCards[0]
        val beforeCenter = game.centerCards[0]

        rootService.playerActionService.switchOne(0, 0)

        assertEquals(1, player.actionsLeft)
        assertEquals(beforeCenter, player.openCards[0])
        assertEquals(beforeOpen, game.centerCards[0])
        assertEquals(1, spy.switchCalls)
    }

    /** Rejects switch with broad index. */
    @Test
    fun testSwitchOneInvalidOpenIndex() {
        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.switchOne(openCardIndex = 5, centerCardIndex = 0)
        }
    }

    /** Rejects switch with broad center index. */
    @Test
    fun testSwitchOneInvalidCenterIndex() {
        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.switchOne(openCardIndex = 0, centerCardIndex = 99)
        }
    }

    /** Tests full switch logic. */
    @Test
    fun testSwitchAllSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        val beforeOpen0 = player.openCards[0]
        val beforeCenter0 = game.centerCards[0]

        rootService.playerActionService.switchAll()

        assertEquals(1, player.actionsLeft)
        assertEquals(beforeCenter0, player.openCards[0])
        assertEquals(beforeOpen0, game.centerCards[0])
        assertEquals(1, spy.switchCalls)
    }

    /** Fails action when points are zero. */
    @Test
    fun testFailsWhenNoActionsLeft() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 0

        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.pushLeft()
        }
    }

    /** Rejects moves if no game is set up. */
    @Test
    fun testFailsWithoutGame() {
        rootService.currentGame = null

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.pushRight()
        }
    }

    /** Checks if turn ends after 2 moves. */
    @Test
    fun testAutoEndTurnAfterSecondAction() {
        val game = rootService.currentGame!!
        val startIndex = game.currentPlayerIndex
        val player = game.players[startIndex]
        player.actionsLeft = 2

        rootService.playerActionService.pushLeft()
        assertEquals(1, player.actionsLeft)

        rootService.playerActionService.pushRight()

        assertEquals((startIndex + 1) % game.players.size, game.currentPlayerIndex)
        assertTrue(spy.turnEndCalls >= 1)
        assertTrue(spy.turnStartCalls >= 1)
    }

    /** If both stacks are empty, pushing should fail. */
    @Test
    fun testPushLeftFailsWhenNoCardsAvailable() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]

        player.actionsLeft = 2
        game.drawStack.clear()
        game.discardStack.clear()

        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.pushLeft()
        }
    }

    /** Same idea as the left push test, but for pushRight. */
    @Test
    fun testPushRightReshuffleFromDiscard() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]

        player.actionsLeft = 2

        // empty the draw stack so refill logic triggers
        while (game.drawStack.isNotEmpty()) {
            game.discardStack.push(game.drawStack.pop())
        }

        rootService.playerActionService.pushRight()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.pushRightCalls)
        assertTrue(game.drawStack.isNotEmpty())
    }

    /** Switching should not work if the player has no actions left. */
    @Test
    fun testSwitchFailsWithoutActions() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]

        player.actionsLeft = 0

        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.switchOne(0, 0)
        }
    }

    /** Push right should fail if no cards exist in draw or discard stacks. */
    @Test
    fun testPushRightFailsWhenNoCardsAvailable() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]

        player.actionsLeft = 2
        game.drawStack.clear()
        game.discardStack.clear()

        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.pushRight()
        }
    }

    /** switchAll should fail if the player has no actions left. */
    @Test
    fun testSwitchAllFailsWithoutActions() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]

        player.actionsLeft = 0

        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.switchAll()
        }
    }

    /** switchAll should fail if the player does not have 3 open cards (guard branch). */
    @Test
    fun testSwitchAllFailsWhenOpenCardsMissing() {
        // make sure we start clean for this test
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)

        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        player.openCards.clear()

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.switchAll()
        }
    }

    /** Switching should fail when index is negative. */
    @Test
    fun testSwitchOneNegativeIndex() {
        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.switchOne(-1, 0)
        }
    }

    /** switchAll should fail if the center cards are missing. */
    @Test
    fun testSwitchAllFailsWhenCenterEmpty() {
        // make sure we start clean for this test
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)

        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]

        player.actionsLeft = 2
        game.centerCards.clear()

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.switchAll()
        }
    }

    /** switchAll should fail if there is no running game. */
    @Test
    fun testSwitchAllFailsWithoutGame() {
        rootService.currentGame = null

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.switchAll()
        }
    }
}