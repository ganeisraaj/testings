package service

/**
 * Both services extend this class.
 * It keeps track of who needs to be updated when something changes.
 */
abstract class AbstractRefreshingService {

    // This list holds all the things that want updates
    private val refreshables: MutableList<Refreshable> = mutableListOf<Refreshable>()

    /**
     * Adds something new to the list of refreshables.
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables.add(newRefreshable)
    }

    /**
     * Calls a refresh method on every single refreshable in the list.
     * loop through manually so we know exactly whats happening.
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) {
        // Get the total count first
        val count: Int = refreshables.size

        // Go through each one and call the method
        for (i in 0 until count) {
            val r: Refreshable = refreshables[i]
            r.method()
        }
    }

}