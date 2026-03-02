package service

import entity.Card
import entity.Player
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Test class for [PlayerActionService].
 *
 * Covered behavior:
 * - pushLeft / pushRight success cases
 * - pushLeft / pushRight error branch when drawStack is empty (no action consumed)
 * - switchOne / switchAll success cases
 * - switchOne invalid indices (no action consumed)
 * - error cases: no active game, no actions left
 * - automatic turn end after the second action
 *
 * Refresh callbacks are verified using a [Refreshable] spy.
 */
class PlayerActionServiceTest {

    private lateinit var rootService: RootService
    private lateinit var spy: RefreshSpy

    /**
     * Refreshable spy to track callback invocations.
     */
    private class RefreshSpy : Refreshable {
        var turnStartCalls = 0
        var turnEndCalls = 0
        var pushCalls = 0
        var switchCalls = 0
        var errorCalls = 0
        val logs = mutableListOf<String>()

        override fun refreshAfterTurnStart() {
            turnStartCalls++
        }

        override fun refreshAfterTurnEnd() {
            turnEndCalls++
        }

        override fun refreshAfterPush(newCard: Card, direction: Int) {
            pushCalls++
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

    /**
     * Creates a fresh [RootService] and starts a game before each test.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        spy = RefreshSpy()
        rootService.addRefreshable(spy)
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
    }

    /**
     * Verifies pushLeft consumes an action and triggers refreshAfterPush.
     */
    @Test
    fun testPushLeftSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        val beforeCenter = game.centerCards.toList()

        rootService.playerActionService.pushLeft()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.pushCalls)
        assertTrue(game.centerCards != beforeCenter || game.discardStack.isNotEmpty())
    }

    /**
     * Verifies pushRight consumes an action and triggers refreshAfterPush.
     */
    @Test
    fun testPushRightSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        rootService.playerActionService.pushRight()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.pushCalls)
    }

    /**
     * Verifies pushLeft with empty drawStack triggers refreshAfterError and does not consume an action.
     */
    @Test
    fun testPushLeftEmptyDrawStackBranch() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        game.drawStack.clear()

        rootService.playerActionService.pushLeft()

        assertEquals(2, player.actionsLeft)
        assertEquals(1, spy.errorCalls)
        assertEquals(0, spy.pushCalls)
    }

    /**
     * Verifies pushRight with empty drawStack triggers refreshAfterError and does not consume an action.
     */
    @Test
    fun testPushRightEmptyDrawStackBranch() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        game.drawStack.clear()

        rootService.playerActionService.pushRight()

        assertEquals(2, player.actionsLeft)
        assertEquals(1, spy.errorCalls)
        assertEquals(0, spy.pushCalls)
    }

    /**
     * Verifies switchOne swaps selected cards and consumes an action.
     */
    @Test
    fun testSwitchOneSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        player.openCards.clear()
        player.openCards.add(game.drawStack.pop())

        val beforeOpen = player.openCards[0]
        val beforeCenter = game.centerCards[0]

        rootService.playerActionService.switchOne(0, 0)

        assertEquals(1, player.actionsLeft)
        assertEquals(beforeCenter, player.openCards[0])
        assertEquals(beforeOpen, game.centerCards[0])
        assertEquals(1, spy.switchCalls)
    }

    /**
     * Verifies switchOne fails for an invalid openCardIndex and does not consume an action.
     */
    @Test
    fun testSwitchOneInvalidOpenIndex() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        player.openCards.clear()
        player.openCards.add(game.drawStack.pop())

        assertFailsWith<IndexOutOfBoundsException> {
            rootService.playerActionService.switchOne(openCardIndex = 5, centerCardIndex = 0)
        }

        assertEquals(2, player.actionsLeft)
        assertEquals(0, spy.switchCalls)
    }

    /**
     * Verifies switchOne fails for an invalid centerCardIndex and does not consume an action.
     */
    @Test
    fun testSwitchOneInvalidCenterIndex() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        player.openCards.clear()
        player.openCards.add(game.drawStack.pop())

        assertFailsWith<IndexOutOfBoundsException> {
            rootService.playerActionService.switchOne(openCardIndex = 0, centerCardIndex = 99)
        }

        assertEquals(2, player.actionsLeft)
        assertEquals(0, spy.switchCalls)
    }

    /**
     * Verifies switchAll swaps pairwise up to the minimum list size and consumes an action.
     */
    @Test
    fun testSwitchAllSuccess() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        player.openCards.clear()
        repeat(3) { player.openCards.add(game.drawStack.pop()) }

        val beforeOpen0 = player.openCards[0]
        val beforeCenter0 = game.centerCards[0]

        rootService.playerActionService.switchAll()

        assertEquals(1, player.actionsLeft)
        assertEquals(beforeCenter0, player.openCards[0])
        assertEquals(beforeOpen0, game.centerCards[0])
        assertEquals(1, spy.switchCalls)
    }

    /**
     * Verifies switchAll works when the player has fewer open cards than center cards.
     */
    @Test
    fun testSwitchAllWhenOpenCardsSmallerThanCenter() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        player.openCards.clear()
        player.openCards.add(game.drawStack.pop())

        val beforeOpen = player.openCards[0]
        val beforeCenter = game.centerCards[0]

        rootService.playerActionService.switchAll()

        assertEquals(1, player.actionsLeft)
        assertEquals(beforeCenter, player.openCards[0])
        assertEquals(beforeOpen, game.centerCards[0])
        assertEquals(1, spy.switchCalls)
    }

    /**
     * Verifies pushLeft works when centerCards is initially empty.
     */
    @Test
    fun testPushLeftWhenCenterCardsEmpty() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        game.centerCards.clear()

        rootService.playerActionService.pushLeft()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, game.centerCards.size)
        assertEquals(1, spy.pushCalls)
    }

    /**
     * Verifies actions are rejected if the current player has no actions left.
     */
    @Test
    fun testFailsWhenNoActionsLeft() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 0

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.pushLeft()
        }
    }

    /**
     * Verifies actions are rejected if no game is active.
     */
    @Test
    fun testFailsWithoutGame() {
        rootService.currentGame = null

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.pushRight()
        }
    }

    /**
     * Verifies that after the second action the turn ends automatically and the next turn starts.
     */
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
}