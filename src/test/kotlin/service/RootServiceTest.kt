package service

import entity.Game
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests the central RootService initialization.
 */
class RootServiceTest {

    /** Checks if a new root starts with no game. */
    @Test
    fun testCurrentGameInitiallyNull() {
        val rootService = RootService()
        assertNull(rootService.currentGame)
    }

    /** Checks if sub-services are created. */
    @Test
    fun testServicesAreInitialized() {
        val rootService = RootService()
        assertNotNull(rootService.gameService)
        assertNotNull(rootService.playerActionService)
    }

    /** Checks if adding a UI registers it everywhere. */
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

        // Trigger via PlayerActionService
        rootService.playerActionService.onAllRefreshables { refreshLog("b") }

        assertEquals(2, spy.logCalls)
    }
}