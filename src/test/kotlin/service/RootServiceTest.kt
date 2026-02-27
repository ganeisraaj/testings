package service

import entity.Game
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test class for [RootService].
 *
 * Covers:
 * - initialization (services exist, currentGame is null)
 * - addRefreshable() registers the refreshable in all services
 */
class RootServiceTest {

    /**
     * Verifies that a newly created [RootService] has no active game.
     */
    @Test
    fun testCurrentGameInitiallyNull() {
        val rootService = RootService()
        assertNull(rootService.currentGame)
    }

    /**
     * Verifies that [RootService] initializes contained services.
     */
    @Test
    fun testServicesAreInitialized() {
        val rootService = RootService()
        assertNotNull(rootService.gameService)
        assertNotNull(rootService.playerActionService)
    }

    /**
     * Verifies that addRefreshable() registers the same refreshable at all services.
     *
     * This is tested indirectly: we register a refreshable that increments a counter
     * when refreshLog is called, then we trigger refreshLog once through GameService
     * and once through PlayerActionService.
     */
    @Test
    fun testAddRefreshableRegistersAtAllServices() {
        val rootService = RootService()

        val spy = object : Refreshable {
            var logCalls = 0
            override fun refreshLog(message: String) {
                logCalls++
            }
        }

        rootService.addRefreshable(spy)

        // Trigger via GameService
        rootService.currentGame = Game()
        rootService.gameService.updateLog("a")

        // Trigger via PlayerActionService (uses refreshAfterError in our implementation)
        rootService.playerActionService.onAllRefreshables { refreshLog("b") }

        assertEquals(2, spy.logCalls)
    }
}