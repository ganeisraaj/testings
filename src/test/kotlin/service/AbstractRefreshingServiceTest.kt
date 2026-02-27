package service

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for [AbstractRefreshingService].
 *
 * Covers:
 * - onAllRefreshables() with empty refreshable list
 * - addRefreshable() + onAllRefreshables() with one registered refreshable
 */
class AbstractRefreshingServiceTest {

    /**
     * Minimal concrete implementation for testing [AbstractRefreshingService].
     */
    private class DummyService : AbstractRefreshingService()

    /**
     * Verifies that onAllRefreshables() does not throw when no refreshables are registered.
     */
    @Test
    fun testOnAllRefreshablesWithEmptyListDoesNotThrow() {
        val service = DummyService()
        service.onAllRefreshables { refreshAfterTurnStart() }
        // no assertion needed: test passes if no exception is thrown
    }

    /**
     * Verifies that addRefreshable() registers a refreshable and that onAllRefreshables()
     * executes the provided callback on it.
     */
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