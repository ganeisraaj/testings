package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for the [Player] entity.
 *
 * This class verifies the correct initialization of default values
 * and the mutability of player-specific state properties.
 */
class PlayerTest {

    /**
     * Verifies that a newly created Player instance
     * initializes the property actionsLeft with its default value.
     *
     * Expected behavior:
     * - actionsLeft is set to 2 if no explicit value is provided.
     */
    @Test
    fun testDefaultActionsLeft() {
        val player = Player("Alice")

        assertEquals(2, player.actionsLeft)
    }

    /**
     * Verifies that the property actionsLeft is mutable
     * and can be modified after object creation.
     *
     * Expected behavior:
     * - Changing actionsLeft updates the stored value accordingly.
     */
    @Test
    fun testActionsLeftMutable() {
        val player = Player("Bob")

        player.actionsLeft = 1
        assertEquals(1, player.actionsLeft)
    }
}