package service

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test class for [PlayerActionService].
 *
 * Covers:
 * - pushLeft / pushRight success + drawStack empty branch
 * - switchOne / switchAll success
 * - error branches: no game, no actions left
 * - refresh callbacks via [Refreshable]
 */
class PlayerActionServiceTest {

    private lateinit var rootService: RootService
    private lateinit var spy: RefreshSpy

    /**
     * Refreshable spy to ensure callback branches are executed.
     */
    private class RefreshSpy : Refreshable {
        var pushCalls = 0
        var switchCalls = 0
        var errorCalls = 0

        override fun refreshAfterPush(newCard: entity.Card, direction: Int) {
            pushCalls++
        }

        override fun refreshAfterSwitch() {
            switchCalls++
        }

        override fun refreshAfterError(message: String) {
            errorCalls++
        }
    }

    /**
     * Creates a fresh game state before each test.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        spy = RefreshSpy()
        rootService.addRefreshable(spy)
        rootService.gameService.startNewGame(mutableListOf("Alice", "Bob"), totalRounds = 3)
    }

    /**
     * Verifies that pushLeft() reduces actions and triggers refreshAfterPush when drawStack not empty.
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
        assertTrue(game.centerCards.size >= 1)
        assertTrue(game.centerCards != beforeCenter || game.discardStack.isNotEmpty())
    }

    /**
     * Verifies that pushRight() reduces actions and triggers refreshAfterPush when drawStack not empty.
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
     * Verifies that pushLeft() handles empty drawStack by triggering refreshAfterError and returning.
     */
    @Test
    fun testPushLeftEmptyDrawStackBranch() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        game.drawStack.clear()

        rootService.playerActionService.pushLeft()

        // reduceAction happens before the empty-stack check
        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.errorCalls)
    }

    /**
     * Verifies that pushRight() handles empty drawStack by triggering refreshAfterError and returning.
     */
    @Test
    fun testPushRightEmptyDrawStackBranch() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        game.drawStack.clear()

        rootService.playerActionService.pushRight()

        assertEquals(1, player.actionsLeft)
        assertEquals(1, spy.errorCalls)
    }

    /**
     * Verifies that switchOne() swaps a selected open card with a selected center card.
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
     * Verifies that switchAll() swaps pairwise up to min(openCards.size, centerCards.size).
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
     * Verifies that an action fails if the current player has no actions left.
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
     * Verifies pushLeft when centerCards is initially empty (covers false branch).
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
    }

    /**
     * Verifies switchAll when player has fewer open cards than center cards.
     */
    @Test
    fun testSwitchAllWhenOpenCardsSmallerThanCenter() {
        val game = rootService.currentGame!!
        val player = game.players[game.currentPlayerIndex]
        player.actionsLeft = 2

        // centerCards has 3
        player.openCards.clear()
        player.openCards.add(game.drawStack.pop()) // only 1 open card

        val beforeOpen = player.openCards[0]
        val beforeCenter = game.centerCards[0]

        rootService.playerActionService.switchAll()

        assertEquals(beforeCenter, player.openCards[0])
        assertEquals(beforeOpen, game.centerCards[0])
    }
    /**
     * Verifies that an action fails if no game is active.
     */
    @Test
    fun testFailsWithoutGame() {
        rootService.currentGame = null

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.pushRight()
        }
    }
}