package service

/**
 * Base class for services that update the UI.
 */
abstract class AbstractRefreshingService {

    /** List of refreshable objects. */
    private val refreshables = mutableListOf<Refreshable>()

    /**
     * Adds a new refreshable to the list.
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables += newRefreshable
    }

    /**
     * Calls a refresh method on all registered refreshables.
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) {
        refreshables.forEach { it.method() }
    }
}