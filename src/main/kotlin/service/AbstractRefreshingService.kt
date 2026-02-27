package service

/**
 * Abstract base class for services that need to notify registered [Refreshable] instances.
 *
 * This class maintains a list of [Refreshable] objects (usually UI scenes) and provides
 * helper methods to register them and to execute a refresh callback on all registered
 * refreshables after the game state has changed.
 */
abstract class AbstractRefreshingService {

    /** The list of registered refreshable objects that are notified on updates. */
    private val refreshables = mutableListOf<Refreshable>()

    /**
     * Registers a new [Refreshable] to receive refresh callbacks.
     *
     * @param newRefreshable The refreshable instance to be added.
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables += newRefreshable
    }

    /**
     * Executes the given [method] on all registered [Refreshable] instances.
     *
     * This should be called by service methods after they have changed the game state,
     * so that the UI can update accordingly.
     *
     * @param method The refresh callback to execute for each registered refreshable.
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) {
        refreshables.forEach { it.method() }
    }
}