package service

/**
 * Base class for all services in the logic layer.
 * It manages a list of observers (refreshables) that need to stay updated.
 */
abstract class AbstractRefreshingService {

    /** 
     * A list of all objects that want to be updated when logic state changes.
     */
    private val refreshables: MutableList<Refreshable> = mutableListOf<Refreshable>()

    /**
     * Registers a new refreshable to the internal list.
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables.add(newRefreshable)
    }

    /**
     * Helper function to notify all observers at once.
     * It loops through the list and calls the provided method.
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) {
        val count: Int = refreshables.size
        for (i in 0 until count) {
            val r: Refreshable = refreshables[i]
            r.method()
        }
    }
}