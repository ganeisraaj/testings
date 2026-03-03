package service

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the base service update mechanism.
 */
class AbstractRefreshingServiceTest {

    private class DummyService : AbstractRefreshingService()

    /** Checks if calling refresh without any UI registered works. */
    @Test
    fun testOnAllRefreshablesWithEmptyListDoesNotThrow() {
        val service = DummyService()
        service.onAllRefreshables { refreshAfterTurnStart() }
    }

    /** Checks if a registered UI actually gets the update. */
    @Test
    fun testAddRefreshableAndCallbackExecution() {
        val service = DummyService()

        val spy = object : Refreshable {
            var calls = 0
            override fun refreshAfterTurnStart() {
                calls++
            }
        }

        service.addRefreshable(spy)
        service.onAllRefreshables { refreshAfterTurnStart() }

        assertEquals(1, spy.calls)
    }
}